package pl.poznan.put.structures

import org.json.JSONObject

class PhoneCallResponse extends ApiResponse implements JSONable {
    int sessionId
    int forwarderPort
    String sourceUsername
    String targetUsername

    @Override
    JSONObject toJSON() {
        return new JSONObject()
                .put('sessionId', sessionId)
                .put('forwarderPort', forwarderPort)
                .put('sourceUsername', sourceUsername)
                .put('targetUsername', targetUsername)
    }

    static PhoneCallResponse parseJSON(String text) {
        return parseJSON(new JSONObject(text))
    }

    static PhoneCallResponse parseJSON(JSONObject object) {
        int sessionId = object.getInt('sessionId')
        int forwarderPort = object.getInt('forwarderPort')
        String sourceUsername = object.getString('sourceUsername')
        String targetUsername = object.getString('targetUsername')
        return new PhoneCallResponse(sessionId: sessionId, forwarderPort: forwarderPort, sourceUsername: sourceUsername,
                targetUsername: targetUsername)
    }
}
