package pl.poznan.put.audio

import javax.sound.sampled.*

class Speakers {

    AudioQuality audioQuality = AudioQuality.LOW_MONO
    AudioStream audioStream = new AudioStream()

    private boolean playing = false
    private Thread playThread
    private SourceDataLine line

    boolean isLineSupported() {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioQuality.format)
        if (!AudioSystem.isLineSupported(info)) {
            return false
        }
        return true
    }

    void start() {
        if (playing) {
            return
        }

        if (!isLineSupported()) {
            println("Line not supported")
            System.exit(1)
        }

        playThread = new Thread({
            while (playing) {
                byte[] buffer = audioStream.read()
                line.write(buffer, 0, buffer.length)
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

        line.open(audioQuality.format)
        line.start()
    }

    void stop() {
        line.stop()
        line.close()
    }

}
