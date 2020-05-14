package pl.poznan.put.structures

import org.json.JSONObject

class PhoneCallRequest extends ApiRequest implements JSONable {
    String sourceUsername
    String targetUsername

    @Override
    JSONObject toJSON() {
        return new JSONObject()
                .put('sourceUsername', sourceUsername)
                .put('targetUsername', targetUsername)
    }

}
