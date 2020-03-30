package pl.poznan.put.examples

import groovy.transform.PackageScope
import groovy.util.logging.Log
import pl.poznan.put.audio.AudioBuffer
import pl.poznan.put.audio.Microphone
import pl.poznan.put.audio.Speakers
import pl.poznan.put.streaming.UdpAudioReceiver
import pl.poznan.put.streaming.UdpAudioStreamer

@Log
@PackageScope
class UdpStreamingExample {

    @SuppressWarnings("DuplicatedCode")
    static void main(String[] args) {
        final int streamerPort = 50000
        final int receiverPort = 50001

        final AudioBuffer audioBuffer1 = new AudioBuffer(4096)
        final Microphone microphone = new Microphone(audioBuffer1)
        final UdpAudioStreamer audioStreamer = new UdpAudioStreamer(
                streamerPort: streamerPort,
                receiverPort: receiverPort,
                sleepTime: microphone.audioQuality.sampleRate / audioBuffer1.size,
                audioBuffer: audioBuffer1
        )

        final AudioBuffer audioBuffer2 = new AudioBuffer(4096)
        final Speakers speakers = new Speakers(audioBuffer2)
        final UdpAudioReceiver audioReceiver = new UdpAudioReceiver(
                streamerPort: streamerPort,
                receiverPort: receiverPort,
                sleepTime: speakers.audioQuality.sampleRate / audioBuffer2.size,
                audioBuffer: audioBuffer2
        )

        audioReceiver.start()
        speakers.start()
        audioStreamer.start()
        microphone.start()

        log.info("enter anything to stop ")
        System.in.newReader().readLine()

        microphone.stop()
        audioStreamer.stop()
        audioReceiver.stop()
        speakers.stop()
    }

}
