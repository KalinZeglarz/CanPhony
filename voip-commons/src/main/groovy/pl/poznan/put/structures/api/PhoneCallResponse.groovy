package pl.poznan.put.structures.api

import org.json.JSONObject

class PhoneCallResponse extends ApiResponse {
    int forwarderPort
    String sourceUsername
    String targetUsername

    @Override
    JSONObject toJSON() {
        return new JSONObject()
                .put("forwarderPort", forwarderPort)
                .put("sourceUsername", sourceUsername)
                .put("targetUsername", targetUsername)
    }

    static PhoneCallResponse parseJSON(String text) {
        return parseJSON(new JSONObject(text))
    }

    static PhoneCallResponse parseJSON(JSONObject object) {
        int forwarderPort = object.getInt("forwarderPort")
        String sourceUsername = object.getString("sourceUsername")
        String targetUsername = object.getString("targetUsername")
        return new PhoneCallResponse(forwarderPort: forwarderPort, sourceUsername: sourceUsername,
                targetUsername: targetUsername)
    }
}
