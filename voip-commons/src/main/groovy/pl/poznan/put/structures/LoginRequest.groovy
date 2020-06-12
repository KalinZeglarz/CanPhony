package pl.poznan.put.structures

import org.json.JSONObject

class LoginRequest extends ApiRequest implements JSONable {
    String username
    String password

    @Override
    JSONObject toJSON() {
        return new JSONObject()
                .put("username", username)
                .put("password", password)
    }

}
