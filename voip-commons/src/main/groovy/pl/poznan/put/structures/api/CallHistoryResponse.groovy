package pl.poznan.put.structures.api

import org.json.JSONObject

class CallHistoryResponse extends ApiResponse {

    List<String> usernames = new ArrayList<>()
    List<String> dates = new ArrayList<>()
    List<String> durations = new ArrayList<>()

    static CallHistoryResponse parseJSON(final String text) {
        JSONObject parsedJson = new JSONObject(text)
        List<String> usernames = parsedJson.get("usernames") as List<String>
        List<String> dates = parsedJson.get("dates") as List<String>
        List<String> durations = parsedJson.get("durations") as List<String>
        return new CallHistoryResponse(usernames: usernames, dates: dates, durations: durations)
    }

    @Override
    JSONObject toJSON() {
        return new JSONObject().put("usernames", usernames)
                .put("dates", dates)
                .put("durations", durations)
    }

}
