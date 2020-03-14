package pl.poznan.put.audio

import javax.sound.sampled.*

class Microphone {

    AudioQuality audioQuality = AudioQuality.LOW_MONO
    AudioStream audioStream

    private boolean capturing = false
    private Thread capturingThread
    private TargetDataLine line

    boolean isLineSupported() {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioQuality.format)
        if (!AudioSystem.isLineSupported(info)) {
            return false
        }
        return true
    }

    void start() {
        if (capturing) {
            return
        }

        if (!isLineSupported()) {
            println("Line not supported")
            System.exit(1)
        }

        capturingThread = new Thread({
            byte[] buffer = new byte[audioStream.bufferSize]
            while (capturing) {
                line.read(buffer, 0, buffer.length)
                audioStream.write(buffer)
                sleep((audioQuality.getSampleRate() / audioStream.bufferSize).toInteger())
            }
        })

        line = AudioSystem.getTargetDataLine(audioQuality.format)
        line.addLineListener(new LineListener() {
            @Override
            void update(LineEvent event) {
                if (event.type == LineEvent.Type.OPEN && !capturing) {
                    capturing = true
                    capturingThread.start()
                } else if (event.type == LineEvent.Type.CLOSE) {
                    capturing = false
                    capturingThread.join()
                }
            }
        })

        line.open(audioQuality.format)
        line.start()
    }

    void stop() {
        line.stop()
        line.close()
    }

}
