package pl.poznan.put.audio

import org.json.JSONObject
import pl.poznan.put.structures.JSONable

import javax.sound.sampled.AudioFormat

enum AudioQuality implements JSONable {
    NONE(0, 0, 0),
    VERY_LOW_MONO(8000, 16, 1),
    LOW_MONO(16000, 16, 1),
    MEDIUM_MONO(22050, 16, 1),
    HIGH_MONO(44100, 16, 1),
    VERY_LOW_STEREO(8000, 16, 2),
    LOW_STEREO(16000, 16, 2),
    MEDIUM_STEREO(22050, 16, 2),
    HIGH_STEREO(44100, 16, 2)

    float sampleRate
    int sampleSize
    int channels
    AudioFormat format

    AudioQuality(float sampleRate, int sampleSize, int channels) {
        this.sampleRate = sampleRate
        this.sampleSize = sampleSize
        this.channels = channels

        boolean signed = false
        final boolean bigEndian = true
        if (sampleSize > 8) {
            signed = true
        }
        this.format = new AudioFormat(sampleRate, sampleSize, channels, signed, bigEndian)
    }

    float getSampleRate() {
        return sampleRate
    }

    AudioFormat getFormat() {
        return format
    }

    JSONObject toJSON() {
        JSONObject json = new JSONObject()
        json.put("sampleRate", sampleRate)
        json.put("sampleSize", sampleSize)
        json.put("channels", channels)
        return json
    }

    static AudioQuality match(float sampleRate, int sampleSize, int channels) {
        for (AudioQuality audioQuality in values()) {
            if (audioQuality.sampleRate == sampleRate && audioQuality.sampleSize == sampleSize
                    && audioQuality.channels == channels) {
                return audioQuality
            }
        }
        return NONE
    }

    static AudioQuality parseJSON(final String text) {
        JSONObject parsedJson = new JSONObject(text)
        float sampleRate = parsedJson.getFloat("sampleRate")
        int sampleSize = parsedJson.getInt("sampleSize")
        int channels = parsedJson.getInt("channels")
        return match(sampleRate, sampleSize, channels)
    }
}