package pl.poznan.put.subpub

enum MessageAction {
    NONE,
    CALL_REQUEST,
    ACCEPT_CALL,
    END_CALL;

    private MessageAction() {}
}