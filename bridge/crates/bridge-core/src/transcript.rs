use serde::Serialize;

use crate::engine::Segment;

#[derive(Debug, Clone, uniffi::Record)]
pub struct Transcript {
    pub segments: Vec<Segment>,
}

#[derive(Serialize)]
struct JsonSegment {
    speaker_id: String,
    start_ms: u64,
    end_ms: u64,
    text: String,
}

#[derive(Serialize)]
struct JsonTranscript {
    transcript: Vec<JsonSegment>,
}

/// Serializes a transcript as `{"transcript": [{"speaker_id", "start_ms", "end_ms", "text"}]}`.
#[uniffi::export]
pub fn transcript_to_json(transcript: Transcript) -> String {
    let body = JsonTranscript {
        transcript: transcript
            .segments
            .into_iter()
            .map(|s| JsonSegment {
                speaker_id: format!("speaker_{}", s.speaker + 1),
                start_ms: s.start_ms,
                end_ms: s.end_ms,
                text: s.text,
            })
            .collect(),
    };
    serde_json::to_string_pretty(&body).unwrap_or_default()
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn json_uses_one_based_speaker_ids() {
        let transcript = Transcript {
            segments: vec![Segment {
                start_ms: 1250,
                end_ms: 4380,
                speaker: 0,
                text: "Добрый день!".into(),
                words: vec![],
            }],
        };
        let json: serde_json::Value = serde_json::from_str(&transcript_to_json(transcript)).unwrap();
        let first = &json["transcript"][0];
        assert_eq!(first["speaker_id"], "speaker_1");
        assert_eq!(first["start_ms"], 1250);
        assert_eq!(first["end_ms"], 4380);
        assert_eq!(first["text"], "Добрый день!");
    }
}
