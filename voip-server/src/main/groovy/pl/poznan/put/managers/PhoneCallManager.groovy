package pl.poznan.put.managers

import groovy.util.logging.Slf4j
import groovyjarjarantlr4.v4.runtime.misc.Tuple2
import pl.poznan.put.GlobalConstants
import pl.poznan.put.controllers.SubPubManager
import pl.poznan.put.streaming.UdpAudioForwarder
import pl.poznan.put.structures.PhoneCallParams
import pl.poznan.put.structures.PhoneCallResponse
import pl.poznan.put.subpub.Message
import pl.poznan.put.subpub.MessageAction

@Slf4j
class PhoneCallManager {

    static Map<Integer, Tuple2<UdpAudioForwarder, UdpAudioForwarder>> phoneCallForwarders = new HashMap<>()
    private static int forwarderPort = GlobalConstants.FORWARDER_MIN_PORT
    static final int receiverPort = GlobalConstants.RECEIVER_PORT
    static final int streamerPort = GlobalConstants.STREAMER_PORT

    private PhoneCallManager() {}

    static Tuple2<PhoneCallResponse, PhoneCallResponse> addPhoneCall(PhoneCallParams params) {
        String sourceUserAddress = DatabaseManager.getUserAddress(params.sourceUsername)
        String targetUserAddress = DatabaseManager.getUserAddress(params.targetUsername)

        final int forwarderPort1 = getForwarderPort()
        final int forwarderPort2 = getForwarderPort()
        UdpAudioForwarder audioForwarder1 = new UdpAudioForwarder(streamerAddress: sourceUserAddress,
                streamerPort: streamerPort, receiverAddress: targetUserAddress, receiverPort: receiverPort,
                forwarderPort: forwarderPort1, audioQuality: params.audioQuality, bufferSize: params.bufferSize)
        UdpAudioForwarder audioForwarder2 = new UdpAudioForwarder(streamerAddress: targetUserAddress,
                streamerPort: streamerPort, receiverAddress: sourceUserAddress, receiverPort: receiverPort,
                forwarderPort: forwarderPort2, audioQuality: params.audioQuality, bufferSize: params.bufferSize)
        phoneCallForwarders.put(params.sessionId, new Tuple2(audioForwarder1, audioForwarder2))

        SubPubManager.redisClient.subscribeChannel(params.sessionId.toString()) { String channelName,
                                                                                  String messageString ->
            Message message = Message.parseJSON(messageString)
            if (message.action == MessageAction.END_CALL) {
                log.info("[${channelName}] received end call")
                audioForwarder1.stop()
                audioForwarder2.stop()
                phoneCallForwarders.remove(channelName)
                SubPubManager.redisClient.unsubscribe(channelName)
            }
        }
        audioForwarder1.start()
        audioForwarder2.start()
        final PhoneCallResponse response1 = new PhoneCallResponse(sourceUsername: params.sourceUsername,
                targetUsername: params.targetUsername, sessionId: params.sessionId, forwarderPort: forwarderPort1)
        final PhoneCallResponse response2 = new PhoneCallResponse(sourceUsername: params.sourceUsername,
                targetUsername: params.targetUsername, sessionId: params.sessionId, forwarderPort: forwarderPort2)
        return new Tuple2<>(response1, response2)
    }

    static int getForwarderPort() {
        if (forwarderPort > GlobalConstants.FORWARDER_MAX_PORT) {
            forwarderPort = GlobalConstants.FORWARDER_MIN_PORT
        }
        return forwarderPort++
    }

}
