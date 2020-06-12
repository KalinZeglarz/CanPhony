package pl.poznan.put.structures.api

import org.json.JSONObject
import pl.poznan.put.structures.UserStatus

class UserListResponse extends ApiResponse {
    Map<String, UserStatus> userList

    static UserListResponse parseJSON(final String text) {
        JSONObject parsedJson = new JSONObject(text)
        Map<String, UserStatus> userList = parsedJson.get("userList") as Map<String, UserStatus>
        return new UserListResponse(userList: userList)
    }

    @Override
    JSONObject toJSON() {
        return new JSONObject().put("userList", new JSONObject(userList))
    }
}
