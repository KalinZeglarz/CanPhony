package pl.poznan.put.structures

import pl.poznan.put.GlobalConstants
import pl.poznan.put.managers.PubSubManager

class PhoneCallParamsFactory {

    private static int currentSessionId = 1

    private PhoneCallParamsFactory() {}

    static PhoneCallParams createPhoneCallParams(PhoneCallRequest request) {
        PhoneCallParams result = new PhoneCallParams()
        result.sourceUsername = request.sourceUsername
        result.targetUsername = request.targetUsername
        result.sourceEncryptionSuite = PubSubManager.redisClient.getEncryptionSuite(request.sourceUsername)
        result.targetEncryptionSuite = PubSubManager.redisClient.getEncryptionSuite(request.targetUsername)
        result.sessionId = currentSessionId

        currentSessionId += 1
        if (currentSessionId > GlobalConstants.SESSION_ID_MAX) {
            currentSessionId = 1
        }

        return result
    }

}
