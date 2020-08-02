package pl.poznan.put.audio

import groovy.util.logging.Slf4j

import javax.sound.sampled.*

@Slf4j
class Microphone {

    AudioQuality audioQuality = AudioQuality.LOW_MONO
    AudioBuffer audioBuffer

    private boolean capturing = false
    private Thread capturingThread
    private TargetDataLine line

    private Microphone() {}

    Microphone(AudioBuffer audioBuffer) {
        this.audioBuffer = audioBuffer
    }

    boolean isLineSupported() {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioQuality.format)
        if (!AudioSystem.isLineSupported(info)) {
            return false
        }
        return true
    }

    boolean start() {
        if (capturing) {
            return false
        }

        if (!isLineSupported()) {
            log.error("Line not supported")
            return false
        }

        capturingThread = new Thread({
            byte[] buffer = new byte[audioBuffer.size]
            while (capturing) {
                line.read(buffer, 0, buffer.length)
                audioBuffer.write(buffer)
                sleep((audioQuality.getSampleRate() / audioBuffer.size).toInteger())
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

        line.open(audioQuality.format, audioBuffer.size)
        line.start()
        return true
    }

    void stop() {
        if (line != null) {
            line.stop()
            line.close()
        }
    }

}
