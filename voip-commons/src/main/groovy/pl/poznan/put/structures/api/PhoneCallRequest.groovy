package pl.poznan.put.structures.api

import org.json.JSONObject
import pl.poznan.put.structures.JSONable
import pl.poznan.put.structures.api.ApiRequest

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
