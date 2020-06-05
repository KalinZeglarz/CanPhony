package pl.poznan.put.structures

import org.json.JSONObject

class LoginResponse extends ApiResponse {
    String message = ""
    String pubSubHost = ""
    int pubSubPort = -1

    static LoginResponse parseJSON(final String text) {
        JSONObject parsedJson = new JSONObject(text)
        String message = parsedJson.getString('message')
        String pubSubHost = null
        int pubSubPort = -1
        if (message.isBlank()) {
            pubSubHost = parsedJson.getString('pubSubHost')
            pubSubPort = parsedJson.getInt('pubSubPort')
        }
        return new LoginResponse(message: message, pubSubHost: pubSubHost, pubSubPort: pubSubPort)
    }
}
