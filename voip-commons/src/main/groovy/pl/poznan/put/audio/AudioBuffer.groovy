package pl.poznan.put.audio

import pl.poznan.put.GlobalConstants

class AudioBuffer {

    int size = GlobalConstants.AUDIO_BUFFER_SIZE
    PipedInputStream input = new PipedInputStream()
    PipedOutputStream output = new PipedOutputStream(input)

    AudioBuffer() {}

    AudioBuffer(int size) {
        this.size = size
    }

    void write(byte[] data) {
        try {
            output.write(data)
            output.flush()
        } catch (IOException ignored) {
        }
    }

    byte[] read() {
        try {
            return input.readNBytes(size)
        } catch (IOException ignored) {
        }
        return null
    }

}
