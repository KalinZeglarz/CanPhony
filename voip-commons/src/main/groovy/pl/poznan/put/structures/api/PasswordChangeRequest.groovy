package pl.poznan.put.structures.api

import org.json.JSONObject

class PasswordChangeRequest extends LoginRequest {
    String newPassword

    @Override
    JSONObject toJSON() {
        return super.toJSON().put("newPassword", newPassword)
    }

}
