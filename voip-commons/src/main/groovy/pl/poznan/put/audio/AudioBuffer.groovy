package pl.poznan.put.audio

import java.util.concurrent.SynchronousQueue

class AudioBuffer {

    int size = 4096
    PipedInputStream input = new PipedInputStream()
    PipedOutputStream output = new PipedOutputStream(input)

    private AudioBuffer(){}

    AudioBuffer(int size){
        this.size = size
    }

    void write(byte[] data) {
        output.flush()
        output.write(data)
    }

    byte[] read() {
        return input.readNBytes(size)
    }

}
