package edu.fgu.dclab;

import java.io.Serializable;

public interface Message extends Serializable { //序列化:把訊息內容編碼後透過網路傳輸再解碼.
    //訊息種類
    int LOGIN = 0;
    int ROOM_STATE = 1; //聊天室訊息; 設定人數.更新狀態列等用途.
    int CHAT = 2;   //聊天內容訊息.

    String getSource(); //取得訊息來源.
    int getType();
}
/*繼承關係
Message↓
Abstract Message↓
Login/Chat/Room Message
*/