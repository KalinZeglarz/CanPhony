package pl.poznan.put.structures.api

import org.json.JSONObject
import pl.poznan.put.structures.JSONable
import pl.poznan.put.structures.api.ApiRequest

class LoginRequest extends ApiRequest implements JSONable {
    String username
    String password

    @Override
    JSONObject toJSON() {
        return new JSONObject()
                .put('username', username)
                .put('password', password)
    }

}
