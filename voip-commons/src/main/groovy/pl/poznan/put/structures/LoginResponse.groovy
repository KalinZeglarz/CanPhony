package pl.poznan.put.structures

import org.json.JSONObject
import pl.poznan.put.GlobalConstants

class LoginResponse extends ApiResponse {
    String message = ""
    String pubSubHost = ""
    int pubSubPort = -1
    boolean audioEncryption = GlobalConstants.ENCRYPT_AUDIO

    static LoginResponse parseJSON(final String text) {
        JSONObject parsedJson = new JSONObject(text)
        String message = parsedJson.getString("message")
        String pubSubHost = null
        int pubSubPort = -1
        boolean audioEncryption = false
        if (message.isBlank()) {
            pubSubHost = parsedJson.getString("pubSubHost")
            pubSubPort = parsedJson.getInt("pubSubPort")
            audioEncryption = parsedJson.getBoolean("audioEncryption")
        }
        return new LoginResponse(message: message, pubSubHost: pubSubHost, pubSubPort: pubSubPort, audioEncryption: audioEncryption)
    }

    @Override
    JSONObject toJSON() {
        return new JSONObject().put("message", message)
                .put("pubSubHost", pubSubHost)
                .put("pubSubPort", pubSubPort)
                .put("audioEncryption", audioEncryption)
    }
}
