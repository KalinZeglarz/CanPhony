package pl.poznan.put.examples

import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import pl.poznan.put.GlobalConstants
import pl.poznan.put.audio.AudioBuffer
import pl.poznan.put.audio.Microphone
import pl.poznan.put.audio.Speakers
import pl.poznan.put.streaming.UdpAudioForwarder
import pl.poznan.put.streaming.UdpAudioReceiver
import pl.poznan.put.streaming.UdpAudioStreamer

@Slf4j
@PackageScope
class UdpAudioForwarderExample {

    @SuppressWarnings("DuplicatedCode")
    static void main(String[] args) {
        final int forwarderPort = 50001

        final AudioBuffer audioBuffer1 = new AudioBuffer()
        final Microphone microphone = new Microphone(audioBuffer1)
        final UdpAudioStreamer audioStreamer = new UdpAudioStreamer(
                remoteAddress: "127.0.0.1",
                streamerPort: GlobalConstants.STREAMER_PORT,
                receiverPort: forwarderPort,
                sleepTime: microphone.audioQuality.sampleRate / audioBuffer1.size,
                audioBuffer: audioBuffer1,
        )

        final UdpAudioForwarder forwarder = new UdpAudioForwarder(
                sourceAddress: "127.0.0.1",
                targetAddress: "127.0.0.1",
                sourcePort: GlobalConstants.STREAMER_PORT,
                targetPort: GlobalConstants.RECEIVER_PORT,
                forwarderPort: forwarderPort,
                bufferSize: GlobalConstants.AUDIO_BUFFER_SIZE
        )

        final AudioBuffer audioBuffer2 = new AudioBuffer(GlobalConstants.AUDIO_BUFFER_SIZE)
        final Speakers speakers = new Speakers(audioBuffer2)
        final UdpAudioReceiver audioReceiver = new UdpAudioReceiver(
                localAddress: "127.0.0.1",
                streamerPort: forwarderPort,
                receiverPort: GlobalConstants.RECEIVER_PORT,
                sleepTime: speakers.audioQuality.sampleRate / audioBuffer2.size,
                audioBuffer: audioBuffer2
        )

        forwarder.start()
        audioReceiver.start()
        speakers.start()
        audioStreamer.start()
        microphone.start()

        log.info("enter anything to stop ")
        System.in.newReader().readLine()

        log.info("stopping example")
        forwarder.stop()
        log.info("forwarder stopped")
        microphone.stop()
        log.info("microphone stopped")
        audioStreamer.stop()
        log.info("streamer stopped")
        audioReceiver.stop()
        log.info("receiver stopped")
        speakers.stop()
        log.info("speaker stopped")
    }

}
