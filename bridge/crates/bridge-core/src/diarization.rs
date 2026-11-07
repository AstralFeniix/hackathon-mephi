use crate::engine::Segment;
use crate::transcript::Transcript;
use crate::{BridgeError, SAMPLE_RATE};

/// Turns from the same speaker separated by at most this gap are joined into one turn.
const MERGE_GAP_MS: u64 = 500;
/// Turns shorter than this are folded into the turn before them, so single-word blips
/// do not create speaker changes.
const MIN_TURN_MS: u64 = 300;
/// A speaker change inside a segment is only accepted after a pause of this length.
/// Changes without a pause are usually diarization jitter inside one sentence.
const MIN_PAUSE_MS: u64 = 200;

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

/// Cleans up raw turns: joins close turns of the same speaker, and folds very short turns
/// into the turn before them when they are close enough to be a blip inside it.
pub(crate) fn smooth_turns(turns: &[SpeakerTurn]) -> Vec<SpeakerTurn> {
    let mut sorted = turns.to_vec();
    sorted.sort_by_key(|t| t.start_ms);

    let mut out: Vec<SpeakerTurn> = Vec::with_capacity(sorted.len());
    for turn in sorted {
        let short = turn.end_ms.saturating_sub(turn.start_ms) < MIN_TURN_MS;
        if let Some(last) = out.last_mut() {
            let close = turn.start_ms.saturating_sub(last.end_ms) <= MERGE_GAP_MS;
            if close && (short || last.speaker == turn.speaker) {
                last.end_ms = last.end_ms.max(turn.end_ms);
                continue;
            }
        }
        out.push(turn);
    }
    out
}

/// Splits a segment wherever the speaker changes, using its word timings. Each word goes to
/// the speaker who talks most during it, or to the previous word's speaker if no turn covers it.
/// A segment without word timings, or with a single speaker, is returned as one piece.
fn split_by_speaker(segment: Segment, turns: &[SpeakerTurn]) -> Vec<Segment> {
    if segment.words.is_empty() {
        let speaker = dominant_speaker(turns, segment.start_ms, segment.end_ms);
        return vec![Segment {
            speaker: speaker.unwrap_or(segment.speaker),
            ..segment
        }];
    }

    let mut pieces: Vec<Segment> = Vec::new();
    for word in &segment.words {
        let detected = dominant_speaker(turns, word.start_ms, word.end_ms);
        let start_new = match pieces.last() {
            None => true,
            Some(last) => {
                matches!(detected, Some(s) if s != last.speaker)
                    && word.start_ms.saturating_sub(last.end_ms) >= MIN_PAUSE_MS
            }
        };
        if start_new {
            pieces.push(Segment {
                start_ms: word.start_ms,
                end_ms: word.end_ms,
                speaker: detected.unwrap_or(segment.speaker),
                text: word.text.clone(),
                words: vec![word.clone()],
            });
        } else if let Some(piece) = pieces.last_mut() {
            piece.end_ms = word.end_ms;
            piece.text.push(' ');
            piece.text.push_str(&word.text);
            piece.words.push(word.clone());
        }
    }

    if pieces.len() == 1 {
        return vec![Segment {
            speaker: pieces[0].speaker,
            ..segment
        }];
    }
    pieces
}

/// Labels each segment with the speaker who talks for most of it, and splits segments where
/// the speaker changes. Segments that no turn covers keep their current label.
#[uniffi::export]
pub fn label_speakers(transcript: Transcript, turns: Vec<SpeakerTurn>) -> Transcript {
    let turns = smooth_turns(&turns);
    let segments = transcript
        .segments
        .into_iter()
        .flat_map(|segment| split_by_speaker(segment, &turns))
        .collect();
    Transcript { segments }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::engine::Word;

    fn seg(start_ms: u64, end_ms: u64, text: &str) -> Segment {
        Segment {
            start_ms,
            end_ms,
            speaker: 0,
            text: text.into(),
            words: vec![],
        }
    }

    fn word(start_ms: u64, end_ms: u64, text: &str) -> Word {
        Word {
            start_ms,
            end_ms,
            text: text.into(),
        }
    }

    fn turn(start_ms: u64, end_ms: u64, speaker: u32) -> SpeakerTurn {
        SpeakerTurn {
            start_ms,
            end_ms,
            speaker,
        }
    }

    #[test]
    fn segments_take_the_speaker_who_overlaps_most() {
        let transcript = Transcript {
            segments: vec![seg(0, 2000, "a"), seg(2000, 4000, "b"), seg(9000, 10_000, "c")],
        };
        let turns = vec![turn(0, 2500, 0), turn(2500, 6000, 1)];
        let labeled = label_speakers(transcript, turns);
        assert_eq!(labeled.segments[0].speaker, 0);
        assert_eq!(labeled.segments[1].speaker, 1);
        assert_eq!(labeled.segments[2].speaker, 0, "uncovered segments keep their label");
    }

    #[test]
    fn segment_is_split_at_a_speaker_change() {
        let mut s = seg(0, 6400, "один два три четыре пять шесть");
        s.words = vec![
            word(0, 1000, "один"),
            word(1000, 2000, "два"),
            word(2000, 3000, "три"),
            word(3200, 4200, "четыре"),
            word(4400, 5400, "пять"),
            word(5600, 6400, "шесть"),
        ];
        let turns = vec![turn(0, 3100, 0), turn(3100, 4300, 1), turn(4300, 6400, 0)];
        let labeled = label_speakers(Transcript { segments: vec![s] }, turns);

        assert_eq!(labeled.segments.len(), 3);
        assert_eq!(labeled.segments[0].speaker, 0);
        assert_eq!(labeled.segments[0].text, "один два три");
        assert_eq!(labeled.segments[1].speaker, 1);
        assert_eq!(labeled.segments[1].text, "четыре", "a one-word turn after a pause is kept");
        assert_eq!(labeled.segments[2].speaker, 0);
        assert_eq!(labeled.segments[2].text, "пять шесть");
    }

    #[test]
    fn speaker_change_without_pause_does_not_split() {
        let mut s = seg(0, 6000, "один два три четыре пять шесть");
        s.words = (0..6)
            .map(|i| word(i * 1000, (i + 1) * 1000, ["один", "два", "три", "четыре", "пять", "шесть"][i as usize]))
            .collect();
        let turns = vec![turn(0, 2100, 0), turn(2100, 3900, 1), turn(3900, 6000, 0)];
        let labeled = label_speakers(Transcript { segments: vec![s] }, turns);

        assert_eq!(labeled.segments.len(), 1);
        assert_eq!(labeled.segments[0].speaker, 0);
    }

    #[test]
    fn single_speaker_segment_keeps_its_text() {
        let mut s = seg(0, 2000, "Добрый день!");
        s.words = vec![word(0, 1000, "Добрый"), word(1000, 2000, "день!")];
        let labeled = label_speakers(
            Transcript { segments: vec![s] },
            vec![turn(0, 2000, 1)],
        );
        assert_eq!(labeled.segments.len(), 1);
        assert_eq!(labeled.segments[0].speaker, 1);
        assert_eq!(labeled.segments[0].text, "Добрый день!");
    }

    #[test]
    fn segment_without_words_is_labeled_whole() {
        let labeled = label_speakers(
            Transcript { segments: vec![seg(0, 2000, "a")] },
            vec![turn(0, 1000, 0), turn(1000, 2000, 1)],
        );
        assert_eq!(labeled.segments.len(), 1);
        assert_eq!(labeled.segments[0].speaker, 0, "ties go to the first turn");
    }

    #[test]
    fn close_turns_of_the_same_speaker_merge() {
        let merged = smooth_turns(&[turn(0, 1000, 0), turn(1200, 2000, 0)]);
        assert_eq!(merged.len(), 1);
        assert_eq!((merged[0].start_ms, merged[0].end_ms), (0, 2000));
    }

    #[test]
    fn distant_turns_of_the_same_speaker_stay_apart() {
        let turns = smooth_turns(&[turn(0, 1000, 0), turn(2000, 3000, 0)]);
        assert_eq!(turns.len(), 2);
    }

    #[test]
    fn short_blip_is_absorbed_by_the_surrounding_turn() {
        let turns = smooth_turns(&[turn(0, 2000, 0), turn(2100, 2300, 1), turn(2400, 4000, 0)]);
        assert_eq!(turns.len(), 1);
        assert_eq!((turns[0].start_ms, turns[0].end_ms, turns[0].speaker), (0, 4000, 0));
    }

    #[test]
    fn long_turn_from_another_speaker_is_kept() {
        let turns = smooth_turns(&[turn(0, 2000, 0), turn(2100, 4000, 1)]);
        assert_eq!(turns.len(), 2);
    }
}
