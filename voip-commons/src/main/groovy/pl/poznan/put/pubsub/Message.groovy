package pl.poznan.put.pubsub

import org.json.JSONException
import org.json.JSONObject
import pl.poznan.put.structures.JSONable

import java.time.LocalDateTime

class Message implements JSONable {

    LocalDateTime timeStamp = LocalDateTime.now()
    MessageAction action = MessageAction.NONE
    String sender = null
    String target = null
    String content = null

    JSONObject getContentAsJson() {
        return new JSONObject(content)
    }

    @Override
    JSONObject toJSON() {
        JSONObject json = new JSONObject()
        json.put("timestamp", timeStamp)
        json.put("action", action)
        json.put("sender", sender)
        json.put("target", target)
        if (content != null) {
            json.put("content", content)
        }
        return json
    }

    static Message parseJSON(final String text) {
        JSONObject parsedJson = new JSONObject(text)
        LocalDateTime timeStamp = LocalDateTime.parse(parsedJson.getString("timestamp"))
        MessageAction messageAction = MessageAction.valueOf(parsedJson.getString("action"))
        String sender = parsedJson.getString("sender").toString()
        String target = parsedJson.getString("target").toString()
        String content = null
        if (parsedJson.has("content")) {
            try {
                content = parsedJson.getJSONObject("content").toString()
            } catch (JSONException ignored) {
                content = parsedJson.getString("content")
            }
        }
        return new Message(timeStamp: timeStamp, action: messageAction, sender: sender, target: target, content: content)
    }

}
