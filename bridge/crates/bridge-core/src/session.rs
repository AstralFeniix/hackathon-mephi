use std::sync::{Arc, Mutex, MutexGuard};

use crate::asr::Quality;
use crate::audio_file::decode_to_16k_mono;
use crate::check_sample_rate;
use crate::engine::{Engine, Segment};
use crate::transcript::Transcript;
use crate::{BridgeError, SAMPLE_RATE};

// Live transcription buffers audio and cuts it at pauses, so each piece is transcribed once.
const RMS_SILENCE: f32 = 0.01;
const FRAME_SAMPLES: usize = SAMPLE_RATE as usize / 100; // 10 ms
const MIN_SILENCE_FRAMES: usize = 40; // 400 ms
const MIN_CHUNK_SAMPLES: usize = SAMPLE_RATE as usize * 3;
const MAX_CHUNK_SAMPLES: usize = SAMPLE_RATE as usize * 15;

// Partial results: the unfinished buffer is re-transcribed about once per second.
const MIN_PARTIAL_SAMPLES: usize = SAMPLE_RATE as usize * 3 / 2;
const PARTIAL_INTERVAL_SAMPLES: usize = SAMPLE_RATE as usize;

#[derive(Default)]
struct State {
    buffer: Vec<f32>,
    buffer_start_ms: u64,
    partial_len: usize,
    segments: Vec<Segment>,
}

#[derive(Debug, Clone, uniffi::Record)]
pub struct LiveUpdate {
    /// Segments that are final and will not change.
    pub finalized: Vec<Segment>,
    /// Current guess for the speech still in progress. Replace the previous partial with it.
    pub partial: Option<Segment>,
}

#[derive(uniffi::Object)]
pub struct Session {
    engine: Arc<Engine>,
    state: Mutex<State>,
}

#[uniffi::export]
impl Session {
    #[uniffi::constructor]
    pub fn new(engine: Arc<Engine>) -> Arc<Self> {
        Arc::new(Self {
            engine,
            state: Mutex::new(State::default()),
        })
    }

    /// Feeds 16 kHz mono audio. Returns the finalized segments and the current partial text.
    /// Blocking call: run it on a background thread.
    pub fn push_audio(&self, samples: Vec<f32>, sample_rate: u32) -> Result<LiveUpdate, BridgeError> {
        check_sample_rate(sample_rate)?;
        self.push(samples, true)
    }

    /// Decodes an mp3 or wav file and transcribes it. Blocking call: run it on a background thread.
    pub fn push_audio_file(&self, path: String) -> Result<Vec<Segment>, BridgeError> {
        let samples = decode_to_16k_mono(&path)?;
        Ok(self.push(samples, false)?.finalized)
    }

    /// Transcribes a raw file written by `decode_to_pcm`. Blocking call: run it on a background thread.
    pub fn push_pcm_file(&self, path: String) -> Result<Vec<Segment>, BridgeError> {
        let bytes = std::fs::read(&path)
            .map_err(|e| BridgeError::InvalidInput(format!("cannot read {path}: {e}")))?;
        let samples = bytes
            .chunks_exact(4)
            .map(|b| f32::from_le_bytes([b[0], b[1], b[2], b[3]]))
            .collect();
        Ok(self.push(samples, false)?.finalized)
    }

    /// Transcribes any audio still buffered and returns the full transcript.
    pub fn finish(&self) -> Result<Transcript, BridgeError> {
        let mut state = self.lock()?;
        if !state.buffer.is_empty() {
            let chunk = std::mem::take(&mut state.buffer);
            let segments = self.engine.transcribe_chunk(&chunk, state.buffer_start_ms, Quality::Accurate)?;
            state.segments.extend(segments);
        }
        Ok(Transcript {
            segments: state.segments.clone(),
        })
    }
}

impl Session {
    fn lock(&self) -> Result<MutexGuard<'_, State>, BridgeError> {
        self.state
            .lock()
            .map_err(|_| BridgeError::Inference("session state poisoned".into()))
    }

    fn push(&self, samples: Vec<f32>, with_partial: bool) -> Result<LiveUpdate, BridgeError> {
        let mut state = self.lock()?;
        state.buffer.extend_from_slice(&samples);

        let mut finalized = Vec::new();
        while let Some(cut) = find_cut(&state.buffer) {
            let chunk: Vec<f32> = state.buffer.drain(..cut).collect();
            let segments = self.engine.transcribe_chunk(&chunk, state.buffer_start_ms, Quality::Accurate)?;
            state.buffer_start_ms += samples_to_ms(cut);
            state.partial_len = 0;
            finalized.extend(segments.iter().cloned());
            state.segments.extend(segments);
        }

        let partial = if with_partial { self.partial(&mut state)? } else { None };
        Ok(LiveUpdate { finalized, partial })
    }

    fn partial(&self, state: &mut State) -> Result<Option<Segment>, BridgeError> {
        let len = state.buffer.len();
        if len < MIN_PARTIAL_SAMPLES || len.saturating_sub(state.partial_len) < PARTIAL_INTERVAL_SAMPLES {
            return Ok(None);
        }
        state.partial_len = len;
        let segments = self.engine.transcribe_chunk(&state.buffer, state.buffer_start_ms, Quality::Fast)?;
        Ok(merge(segments))
    }
}

/// Joins the segments of an unfinished buffer into one partial segment.
fn merge(segments: Vec<Segment>) -> Option<Segment> {
    let first = segments.first()?.clone();
    let last = segments.last()?;
    let text = segments
        .iter()
        .map(|s| s.text.as_str())
        .collect::<Vec<_>>()
        .join(" ");
    Some(Segment {
        start_ms: first.start_ms,
        end_ms: last.end_ms,
        speaker: first.speaker,
        text,
    })
}

/// Returns how many samples to cut from the front of `buffer`, or `None` if no cut is due.
/// Prefers the latest pause (at least 400 ms) that is past the minimum chunk length;
/// otherwise cuts at the maximum chunk length.
fn find_cut(buffer: &[f32]) -> Option<usize> {
    let silent: Vec<bool> = buffer
        .chunks(FRAME_SAMPLES)
        .map(|frame| rms(frame) < RMS_SILENCE)
        .collect();

    let mut cut = None;
    let mut run_start = None;
    for (i, &is_silent) in silent.iter().chain(std::iter::once(&false)).enumerate() {
        if is_silent {
            run_start.get_or_insert(i);
        } else if let Some(start) = run_start.take() {
            let mid = (start + i) * FRAME_SAMPLES / 2;
            if i - start >= MIN_SILENCE_FRAMES && mid >= MIN_CHUNK_SAMPLES {
                cut = Some(mid);
            }
        }
    }

    cut.or_else(|| (buffer.len() >= MAX_CHUNK_SAMPLES).then_some(MAX_CHUNK_SAMPLES))
}

fn rms(frame: &[f32]) -> f32 {
    (frame.iter().map(|s| s * s).sum::<f32>() / frame.len() as f32).sqrt()
}

fn samples_to_ms(samples: usize) -> u64 {
    samples as u64 * 1000 / SAMPLE_RATE as u64
}

#[cfg(test)]
mod tests {
    use super::*;

    fn tone(samples: usize) -> Vec<f32> {
        (0..samples).map(|i| (i as f32 * 0.1).sin() * 0.5).collect()
    }

    #[test]
    fn cuts_in_the_middle_of_a_pause() {
        let mut buf = tone(4 * 16_000);
        buf.extend(vec![0.0; 8_000]); // 500 ms of silence
        buf.extend(tone(16_000));
        let cut = find_cut(&buf).expect("pause should trigger a cut");
        assert!((64_000..=72_000).contains(&cut), "cut at {cut}");
    }

    #[test]
    fn no_cut_before_minimum_chunk_length() {
        let mut buf = tone(16_000);
        buf.extend(vec![0.0; 16_000]);
        assert_eq!(find_cut(&buf), None);
    }

    #[test]
    fn hard_cut_at_maximum_chunk_length() {
        assert_eq!(find_cut(&tone(MAX_CHUNK_SAMPLES)), Some(MAX_CHUNK_SAMPLES));
    }
}
