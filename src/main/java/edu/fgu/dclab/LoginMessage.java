package edu.fgu.dclab;

public class LoginMessage extends AbstractMessage {
    public final String ID;
    public final String PASSWORD;   //不希望訊息產生後被改變, 用final.

    public LoginMessage(String id, String password) {
        this.ID = id;
        this.PASSWORD = password;
    }

    public int getType() {
        return Message.LOGIN;
    }   //getType目前用於Servant的process(), 對不同狀態的訊息做不同處理.
}
