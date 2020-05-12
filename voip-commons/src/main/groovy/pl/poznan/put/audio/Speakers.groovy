package pl.poznan.put.audio

import groovy.util.logging.Slf4j

import javax.sound.sampled.*

@Slf4j
class Speakers {

    AudioQuality audioQuality = AudioQuality.LOW_MONO
    AudioBuffer audioBuffer

    private boolean playing = false
    private Thread playThread
    private SourceDataLine line

    private Speakers() {}

    Speakers(AudioBuffer audioBuffer) {
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
        if (playing) {
            return false
        }

        if (!isLineSupported()) {
            log.error("Line not supported")
            return false
        }

        playThread = new Thread({
            while (playing) {
                try {
                    byte[] buffer = audioBuffer.read()
                    if (buffer == null) {
                        break
                    }
                    line.write(buffer, 0, buffer.length)
                } catch (InterruptedIOException ignored) {
                }
            }
        })

        line = AudioSystem.getSourceDataLine(audioQuality.format)
        line.addLineListener(new LineListener() {
            @Override
            void update(LineEvent event) {
                if (event.type == LineEvent.Type.OPEN && !playing) {
                    playing = true
                    playThread.start()

                } else if (event.type == LineEvent.Type.CLOSE) {
                    playing = false
                    playThread.interrupt()
                }
            }
        })

        line.open(audioQuality.format, audioBuffer.size)
        line.start()
        return true
    }

    void stop() {
        line.stop()
        line.close()
    }

}
