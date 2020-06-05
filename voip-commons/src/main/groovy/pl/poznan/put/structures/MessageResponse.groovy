package pl.poznan.put.structures

import org.json.JSONObject

class MessageResponse extends ApiResponse {
    String message

    @Override
    JSONObject toJSON() {
        return new JSONObject().put('message', message)
    }
}
