use crate::transcript::Transcript;
use crate::{BridgeError, SAMPLE_RATE};

/// One stretch of speech by one speaker. Speaker ids count from 0.
#[derive(Debug, Clone, Copy, uniffi::Record)]
pub struct SpeakerTurn {
    pub start_ms: u64,
    pub end_ms: u64,
    pub speaker: u32,
}

/// Speaker diarization backend ("who spoke when").
pub trait Diarizer: Send + Sync {
    fn diarize(&self, samples: &[f32]) -> Result<Vec<SpeakerTurn>, BridgeError>;
}

/// Labels the entire clip as speaker 0. Used when no diarization model has run yet.
pub struct SingleSpeaker;

impl Diarizer for SingleSpeaker {
    fn diarize(&self, samples: &[f32]) -> Result<Vec<SpeakerTurn>, BridgeError> {
        let end_ms = samples.len() as u64 * 1000 / SAMPLE_RATE as u64;
        Ok(vec![SpeakerTurn {
            start_ms: 0,
            end_ms,
            speaker: 0,
        }])
    }
}

/// Picks the speaker whose turns overlap the segment the most. `None` if nothing overlaps.
pub(crate) fn dominant_speaker(turns: &[SpeakerTurn], start_ms: u64, end_ms: u64) -> Option<u32> {
    let mut best = (0u64, None);
    for t in turns {
        let overlap = t.end_ms.min(end_ms).saturating_sub(t.start_ms.max(start_ms));
        if overlap > best.0 {
            best = (overlap, Some(t.speaker));
        }
    }
    best.1
}

/// Gives each segment the speaker who talks for most of its time.
/// Segments that no turn covers keep their current label.
#[uniffi::export]
pub fn label_speakers(transcript: Transcript, turns: Vec<SpeakerTurn>) -> Transcript {
    let segments = transcript
        .segments
        .into_iter()
        .map(|mut segment| {
            if let Some(speaker) = dominant_speaker(&turns, segment.start_ms, segment.end_ms) {
                segment.speaker = speaker;
            }
            segment
        })
        .collect();
    Transcript { segments }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::engine::Segment;

    #[test]
    fn segments_take_the_speaker_who_overlaps_most() {
        let transcript = Transcript {
            segments: vec![
                Segment { start_ms: 0, end_ms: 2000, speaker: 0, text: "a".into() },
                Segment { start_ms: 2000, end_ms: 4000, speaker: 0, text: "b".into() },
                Segment { start_ms: 9000, end_ms: 10_000, speaker: 0, text: "c".into() },
            ],
        };
        let turns = vec![
            SpeakerTurn { start_ms: 0, end_ms: 2500, speaker: 0 },
            SpeakerTurn { start_ms: 2500, end_ms: 6000, speaker: 1 },
        ];
        let labeled = label_speakers(transcript, turns);
        assert_eq!(labeled.segments[0].speaker, 0);
        assert_eq!(labeled.segments[1].speaker, 1);
        assert_eq!(labeled.segments[2].speaker, 0, "uncovered segments keep their label");
    }
}
