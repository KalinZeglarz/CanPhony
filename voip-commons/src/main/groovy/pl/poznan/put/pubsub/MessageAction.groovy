package pl.poznan.put.pubsub

enum MessageAction {
    NONE,
    CALL_REQUEST,
    ACCEPT_CALL,
    REJECT_CALL,
    END_CALL,
    KEY_EXCHANGE;

    private MessageAction() {}
}