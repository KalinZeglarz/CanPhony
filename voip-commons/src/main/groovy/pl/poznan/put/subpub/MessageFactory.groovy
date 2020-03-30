package pl.poznan.put.subpub

import org.json.JSONObject

import java.time.LocalDateTime

class MessageFactory {

    static Message createMessage(final String text) {
        return new Message(LocalDateTime.now(), text)
    }

    static Message createMessage(final MessageType messageType) {
        return new Message(LocalDateTime.now(), messageType)
    }

    static Message createMessage(final JSONable object) {
        return new Message(LocalDateTime.now(), object.toJSON())
    }

    static Message createMessage(final JSONObject json) {
        return new Message(LocalDateTime.now(), json)
    }

}
