package edu.fgu.dclab;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Servant implements Runnable {  //服務客戶端的系統(一對一服務)
    private ObjectOutputStream out = null;  //一個物件的輸出串流(內建函數):物件必須支援Serializable, 才能序列化進行網路傳遞.
    private String source = null;   //登入者的ID, 預設為空.

    private Socket socket = null;

    private ChatRoom room = null;

    public Servant(Socket socket, ChatRoom room) {
        this.room = room;
        this.socket = socket;

        try {
            this.out = new ObjectOutputStream(
                this.socket.getOutputStream()   //取得輸出訊息
            );
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        greet();
    }

    public void process(Message message) {
        switch (message.getType()) {
            case Message.ROOM_STATE:
                this.write(message);
                break;

            case Message.CHAT:
                this.write(message);
                break;

            case Message.LOGIN: //登入聊天室(設定ID)
                if (this.source == null) {
                    this.source = ((LoginMessage) message).ID;  //取得使用者ID.
                    this.room.multicast(new ChatMessage(    //multicast為廣播功能.
                        "MurMur",
                        MessageFormat.format("{0} 進入了聊天室。", this.source)
                    ));

                    this.room.multicast(new RoomMessage(
                        room.getRoomNumber(),
                        room.getNumberOfGuests()
                    ));
                }

                break;

            default:
        }
    }

    private void write(Message message) {
        try {
            this.out.writeObject(message);  //檢查()是否為Serializable物件, ObjectOutputStream
            this.out.flush();   //送出系統緩衝區內容(即時傳送不等待)
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void greet() {
        String[] greetings = {
            "歡迎來到 MurMur 聊天室",
            "請問你的【暱稱】?"
        };

        for (String msg : greetings) {
            write(new ChatMessage("MurMur", msg));
        }
    }

    //用戶輸入time?時, 系統回應時間.
    private void time() {
        String dataTime ="現在時間為，"+getDateTime()+"。";
        write(new ChatMessage("MurMur", dataTime));

/*        this.room.multicast(new ChatMessage(    //廣播現在時間.
                "MurMur", MessageFormat.format(
                        "現在時間為，{0}。", getDateTime() )
        ));*/
    }

    //取得系統時間
    public String getDateTime() {
        DateFormat shortFormat =
                DateFormat.getDateTimeInstance(
                        DateFormat.SHORT, DateFormat.SHORT);

//        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        Date date = new Date();
        String strDate = shortFormat.format(date);
        return strDate;
    }

    @Override
    public void run() {
        Message message;

        try (
            ObjectInputStream in = new ObjectInputStream(
                this.socket.getInputStream()
            )
        ) {
            this.process((Message)in.readObject()); //讀取取得為一物件, 指定用(Message)的格式解讀.

            while ((message = (Message) in.readObject()) != null) { //解讀訊息種類.
                String TIME="time?";
                if ( ((ChatMessage)message).MESSAGE.equals(TIME) )
                    time();
                else
                    this.room.multicast(message);   //廣播(multicas)這條訊息.
            }

            this.out.close();
        }
        catch (IOException e) {
            System.out.println("Servant: I/O Exc eption");
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

// Servant.java