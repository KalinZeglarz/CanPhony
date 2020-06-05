package pl.poznan.put.pubsub

import org.json.JSONObject
import pl.poznan.put.structures.JSONable

import java.time.LocalDateTime

class Message implements JSONable {

    LocalDateTime timeStamp = null
    MessageAction action = null
    String sender = null
    String target = null
    JSONObject content = null

    @Override
    JSONObject toJSON() {
        JSONObject json = new JSONObject()
        json.put('timestamp', timeStamp)
        json.put('action', action)
        json.put('sender', sender)
        json.put('target', target)
        if (content != null) {
            json.put('content', content)
        }
        return json
    }

    static Message parseJSON(final String text) {
        JSONObject parsedJson = new JSONObject(text)
        LocalDateTime timeStamp = LocalDateTime.parse(parsedJson.getString('timestamp'))
        MessageAction messageAction = MessageAction.valueOf(parsedJson.getString('action'))
        String sender = parsedJson.getString('sender').toString()
        String target = parsedJson.getString('target').toString()
        JSONObject json = null
        if (parsedJson.has('content')) {
            json = parsedJson.getJSONObject('content')
        }
        return new Message(timeStamp: timeStamp, action: messageAction, sender: sender, target: target, content: json)
    }

}
