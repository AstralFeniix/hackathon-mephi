use symphonia::core::audio::SampleBuffer;
use symphonia::core::codecs::{DecoderOptions, CODEC_TYPE_NULL};
use symphonia::core::errors::Error;
use symphonia::core::formats::FormatOptions;
use symphonia::core::io::MediaSourceStream;
use symphonia::core::meta::MetadataOptions;
use symphonia::core::probe::Hint;

use crate::{BridgeError, SAMPLE_RATE};

/// Decodes an mp3 or wav file and returns 16 kHz mono f32 samples.
pub(crate) fn decode_to_16k_mono(path: &str) -> Result<Vec<f32>, BridgeError> {
    let invalid = |msg: String| BridgeError::InvalidInput(msg);

    let file = std::fs::File::open(path).map_err(|e| invalid(format!("cannot open {path}: {e}")))?;
    let stream = MediaSourceStream::new(Box::new(file), Default::default());

    let mut hint = Hint::new();
    if let Some(ext) = std::path::Path::new(path).extension().and_then(|e| e.to_str()) {
        hint.with_extension(ext);
    }
    let mut format = symphonia::default::get_probe()
        .format(&hint, stream, &FormatOptions::default(), &MetadataOptions::default())
        .map_err(|e| invalid(format!("unsupported audio file: {e}")))?
        .format;

    let track = format
        .tracks()
        .iter()
        .find(|t| t.codec_params.codec != CODEC_TYPE_NULL)
        .ok_or_else(|| invalid("no audio track found".into()))?;
    let track_id = track.id;
    let source_rate = track
        .codec_params
        .sample_rate
        .ok_or_else(|| invalid("unknown sample rate".into()))?;
    let mut decoder = symphonia::default::get_codecs()
        .make(&track.codec_params, &DecoderOptions::default())
        .map_err(|e| invalid(format!("unsupported codec: {e}")))?;

    let mut mono = Vec::new();
    loop {
        let packet = match format.next_packet() {
            Ok(packet) => packet,
            Err(Error::IoError(e)) if e.kind() == std::io::ErrorKind::UnexpectedEof => break,
            Err(e) => return Err(BridgeError::Inference(format!("read failed: {e}"))),
        };
        if packet.track_id() != track_id {
            continue;
        }

        let decoded = match decoder.decode(&packet) {
            Ok(decoded) => decoded,
            Err(Error::DecodeError(_)) => continue, // skip corrupt frames
            Err(e) => return Err(BridgeError::Inference(format!("decode failed: {e}"))),
        };

        let spec = *decoded.spec();
        let channels = spec.channels.count().max(1);
        let mut buf = SampleBuffer::<f32>::new(decoded.capacity() as u64, spec);
        buf.copy_interleaved_ref(decoded);
        mono.extend(
            buf.samples()
                .chunks(channels)
                .map(|frame| frame.iter().sum::<f32>() / channels as f32),
        );
    }

    Ok(resample_box(&mono, source_rate, SAMPLE_RATE))
}

/// Decodes an mp3 or wav file to a raw file of 16 kHz mono little-endian f32 samples.
/// Returns the number of samples written.
#[uniffi::export]
pub fn decode_to_pcm(input_path: String, output_path: String) -> Result<u64, BridgeError> {
    let samples = decode_to_16k_mono(&input_path)?;
    let mut bytes = Vec::with_capacity(samples.len() * 4);
    for sample in &samples {
        bytes.extend_from_slice(&sample.to_le_bytes());
    }
    std::fs::write(&output_path, &bytes)
        .map_err(|e| BridgeError::InvalidInput(format!("cannot write {output_path}: {e}")))?;
    Ok(samples.len() as u64)
}

/// Resamples by averaging the input samples that fall into each output sample.
/// Averaging acts as a simple anti-aliasing filter when downsampling (e.g. 44.1 kHz -> 16 kHz).
fn resample_box(input: &[f32], from: u32, to: u32) -> Vec<f32> {
    if from == to {
        return input.to_vec();
    }
    let ratio = from as f64 / to as f64;
    let out_len = (input.len() as f64 / ratio) as usize;
    (0..out_len)
        .map(|i| {
            let start = (i as f64 * ratio) as usize;
            let end = (((i + 1) as f64 * ratio) as usize).clamp(start + 1, input.len());
            input[start..end].iter().sum::<f32>() / (end - start) as f32
        })
        .collect()
}
