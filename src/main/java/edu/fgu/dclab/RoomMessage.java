package edu.fgu.dclab;

public class RoomMessage extends AbstractMessage {
    public final int NUMBER_OF_GUESTS;  //目前房間有多少人.
    public final int ROOM_NUMBER;

    public RoomMessage(int roomNumber, int guests) {
        this.ROOM_NUMBER = roomNumber;  //房間編號, 未來可能擴充多個聊天室.
        this.NUMBER_OF_GUESTS = guests;
    }

    public int getType() {
        return Message.ROOM_STATE;
    }
}
