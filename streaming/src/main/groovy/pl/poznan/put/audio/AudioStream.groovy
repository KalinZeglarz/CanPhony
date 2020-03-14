package pl.poznan.put.audio

class AudioStream {

    int bufferSize = 768
    PipedInputStream input = new PipedInputStream()
    final PipedOutputStream output = new PipedOutputStream(input)

    AudioStream() {}

    void write(byte[] data) {
        output.flush()
        output.write(data)
    }

    byte[] read() {
        return input.readNBytes(bufferSize)
    }

}
