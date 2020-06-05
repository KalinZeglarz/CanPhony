package pl.poznan.put.structures

import org.json.JSONObject

class StringMessage implements JSONable {

    String str

    @Override
    JSONObject toJSON() {
        return new JSONObject()
                .put('str', str)
    }

    static StringMessage fromJSON(JSONObject json) {
        return new StringMessage(str: json.getString('str'))
    }

}
