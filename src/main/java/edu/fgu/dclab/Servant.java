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

        //遊戲流程
        greet();    //初來乍到
        //點餐

    }

    public void process(Message message) {
        switch (message.getType()) {
            case Message.ROOM_STATE:
                this.write(message);
                break;

            case Message.CHAT:
                instruction(message);
                break;

            case Message.LOGIN: //登入聊天室(設定ID)
                if (this.source == null) {
                    this.source = ((LoginMessage) message).ID;  //取得使用者ID.
                    this.room.multicast(new ChatMessage(    //multicast為廣播功能.
                        "【樓層】",
                        MessageFormat.format("{0} 走進了茶樓。", this.source)
                    ));

                    this.room.multicast(new RoomMessage(
                        room.getRoomNumber(),
                        room.getNumberOfGuests()
                    ));

                    seat();     //入座
                }

                else if(!((LoginMessage) message).SEAT.equals("null")) {
                    this.write(new ChatMessage(
                            "店小二",
                            MessageFormat.format("{0}桌 嗎? 好的!讓我帶您入座...", (((LoginMessage)message).SEAT))

                            )
                    );

                    order();
                }
                break;

            default:
        }
    }

    public void write(Message message) {
        try {
            this.out.writeObject(message);  //檢查()是否為Serializable物件, ObjectOutputStream
            this.out.flush();   //送出系統緩衝區內容(即時傳送不等待)
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //遊戲流程相關
    //初次進入 系統回應
    private void greet() {
        String[] greetings = {
            "歡迎來到「有間茶樓」!",
            "請問這位客官如何稱呼?(請輸入暱稱)"
        };

        for (String msg : greetings) {
            write(new ChatMessage("店小二", msg));
        }
    }

    //入座 系統回應
    private void seat() {
        String[] s = {
                this.source+", 您想坐在哪桌呢?(請輸入1-10)"
        };

        for (String msg : s) {
            write(new ChatMessage("店小二", msg));
        }
    }

    //點餐 系統回應
    private void order() {
        String[] s = {
                "這是我們店裡的 菜單(menu) , 客官您慢慢看!",
                "要點餐再說一聲就行了, 小的先去忙了...\n(輸入menu可叫出菜單並進行點餐)"
        };

        for (String msg : s) {
            write(new ChatMessage("店小二", msg));
        }
    }


    //各種功能
    public void instruction(Message message) {
        String msg=((ChatMessage)message).MESSAGE;
        switch (msg) {
            case "time?":
                time();
                break;

            case "menu":   //點餐
                menu();
                break;

            default:
                this.write(message);
                //this.room.multicast(message);   //廣播(multicas)這條訊息.*/
        }
    }

    //功能指令相關
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

    public void menu() {
        String[] str= {
                "\n┌────────────────┐" +
                        "\n│\t\t[ 有張菜單 ]\t\t│" +
                        "\n│\t\t\t\t\t\t│"+
                        "\n│   1.碧螺春\t\t100\t\t│" +
                        "\n│   2.東方美人\t110\t\t│" +
                        "\n│   3.白毫烏龍\t80\t\t│" +
                        "\n│   4.陳年普洱\t100\t\t│" +
                        "\n│   5.錫蘭紅茶\t60\t\t│" +
                        "\n│   6.客家擂茶\t90\t\t│" +
                        "\n│   7.珍珠奶茶\t55\t\t│" +
                        "\n│   8.宇治抹茶\t120\t\t│"+
                        "\n│   9.玄米茶\t\t110\t\t│"+
                        "\n│  10.忘情水\t\t500\t\t│"+
                        "\n│  11.孟婆湯\t\t500\t\t│"+
                        "\n│  12.心痛的感覺\t1000\t│"+
                        "\n│\t\t\t\t\t\t│"+
                        "\n└────────────────┘"
        };

        for(String menu: str){
            write(new ChatMessage("[ 菜 單 ]", menu));
        }
    }


    @Override
    public void run() {
        Message message;

        try (
            ObjectInputStream in = new ObjectInputStream(
                this.socket.getInputStream()
            )
        ) {
            //this.process((Message)in.readObject()); //讀取取得一物件(網路層來的訊息), 指定用(Message)的格式解讀.

            while ((message = (Message) in.readObject()) != null) { //解讀訊息種類.
                this.process(message);

                /*String TIME="time?";
                if ( ((ChatMessage)message).MESSAGE.equals(TIME) )
                    time();
                else
                    this.room.multicast(message);   //廣播(multicas)這條訊息.*/
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