package pl.poznan.put.structures

import org.json.JSONObject

class LoginResponse extends ApiResponse {
    String message = ""
    String subPubHost = ""
    int subPubPort = -1

    static LoginResponse parseJSON(final String text) {
        JSONObject parsedJson = new JSONObject(text)
        String message = parsedJson.getString('message')
        String subPubHost = null
        int subPubPort = -1
        if (message.isBlank()) {
            subPubHost = parsedJson.getString('subPubHost')
            subPubPort = parsedJson.getInt('subPubPort')
        }
        return new LoginResponse(message: message, subPubHost: subPubHost, subPubPort: subPubPort)
    }
}
