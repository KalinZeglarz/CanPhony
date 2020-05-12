package pl.poznan.put.structures

import org.json.JSONObject

class LoginResponse extends ApiResponse {
    String subPubHost
    int subPubPort

    static LoginResponse parseJSON(final String text) {
        JSONObject parsedJson = new JSONObject(text)
        String subPubHost = parsedJson.getString('subPubHost')
        int subPubPort = parsedJson.getInt('subPubPort')
        return new LoginResponse(subPubHost: subPubHost, subPubPort: subPubPort)
    }
}
