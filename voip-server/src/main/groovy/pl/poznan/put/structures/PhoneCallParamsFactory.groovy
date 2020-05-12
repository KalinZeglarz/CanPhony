package pl.poznan.put.structures

import pl.poznan.put.GlobalConstants

class PhoneCallParamsFactory {

    private static int currentSessionId = 1

    private PhoneCallParamsFactory() {}

    static PhoneCallParams createPhoneCallParams(String clientAddress1, String clientAddress2) {
        PhoneCallParams result = new PhoneCallParams()
        result.clientAddress1 = clientAddress1
        result.clientAddress2 = clientAddress2
        result.sessionId = currentSessionId

        currentSessionId += 1
        if (currentSessionId > GlobalConstants.SESSION_ID_MAX) {
            currentSessionId = 1
        }

        return result
    }

}
