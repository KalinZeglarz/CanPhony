package pl.poznan.put.structures.api

import org.json.JSONObject
import pl.poznan.put.structures.api.ApiResponse

class MessageResponse extends ApiResponse {
    String message

    @Override
    JSONObject toJSON() {
        return new JSONObject().put("message", message)
    }
}
