use std::sync::Arc;

use crate::asr::{self, AsrBackend, Quality, RawSegment};
use crate::check_sample_rate;
use crate::diarization::{dominant_speaker, Diarizer, SingleSpeaker, SpeakerTurn};
use crate::transcript::Transcript;
use crate::BridgeError;

#[derive(Debug, Clone, uniffi::Record)]
pub struct Segment {
    pub start_ms: u64,
    pub end_ms: u64,
    pub speaker: u32,
    pub text: String,
    /// Word timings, used to split a segment where the speaker changes. Empty if unknown.
    pub words: Vec<Word>,
}

#[derive(Debug, Clone, uniffi::Record)]
pub struct Word {
    pub start_ms: u64,
    pub end_ms: u64,
    pub text: String,
}

#[derive(uniffi::Object)]
pub struct Engine {
    asr: Box<dyn AsrBackend>,
    diarizer: Box<dyn Diarizer>,
}

#[uniffi::export]
impl Engine {
    #[uniffi::constructor]
    pub fn new(whisper_model_path: String) -> Result<Arc<Self>, BridgeError> {
        Ok(Arc::new(Self {
            asr: asr::default_backend(&whisper_model_path)?,
            diarizer: Box::new(SingleSpeaker),
        }))
    }

    /// Transcribes a complete recording. Blocking call: run it on a background thread.
    pub fn transcribe(&self, samples: Vec<f32>, sample_rate: u32) -> Result<Transcript, BridgeError> {
        check_sample_rate(sample_rate)?;
        let segments = self.transcribe_chunk(&samples, 0, Quality::Accurate)?;
        Ok(Transcript { segments })
    }
}

fn to_segment(raw: RawSegment, turns: &[SpeakerTurn], offset_ms: u64) -> Segment {
    Segment {
        speaker: dominant_speaker(turns, raw.start_ms, raw.end_ms).unwrap_or(0),
        start_ms: offset_ms + raw.start_ms,
        end_ms: offset_ms + raw.end_ms,
        text: raw.text,
        words: raw
            .words
            .into_iter()
            .map(|w| Word {
                start_ms: offset_ms + w.start_ms,
                end_ms: offset_ms + w.end_ms,
                text: w.text,
            })
            .collect(),
    }
}

impl Engine {
    /// Transcribes one chunk of 16 kHz audio. Timestamps are shifted by `offset_ms`
    /// so they stay on the timeline of the whole recording.
    pub(crate) fn transcribe_chunk(
        &self,
        samples: &[f32],
        offset_ms: u64,
        quality: Quality,
    ) -> Result<Vec<Segment>, BridgeError> {
        let raw = self.asr.transcribe(samples, quality)?;
        let turns = self.diarizer.diarize(samples)?;

        Ok(raw
            .into_iter()
            .filter(|s| !s.text.is_empty())
            .map(|s| to_segment(s, &turns, offset_ms))
            .collect())
    }
}

