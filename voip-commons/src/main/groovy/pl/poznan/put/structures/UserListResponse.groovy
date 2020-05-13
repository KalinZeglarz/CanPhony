package pl.poznan.put.structures

import org.json.JSONObject

class UserListResponse extends ApiResponse {
    Set<String> userList

    static UserListResponse parseJSON(final String text) {
        JSONObject parsedJson = new JSONObject(text)
        Set<String> userList = parsedJson.get('userList') as Set<String>
        return new UserListResponse(userList: userList)
    }

}
