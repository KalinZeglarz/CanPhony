package pl.poznan.put.audio

import org.json.JSONObject

import javax.sound.sampled.AudioFormat

enum AudioQuality {

    VERY_LOW_MONO(16000, 8, 1),
    LOW_MONO(16000, 16, 1),
    MEDIUM_MONO(22050, 16, 1),
    HIGH_MONO(44100, 16, 1),
    VERY_LOW_STEREO(16000, 8, 2),
    LOW_STEREO(16000, 16, 2),
    MEDIUM_STEREO(22050, 16, 2),
    HIGH_STEREO(44100, 16, 2)

    private float sampleRate
    private int sampleSizeInBits
    private int channels
    private AudioFormat format

    AudioQuality(float sampleRate, int sampleSizeInBits, int channels) {
        this.sampleRate = sampleRate
        this.sampleSizeInBits = sampleSizeInBits
        this.channels = channels

        boolean signed = false
        final boolean bigEndian = true
        if (sampleSizeInBits > 8) {
            signed = true
        }
        this.format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian)
    }

    float getSampleRate() {
        return sampleRate
    }

    AudioFormat getFormat() {
        return format
    }

    String toJson() {
        JSONObject json = new JSONObject()
        json.put('sampleRate', sampleRate)
        json.put('sampleSize', sampleSizeInBits)
        json.put('channels', channels)
        return json.toString()
    }

}