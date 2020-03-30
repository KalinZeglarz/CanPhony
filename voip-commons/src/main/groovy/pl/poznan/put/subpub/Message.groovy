package pl.poznan.put.subpub

import groovy.transform.PackageScope
import org.json.JSONObject

import java.time.LocalDateTime

class Message implements JSONable {

    final private LocalDateTime timeStamp
    final private MessageType messageType
    final private JSONObject content

    private Message() {
        this.timeStamp = null
        this.messageType = null
        this.content = null
    }

    @PackageScope
    Message(LocalDateTime timeStamp, MessageType messageType, JSONObject content) {
        this.timeStamp = timeStamp
        this.messageType = messageType
        this.content = content
    }

    @PackageScope
    Message(LocalDateTime timeStamp, MessageType messageType) {
        this(timeStamp, messageType, null)
    }

    @PackageScope
    Message(LocalDateTime timeStamp, JSONObject content) {
        this(timeStamp, MessageType.NONE, content)
    }

    @PackageScope
    Message(LocalDateTime timeStamp, String text) {
        this(timeStamp, MessageType.TEXT, new JSONObject("{'content': ${text}"))
    }

    LocalDateTime getTimeStamp() {
        return timeStamp
    }

    MessageType getMessageType() {
        return messageType
    }

    JSONObject getContent() {
        return content
    }

    @Override
    JSONObject toJSON() {
        JSONObject json = new JSONObject()
        json.put('timestamp', timeStamp)
        json.put('type', messageType)
        if (content != null) {
            json.put('content', content)
        }
        return json
    }

    static Message parseJSON(final String text) {
        JSONObject parsedJson = new JSONObject(text)
        LocalDateTime timeStamp = LocalDateTime.parse(parsedJson.get('timestamp').toString())
        MessageType messageType = MessageType.valueOf(parsedJson.get('type').toString())
        JSONObject json = null
        if (parsedJson.has('content')) {
            json = parsedJson.getJSONObject('content')
        }
        return new Message(timeStamp, messageType, json)
    }
}
