package pl.poznan.put

import pl.poznan.put.audio.AudioStream
import pl.poznan.put.audio.Microphone
import pl.poznan.put.streaming.UdpAudioStreamer

class MainStreamer {

    static void main(String[] args) {
        final int sourcePort = System.getProperty('voip.source_port').toInteger()
        final int destinationPort = System.getProperty('voip.destination_port').toInteger()

        final AudioStream audioStream = new AudioStream()
        final Microphone microphone = new Microphone(audioStream: audioStream)
        final UdpAudioStreamer audioStreamer = new UdpAudioStreamer(
                sourcePort: sourcePort,
                destinationPort: destinationPort,
                sleepTime: microphone.audioQuality.sampleRate / audioStream.bufferSize,
                audioStream: audioStream
        )

        audioStreamer.start()
        microphone.start()

        sleep(20000)
        microphone.stop()
        audioStreamer.stop()
        println('the end')
    }

}
