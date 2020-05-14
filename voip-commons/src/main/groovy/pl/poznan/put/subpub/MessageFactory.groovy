package pl.poznan.put.subpub

import pl.poznan.put.structures.JSONable

import java.time.LocalDateTime

class MessageFactory {

    static Message createMessage(final MessageAction messageAction) {
        return createMessage(messageAction, '', null)
    }

    static Message createMessage(final MessageAction messageAction, final String sender) {
        return createMessage(messageAction, sender, null)
    }

    static Message createMessage(final JSONable object) {
        return createMessage(null, '', object)
    }

    static Message createMessage(final MessageAction messageAction, final JSONable object) {
        createMessage(messageAction, '', object)
    }

    static Message createMessage(final MessageAction messageAction, final String sender, final JSONable object) {
        if (object != null) {
            return new Message(sender: sender, timeStamp: LocalDateTime.now(), action: messageAction,
                    content: object.toJSON())
        } else {
            return new Message(sender: sender, timeStamp: LocalDateTime.now(), action: messageAction)
        }
    }

}
