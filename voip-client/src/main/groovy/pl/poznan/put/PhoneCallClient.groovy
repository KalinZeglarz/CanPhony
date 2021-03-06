package pl.poznan.put

import groovy.util.logging.Slf4j
import pl.poznan.put.audio.AudioBuffer
import pl.poznan.put.audio.Microphone
import pl.poznan.put.audio.Speakers
import pl.poznan.put.security.EncryptionSuite
import pl.poznan.put.streaming.UdpAudioReceiver
import pl.poznan.put.streaming.UdpAudioStreamer

@Slf4j
class PhoneCallClient {

    UdpAudioReceiver audioReceiver
    UdpAudioStreamer audioStreamer
    Speakers speakers
    Microphone microphone


    PhoneCallClient(String serverAddress, int forwarderPort, EncryptionSuite encryptionSuite) {
        AudioBuffer audioBuffer1 = new AudioBuffer(GlobalConstants.AUDIO_BUFFER_SIZE)
        microphone = new Microphone(audioBuffer1)
        audioStreamer = new UdpAudioStreamer(
                remoteAddress: serverAddress,
                streamerPort: GlobalConstants.STREAMER_PORT,
                receiverPort: forwarderPort,
                sleepTime: microphone.audioQuality.sampleRate / audioBuffer1.size,
                audioBuffer: audioBuffer1,
                encryptionSuite: encryptionSuite
        )
        AudioBuffer audioBuffer2 = new AudioBuffer(GlobalConstants.AUDIO_BUFFER_SIZE)
        speakers = new Speakers(audioBuffer2)
        audioReceiver = new UdpAudioReceiver(
                localAddress: serverAddress,
                streamerPort: forwarderPort,
                receiverPort: GlobalConstants.RECEIVER_PORT,
                sleepTime: speakers.audioQuality.sampleRate / audioBuffer2.size,
                audioBuffer: audioBuffer2,
                encryptionSuite: encryptionSuite
        )
    }

    void start() {
        log.info("starting phone call client")
        audioReceiver.start()
        speakers.start()
        audioStreamer.start()
        microphone.start()
        log.info("started phone call client")
    }

    void stop() {
        log.info("stopping phone call client")
        audioReceiver.stop()
        speakers.stop()
        audioStreamer.stop()
        microphone.stop()
        log.info("stopped phone call client")
    }

}
