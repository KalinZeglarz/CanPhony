package pl.poznan.put.streaming

import groovy.util.logging.Log
import pl.poznan.put.audio.AudioQuality
import pl.poznan.put.audio.AudioBuffer

@Log
class UdpAudioForwarder {

    int streamerPort
    int receiverPort
    int forwarderPort
    int bufferSize
    AudioBuffer audioBuffer
    UdpAudioReceiver receiver
    UdpAudioStreamer streamer

    void start() {
        audioBuffer = new AudioBuffer(bufferSize)
        receiver = new UdpAudioReceiver(
                streamerPort: streamerPort,
                receiverPort: forwarderPort,
                sleepTime: AudioQuality.LOW_MONO.sampleRate / audioBuffer.size,
                audioBuffer: audioBuffer
        )
        streamer = new UdpAudioStreamer(
                streamerPort: forwarderPort,
                receiverPort: receiverPort,
                sleepTime: AudioQuality.LOW_MONO.sampleRate / audioBuffer.size,
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
