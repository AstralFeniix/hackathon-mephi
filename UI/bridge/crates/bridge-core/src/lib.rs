uniffi::setup_scaffolding!();

mod asr;
mod audio_file;
mod diarization;
mod engine;
mod error;
mod session;
mod transcript;

pub use audio_file::decode_to_pcm;
pub use diarization::{label_speakers, SpeakerTurn};
pub use engine::{Engine, Segment};
pub use error::BridgeError;
pub use session::{LiveUpdate, Session};
pub use transcript::{transcript_to_json, Transcript};

pub const SAMPLE_RATE: u32 = 16_000;

pub(crate) fn check_sample_rate(sample_rate: u32) -> Result<(), BridgeError> {
    if sample_rate != SAMPLE_RATE {
        return Err(BridgeError::InvalidInput(format!(
            "expected {SAMPLE_RATE} Hz mono audio, got {sample_rate} Hz"
        )));
    }
    Ok(())
}
