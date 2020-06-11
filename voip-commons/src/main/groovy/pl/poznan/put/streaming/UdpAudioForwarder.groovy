package pl.poznan.put.streaming

import groovy.util.logging.Slf4j
import pl.poznan.put.audio.AudioBuffer
import pl.poznan.put.audio.AudioQuality
import pl.poznan.put.security.EncryptionSuite

@Slf4j
class UdpAudioForwarder {

    String sourceAddress
    int sourcePort
    String targetAddress
    int targetPort
    int forwarderPort
    int bufferSize
    AudioQuality audioQuality = AudioQuality.LOW_MONO
    AudioBuffer audioBuffer
    UdpAudioReceiver receiver
    UdpAudioStreamer streamer
    EncryptionSuite sourceEncryptionSuite
    EncryptionSuite targetEncryptionSuite

    void start() {
        audioBuffer = new AudioBuffer(bufferSize)
        receiver = new UdpAudioReceiver(
                localAddress: sourceAddress,
                streamerPort: sourcePort,
                receiverPort: forwarderPort,
                sleepTime: audioQuality.sampleRate / audioBuffer.size,
                audioBuffer: audioBuffer,
                encryptionSuite: sourceEncryptionSuite
        )
        streamer = new UdpAudioStreamer(
                remoteAddress: targetAddress,
                streamerPort: forwarderPort,
                receiverPort: targetPort,
                sleepTime: audioQuality.sampleRate / audioBuffer.size,
                audioBuffer: audioBuffer,
                encryptionSuite: targetEncryptionSuite
        )
        receiver.start()
        streamer.start()
    }

    void stop() {
        receiver.stop()
        log.info("stopped receiver")
        streamer.stop()
        log.info("stopped streamer")
    }

}
