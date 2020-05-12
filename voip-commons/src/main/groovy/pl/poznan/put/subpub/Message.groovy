package pl.poznan.put.subpub

import org.json.JSONObject
import pl.poznan.put.structures.JSONable

import java.time.LocalDateTime

class Message implements JSONable {

    private LocalDateTime timeStamp = null
    private MessageAction action = null
    private String sender = null
    private JSONObject content = null

    LocalDateTime getTimeStamp() {
        return timeStamp
    }

    MessageAction getAction() {
        return action
    }

    String getSender() {
        return sender
    }

    JSONObject getContent() {
        return content
    }

    @Override
    JSONObject toJSON() {
        JSONObject json = new JSONObject()
        json.put('timestamp', timeStamp)
        json.put('action', action)
        json.put('sender', sender)
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
        JSONObject json = null
        if (parsedJson.has('content')) {
            json = parsedJson.getJSONObject('content')
        }
        return new Message(timeStamp: timeStamp, action: messageAction, sender: sender, content: json)
    }

}
