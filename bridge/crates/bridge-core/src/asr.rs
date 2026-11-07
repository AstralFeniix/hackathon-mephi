use crate::BridgeError;

#[derive(Debug, Clone)]
pub struct RawSegment {
    pub start_ms: u64,
    pub end_ms: u64,
    pub text: String,
}

/// Fast suits partial results that are re-run every second; Accurate suits final segments.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Quality {
    Fast,
    Accurate,
}

/// Speech-to-text backend. Input is 16 kHz mono f32 samples in [-1, 1].
pub trait AsrBackend: Send + Sync {
    fn transcribe(&self, samples: &[f32], quality: Quality) -> Result<Vec<RawSegment>, BridgeError>;
}

#[cfg(feature = "whisper")]
pub fn default_backend(model_path: &str) -> Result<Box<dyn AsrBackend>, BridgeError> {
    Ok(Box::new(WhisperBackend::new(model_path)?))
}

#[cfg(not(feature = "whisper"))]
pub fn default_backend(_model_path: &str) -> Result<Box<dyn AsrBackend>, BridgeError> {
    Ok(Box::new(Unavailable))
}

#[cfg(not(feature = "whisper"))]
struct Unavailable;

#[cfg(not(feature = "whisper"))]
impl AsrBackend for Unavailable {
    fn transcribe(&self, _samples: &[f32], _quality: Quality) -> Result<Vec<RawSegment>, BridgeError> {
        Err(BridgeError::Inference(
            "bridge-core was built without the `whisper` feature".into(),
        ))
    }
}

#[cfg(feature = "whisper")]
pub struct WhisperBackend {
    // Created once and reused: allocating a new state per call dominated the run time.
    state: std::sync::Mutex<whisper_rs::WhisperState>,
}

#[cfg(feature = "whisper")]
impl WhisperBackend {
    pub fn new(model_path: &str) -> Result<Self, BridgeError> {
        let ctx = whisper_rs::WhisperContext::new_with_params(
            model_path,
            whisper_rs::WhisperContextParameters::default(),
        )
        .map_err(|e| BridgeError::ModelLoad(e.to_string()))?;
        let state = ctx
            .create_state()
            .map_err(|e| BridgeError::ModelLoad(e.to_string()))?;
        Ok(Self {
            state: std::sync::Mutex::new(state),
        })
    }
}

#[cfg(feature = "whisper")]
fn thread_count() -> i32 {
    std::thread::available_parallelism()
        .map(|n| n.get())
        .unwrap_or(4)
        .min(8) as i32
}

/// Whisper always encodes a 30 s window, which is slow for short clips. A smaller
/// encoder context (50 frames per second of audio) makes short clips much faster.
#[cfg(feature = "whisper")]
fn audio_ctx_for(samples: usize) -> i32 {
    let seconds = samples as f32 / crate::SAMPLE_RATE as f32;
    ((seconds + 2.0) * 50.0).clamp(300.0, 1500.0) as i32
}

#[cfg(all(test, feature = "whisper"))]
mod bench {
    use super::*;

    /// Manual check: BRIDGE_MODEL=<ggml file> BRIDGE_WAV=<audio> cargo test -p bridge-core --features whisper -- --ignored --nocapture
    #[test]
    #[ignore]
    fn speed_and_text_by_chunk_length() {
        let model = std::env::var("BRIDGE_MODEL").expect("BRIDGE_MODEL");
        let wav = std::env::var("BRIDGE_WAV").expect("BRIDGE_WAV");
        let samples = crate::audio_file::decode_to_16k_mono(&wav).expect("decode");
        let backend = WhisperBackend::new(&model).expect("load model");
        for secs in [5usize, 10] {
            for quality in [Quality::Fast, Quality::Accurate] {
                let chunk = &samples[..(secs * 16_000).min(samples.len())];
                let started = std::time::Instant::now();
                let segments = backend.transcribe(chunk, quality).expect("transcribe");
                let texts: Vec<&str> = segments.iter().map(|s| s.text.as_str()).collect();
                println!("{secs} s {quality:?}: {:?}, {:?}", started.elapsed(), texts);
            }
        }
    }
}

#[cfg(feature = "whisper")]
impl AsrBackend for WhisperBackend {
    fn transcribe(&self, samples: &[f32], quality: Quality) -> Result<Vec<RawSegment>, BridgeError> {
        use whisper_rs::{FullParams, SamplingStrategy};

        let err = |e: whisper_rs::WhisperError| BridgeError::Inference(e.to_string());

        let mut state = self
            .state
            .lock()
            .map_err(|_| BridgeError::Inference("whisper state poisoned".into()))?;

        // audio_ctx 0 means the full 30 s encoder context, which is more accurate.
        let (strategy, audio_ctx) = match quality {
            Quality::Fast => (SamplingStrategy::Greedy { best_of: 1 }, audio_ctx_for(samples.len())),
            Quality::Accurate => (SamplingStrategy::BeamSearch { beam_size: 5, patience: -1.0 }, 0),
        };

        let mut params = FullParams::new(strategy);
        params.set_language(Some("auto"));
        params.set_print_progress(false);
        params.set_print_realtime(false);
        params.set_print_special(false);
        params.set_n_threads(thread_count());
        params.set_audio_ctx(audio_ctx);
        params.set_temperature_inc(0.0);

        state.full(params, samples).map_err(err)?;

        let n = state.full_n_segments().map_err(err)?;
        let mut out = Vec::with_capacity(n as usize);
        for i in 0..n {
            let text = state.full_get_segment_text(i).map_err(err)?;
            // whisper timestamps are in centiseconds (10 ms units)
            let t0 = state.full_get_segment_t0(i).map_err(err)?;
            let t1 = state.full_get_segment_t1(i).map_err(err)?;
            out.push(RawSegment {
                start_ms: t0.max(0) as u64 * 10,
                end_ms: t1.max(0) as u64 * 10,
                text: text.trim().to_string(),
            });
        }
        Ok(out)
    }
}
