package pl.poznan.put.structures

import org.json.JSONObject

class PhoneCallResponse extends ApiResponse implements JSONable {
    int sessionId
    int forwarderPort

    @Override
    JSONObject toJSON() {
        return new JSONObject()
                .put('sessionId', sessionId)
                .put('forwarderPort', forwarderPort)
    }

    static PhoneCallResponse parseJSON(String text) {
        return parseJSON(new JSONObject(text))
    }

    static PhoneCallResponse parseJSON(JSONObject object){
        int sessionId = object.getInt('sessionId')
        int forwarderPort = object.getInt('forwarderPort')
        return new PhoneCallResponse(sessionId: sessionId, forwarderPort: forwarderPort)
    }
}
