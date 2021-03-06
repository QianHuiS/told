package edu.fgu.dclab;

public class LoginMessage extends AbstractMessage {
    public final String ID;
    public final String PASSWORD;   //不希望訊息產生後被改變, 用final.
    /*public final int MONEY;
    public final int SEAT;
    public final int EAT;
*/

    public LoginMessage(String id, String password/*int money, int seat, int eat*/) {
        this.ID = id;
        this.PASSWORD = password;
        /*this.MONEY= money;
        this.SEAT= seat;
        this.EAT = eat;
        */
    }

    public int getType() {
        return Message.LOGIN;
    }   //getType目前用於Servant的process(), 對不同狀態的訊息做不同處理.
}
