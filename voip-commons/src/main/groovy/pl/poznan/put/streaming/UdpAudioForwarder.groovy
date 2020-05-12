package pl.poznan.put.streaming

import groovy.util.logging.Slf4j
import pl.poznan.put.audio.AudioQuality
import pl.poznan.put.audio.AudioBuffer

@Slf4j
class UdpAudioForwarder {

    String streamerAddress
    int streamerPort
    String receiverAddress
    int receiverPort
    int forwarderPort
    int bufferSize
    AudioQuality audioQuality = AudioQuality.LOW_MONO
    AudioBuffer audioBuffer
    UdpAudioReceiver receiver
    UdpAudioStreamer streamer

    void start() {
        audioBuffer = new AudioBuffer(bufferSize)
        receiver = new UdpAudioReceiver(
                localAddress: streamerAddress,
                streamerPort: streamerPort,
                receiverPort: forwarderPort,
                sleepTime: audioQuality.sampleRate / audioBuffer.size,
                audioBuffer: audioBuffer
        )
        streamer = new UdpAudioStreamer(
                remoteAddress: receiverAddress,
                streamerPort: forwarderPort,
                receiverPort: receiverPort,
                sleepTime: audioQuality.sampleRate / audioBuffer.size,
                audioBuffer: audioBuffer
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
