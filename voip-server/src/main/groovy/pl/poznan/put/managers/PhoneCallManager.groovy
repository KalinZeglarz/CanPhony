package pl.poznan.put.managers

import groovy.util.logging.Slf4j
import groovyjarjarantlr4.v4.runtime.misc.Tuple2
import pl.poznan.put.GlobalConstants
import pl.poznan.put.streaming.UdpAudioForwarder
import pl.poznan.put.structures.PhoneCallParams
import pl.poznan.put.structures.PhoneCallResponse

@Slf4j
class PhoneCallManager {

    private static Map<Integer, Tuple2<UdpAudioForwarder, UdpAudioForwarder>> phoneCallForwarders = new HashMap<>()
    private static Map<String, Integer> userSessionIds = new HashMap<>()
    private static Map<Integer, Tuple2<String, String>> sessionIdUsers = new HashMap<>()
    private static int forwarderPort = GlobalConstants.FORWARDER_MIN_PORT
    private static final int receiverPort = GlobalConstants.RECEIVER_PORT
    private static final int streamerPort = GlobalConstants.STREAMER_PORT

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

        audioForwarder1.start()
        audioForwarder2.start()
        final PhoneCallResponse response1 = new PhoneCallResponse(sourceUsername: params.sourceUsername,
                targetUsername: params.targetUsername, forwarderPort: forwarderPort1)
        final PhoneCallResponse response2 = new PhoneCallResponse(sourceUsername: params.sourceUsername,
                targetUsername: params.targetUsername, forwarderPort: forwarderPort2)

        userSessionIds.put(params.sourceUsername, params.sessionId)
        userSessionIds.put(params.targetUsername, params.sessionId)
        sessionIdUsers.put(params.sessionId, new Tuple2(params.sourceUsername, params.targetUsername))
        return new Tuple2<>(response1, response2)
    }

    static void removePhoneCall(String userName) {
        int sessionsId = userSessionIds.get(userName)
        phoneCallForwarders.get(sessionsId).item1.stop()
        phoneCallForwarders.get(sessionsId).item2.stop()
        Tuple2<String, String> userNames = sessionIdUsers.get(sessionsId)
        sessionIdUsers.remove(userNames.item1)
        sessionIdUsers.remove(userNames.item2)
        phoneCallForwarders.remove(sessionsId)
    }

    private static int getForwarderPort() {
        if (forwarderPort > GlobalConstants.FORWARDER_MAX_PORT) {
            forwarderPort = GlobalConstants.FORWARDER_MIN_PORT
        }
        return forwarderPort++
    }

}
