package pl.poznan.put.examples

import groovy.transform.PackageScope
import groovy.util.logging.Log
import pl.poznan.put.subpub.ChannelManager
import pl.poznan.put.VoipHttpClient
import pl.poznan.put.audio.AudioBuffer
import pl.poznan.put.audio.Microphone
import pl.poznan.put.audio.Speakers
import pl.poznan.put.subpub.Message
import pl.poznan.put.streaming.UdpAudioReceiver
import pl.poznan.put.streaming.UdpAudioStreamer

@Log
@PackageScope
class PhoneCallExample {

    @SuppressWarnings("DuplicatedCode")
    static void main(String[] args) {
        VoipHttpClient voipHttpClient = new VoipHttpClient()
        ChannelManager channelManager = new ChannelManager("127.0.0.1", 6381)

        Message message = voipHttpClient.startCall()
        String channelName = message.getContent().getString('channelName')
        channelManager.subscribeChannel(channelName)

        final int streamerPort = message.getContent().getInt('streamerPort')
        final int receiverPort = message.getContent().getInt('receiverPort')
        final int forwarderPort = message.getContent().getInt('forwarderPort')

        final AudioBuffer audioBuffer1 = new AudioBuffer(4096)
        final Microphone microphone = new Microphone(audioBuffer1)
        final UdpAudioStreamer audioStreamer = new UdpAudioStreamer(
                streamerPort: streamerPort,
                receiverPort: forwarderPort,
                sleepTime: microphone.audioQuality.sampleRate / audioBuffer1.size,
                audioBuffer: audioBuffer1
        )

        final AudioBuffer audioBuffer2 = new AudioBuffer(4096)
        final Speakers speakers = new Speakers(audioBuffer2)
        final UdpAudioReceiver audioReceiver = new UdpAudioReceiver(
                streamerPort: forwarderPort,
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

        voipHttpClient.endCall()
        log.info('call end')

        microphone.stop()
        audioStreamer.stop()

        audioReceiver.stop()
        speakers.stop()

        channelManager.closeChannel(channelName)
    }

}
