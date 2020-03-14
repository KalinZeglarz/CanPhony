package pl.poznan.put

import pl.poznan.put.audio.AudioStream
import pl.poznan.put.audio.Speakers
import pl.poznan.put.streaming.UdpAudioReceiver

class MainPlayer {

    static void main(String[] args) {
        final int sourcePort = System.getProperty('voip.source_port').toInteger()
        final int destinationPort = System.getProperty('voip.destination_port').toInteger()

        final AudioStream audioStream = new AudioStream()
        final Speakers speakers = new Speakers(audioStream: audioStream)
        final UdpAudioReceiver audioReceiver = new UdpAudioReceiver(
                sourcePort: sourcePort,
                destinationPort: destinationPort,
                sleepTime: speakers.audioQuality.sampleRate / audioStream.bufferSize,
                audioStream: audioStream
        )

        audioReceiver.start()
        speakers.start()

        sleep(20000)
        speakers.stop()
        audioReceiver.stop()
        println('the end')
    }

}
