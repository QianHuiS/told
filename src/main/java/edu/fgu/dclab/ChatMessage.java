package edu.fgu.dclab;

public class ChatMessage extends AbstractMessage {
    public final String MESSAGE;
    public int SEAT= 0;

    public ChatMessage(String source, String message) {
        this.source = source;
        this.MESSAGE = message;
    }

    public ChatMessage(int seat, String source, String message) {
        this.SEAT = seat;
        this.source = source;
        this.MESSAGE = message;
    }


    public int getType() {
        return Message.CHAT;
    }
}
