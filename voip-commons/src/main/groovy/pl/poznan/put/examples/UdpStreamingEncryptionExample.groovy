package pl.poznan.put.examples

import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import pl.poznan.put.GlobalConstants
import pl.poznan.put.audio.AudioBuffer
import pl.poznan.put.audio.Microphone
import pl.poznan.put.audio.Speakers
import pl.poznan.put.security.EncryptionSuite
import pl.poznan.put.streaming.UdpAudioReceiver
import pl.poznan.put.streaming.UdpAudioStreamer

import static pl.poznan.put.GlobalConstants.RECEIVER_PORT
import static pl.poznan.put.GlobalConstants.STREAMER_PORT

@Slf4j
@PackageScope
class UdpStreamingEncryptionExample {

    @SuppressWarnings("DuplicatedCode")
    static void main(String[] args) {
        EncryptionSuite suite1 = new EncryptionSuite()
        EncryptionSuite suite2 = new EncryptionSuite()
        suite1.generateKeys()
        suite2.generateKeys()
        String publicKey1 = suite1.serializePublicKey()
        String publicKey2 = suite2.serializePublicKey()
        suite1.generateCommonSecretKey(publicKey2)
        suite2.generateCommonSecretKey(publicKey1)

        final AudioBuffer audioBuffer1 = new AudioBuffer(GlobalConstants.AUDIO_BUFFER_SIZE)
        final Microphone microphone = new Microphone(audioBuffer1)
        final UdpAudioStreamer audioStreamer = new UdpAudioStreamer(
                remoteAddress: "127.0.0.1",
                streamerPort: STREAMER_PORT,
                receiverPort: RECEIVER_PORT,
                sleepTime: microphone.audioQuality.sampleRate / audioBuffer1.size,
                audioBuffer: audioBuffer1,
                encryptionSuite: suite1
        )

        final AudioBuffer audioBuffer2 = new AudioBuffer(GlobalConstants.AUDIO_BUFFER_SIZE)
        final Speakers speakers = new Speakers(audioBuffer2)
        final UdpAudioReceiver audioReceiver = new UdpAudioReceiver(
                streamerPort: STREAMER_PORT,
                receiverPort: RECEIVER_PORT,
                sleepTime: speakers.audioQuality.sampleRate / audioBuffer2.size,
                audioBuffer: audioBuffer2,
                encryptionSuite: suite2
        )

        audioReceiver.start()
        log.info("started audio receiver")
        speakers.start()
        log.info("started speakers")
        audioStreamer.start()
        log.info("started audio streamer")
        microphone.start()
        log.info("started microphone")

        log.info("enter anything to stop ")
        System.in.newReader().readLine()

        microphone.stop()
        audioStreamer.stop()
        audioReceiver.stop()
        speakers.stop()
    }

}
