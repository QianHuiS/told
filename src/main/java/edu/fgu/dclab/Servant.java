package edu.fgu.dclab;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Servant implements Runnable {  //服務客戶端的系統(一對一服務)
    private ObjectOutputStream out = null;  //一個物件的輸出串流(內建函數):物件必須支援Serializable, 才能序列化進行網路傳遞.
    private String source = null;   //登入者的ID, 預設為空.

    private Socket socket = null;

    private ChatRoom room = null;

    private int money= 1000;
    private int seat= 0;
    private int eat= 0;
    private int eattime= 0;
    private String food[][]= {
            {"空氣","0",null},
            {"碧螺春","100",null},
            {"東方美人","110",null},
            {"白毫烏龍","80",null},
            {"陳年普洱","120",null},
            {"錫蘭紅茶","60",null},
            {"客家擂茶","90",null},
            {"珍珠奶茶","55",null},
            {"宇治抹茶","120",null},
            {"玄米茶","110",null},
            {"忘情水","500",null},
            {"孟婆湯","500",null},
            {"心痛的感覺","1000",null}
    };
    private int flag[]= {1,1,1,1,1};    //askmenu,lookme,useTime,
    long start= 0;
    long end= 0;
    long useTime= 0;
    int choke= 0;
    int choketime= 3;


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

        greet();    //初來乍到
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
                    this.source =((LoginMessage) message).ID;  //取得使用者ID.

                    this.room.multicast(new ChatMessage(    //multicast為廣播功能.
                        "【樓層】",
                        MessageFormat.format("{0} 走進了茶樓。", this.source)
                    ));

                    this.room.multicast(new RoomMessage(
                        room.getRoomNumber(),
                        room.getNumberOfGuests()
                    ));

                    askseat();  //入座
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
    //走入茶樓 系統回應
    private void teahuouse() {
        String[] s = {
                "......\n"+
                        "艷陽高照, 微風拂過你的臉龐,\n"+
                        "小販的吆喝熙熙攘攘充滿了整條大街,\n"+
                        "一陣馬蹄聲噠噠揚起塵土......\n\n"+
                        "正愁沒個地方歇息, 你抬頭望一旁的人聲鼎沸處,\n"+
                        "「茶」字旗幡隨風飄揚,\n金色四字匾額慢慢映入你的眼簾,\n"+
                        "大字橫書:\n 有、間、茶、樓 !\n\n"
        };

        for (String msg : s) {
            write(new ChatMessage("???", msg));
        }
    }

    //初來乍到 系統回應
    private void greet() {
        teahuouse();

        String[] greetings = {
            "歡迎來到「有間茶樓」!",
            "請問這位客官如何稱呼? (請輸入暱稱)\n"
        };

        for (String msg : greetings) {
            write(new ChatMessage("店小二", msg));
        }
    }

    //入座 系統回應
    private void askseat() {
        String[] s = {
                this.source+", 您想坐在哪桌呢? (請輸入 seat 1-10)\n"
        };

        for (String msg : s) {
            write(new ChatMessage("店小二", msg));
        }
    }

    //入座
    public void seat(String msg) {
        try {
            int i = Integer.parseInt(msg.substring(5));  //取出第4個字之後的字串(玩家輸入的桌號),String解析為10進制整數.
            if (i>=1 &&i<=10) {
                this.seat = i;

                this.write(new ChatMessage(
                                "店小二",
                                "要去"+msg.substring(5)+"桌嗎? 好的!讓我帶您入座...\n"
                        )
                );

                if(flag[0]==1) {  askmenu();  flag[0]--;}
            } else
                this.write(new ChatMessage(
                                "店小二",
                                msg.substring(5)+"桌? 只有1-10桌號哦~"
                        )
                );

        } catch (Exception e) {
            this.write(new ChatMessage(
                            "店小二",
                            msg.substring(5)+"桌? 沒有這個桌號哦~"
                    )
            );
        }
    }

    //點餐 系統回應
    private void askmenu() {
        String[] s = {
                "這是我們店裡的 菜單(menu) , 客官您慢慢看!",
                "要點餐再說一聲就行了, 小的先去忙了...\n(叫出菜單輸入 menu)\n"
        };

        for (String msg : s) {
            write(new ChatMessage("店小二", msg));
        }

        if(flag[1]==1) {  lookme();  flag[1]--;}
    }

    //察看座位及銅錢
    public void lookme() {
        this.write(new ChatMessage(
                        this.source,
                        MessageFormat.format("\n你在{0}號桌的座位上, 摸了摸袖中, 自己大概還有{1}銅錢。\n(察看座位及銅錢 輸入 lookme)\n",
                                seat, money)
                )
        );
    }

    //菜單
    public void menu() {
        String[] str= {
                "\n┌────────────────┐" +
                        "\n│\t\t[ 有張菜單 ]\t\t│" +
                        "\n│\t\t\t\t\t\t│"+
                        "\n│   1.碧螺春\t\t100\t\t│" +
                        "\n│   2.東方美人\t110\t\t│" +
                        "\n│   3.白毫烏龍\t80\t\t│" +
                        "\n│   4.陳年普洱\t120\t\t│" +
                        "\n│   5.錫蘭紅茶\t60\t\t│" +
                        "\n│   6.客家擂茶\t90\t\t│" +
                        "\n│   7.珍珠奶茶\t55\t\t│" +
                        "\n│   8.宇治抹茶\t120\t\t│"+
                        "\n│   9.玄米茶\t\t110\t\t│"+
                        "\n│  10.忘情水\t\t500\t\t│"+
                        "\n│  11.孟婆湯\t\t500\t\t│"+
                        "\n│  12.心痛的感覺\t1000\t│"+
                        "\n│\t\t\t\t\t\t│"+
                        "\n└────────────────┘"+
                        "\n(點餐請輸入 order 1-12)"
        };

        for(String menu: str){
            write(new ChatMessage("菜單", menu));
        }
    }

    //點餐
    public void order(String msg) {
        if(eattime==0) {
            try {
                int i = Integer.parseInt(msg.substring(6));  //取出第5個字之後的字串(玩家輸入的餐點),String解析為10進制整數.
                if (i >= 1 && i <= 12) {
                    this.eat = i;

                    this.room.multicast(new ChatMessage(
                            "【"+this.source+"】",
                                    MessageFormat.format("小二! 來一壺{0}!!", food[eat][0])
                            )
                    );

                    this.room.multicast(new ChatMessage(
                            "【店小二】",
                            "一壺" + food[eat][0] + "嗎? 客官還請稍候片刻, 馬上就來!\n"
                            )
                    );

                    askeat();

                } else
                    this.write(new ChatMessage(
                                    "店小二",
                                    msg.substring(5) + "號餐? 只有1-12種餐點哦~"
                            )
                    );

            } catch (Exception e) {
                this.write(new ChatMessage(
                                "店小二",
                                msg.substring(5) + "? 沒有這個餐點哦~"
                        )
                );
            }
        } else {
            this.write(new ChatMessage(
                            "店小二",
                            "客官您還沒吃完呢! 就急著點餐?"
                    )
            );
        }
    }

    //上餐 系統回應
    private void askeat() {

        this.write(new ChatMessage(
                        "店小二",
                        MessageFormat.format("客官~ 這是您點的{0}, \n這樣一共是{1}元... (食用餐點輸入 eat)\n",food[eat][0],food[eat][1])
                )
        );
        this.money-=Integer.parseInt(food[eat][1]);
        this.eattime= 5;

        if(money<=200)  askpss();
    }

    public void eat() {

        if(eattime>1) {
            this.write(new ChatMessage(
                            this.source,
                            MessageFormat.format("...\n(你給自己沏上一碗{0}, 抿了口, 靜品清茗、沁人心脾...)\n", food[eat][0])
                    )
            );
            eattime--;
            usetime();
        }
        else if(eattime==1){
            this.write(new ChatMessage(
                            this.source,
                            MessageFormat.format("...\n(壺中的{0}所剩無幾, 你乾脆揚起茶壺一飲而盡。)\n", food[eat][0])
                    )
            );
            usetime();
            eat=0;
            eattime--;
        } else {
            this.write(new ChatMessage(
                    this.source, "唔...要點些什麼呢... (你尚未點餐, 吃啥呢!)"));
        }
    }

    public void usetime() {
        if(flag[2]==1) {
            start = System.currentTimeMillis(); //以毫秒為單位, 1秒=1000毫秒.
            flag[2]--;
        } else {
            end= System.currentTimeMillis();
            useTime= end -start;
            start= end;
            end=0;
            if(useTime<10*1000)
                choke++;
        }
    }

    //嗆到的表現
    public void choke(String msg) {
        int sumlenght= msg.length();
        //this.write(new ChatMessage("測試", msg.substring(sumlenght/2)));
        msg=msg.replaceFirst(msg.substring(sumlenght/2),"...咳!咳咳!"); //將原字串的1/3-2/3處替換成咳咳.
        this.room.multicast(new ChatMessage("【"+this.source+"】", msg+" (嗆到了)"));
    }

    //猜拳 系統回應
    private void askpss() {
        String[] s = {
                "啊啊~~! 無聊死了!", "本公子難得出來逛逛, 居然還沒個人作陪! 鬱悶~"+
                "\n(與貴公子玩猜拳, 輸入 pss 剪刀/石頭/布)\n"
        };

        for (String msg : s) {
            this.room.multicast(new ChatMessage("【吳吉郎】", msg));
        }

    }

    //猜拳
    public void paperScissorsStone(String msg){
        try {
            msg = msg.substring(4);
            if (msg.equals("剪刀") || msg.equals("石頭") || msg.equals("布")) {
                this.write(new ChatMessage(this.source, "吳公子若是無聊, 我"+this.source+"來作陪會會你!"));
                this.write(new ChatMessage(this.source + "&吳吉郎", "剪刀、石頭、布..."));
                this.write(new ChatMessage(this.source, msg + "!"));

                int x = (int) (Math.random() * 3 + 1);
                String s = "";
                if (x == 1) {
                    s = "剪刀";
                }
                if (x == 2) {
                    s = "石頭";
                }
                if (x == 3) {
                    s = "布";
                }

                this.write(new ChatMessage("吳吉郎", s + "!"));


                if ("剪刀".equals(msg) & x == 1) {
                    this.write(new ChatMessage("吳吉郎", "哎呀!好可惜呀!"));
                }
                if ("剪刀".equals(msg) & x == 2) {
                    this.write(new ChatMessage("吳吉郎", "你還太嫩了!"));
                }
                if ("剪刀".equals(msg) & x == 3) {
                    this.write(new ChatMessage("吳吉郎", "哼!別誤會了!這是故意讓你的!\n(貴公子留下了50銅板)"));
                    this.money+=50;
                }
                if ("石頭".equals(msg) & x == 1) {
                    this.write(new ChatMessage("吳吉郎", "哼!別誤會了!這是故意讓你的!\n(貴公子留下了50銅板"));
                    this.money+=50;
                }
                if ("石頭".equals(msg) & x == 2) {
                    this.write(new ChatMessage("吳吉郎", "哎呀!好可惜呀!"));
                }
                if ("石頭".equals(msg) & x == 3) {
                    this.write(new ChatMessage("吳吉郎", "你還太嫩了!"));
                }
                if ("布".equals(msg) & x == 1) {
                    this.write(new ChatMessage("吳吉郎", "哼!別誤會了!這是故意讓你的!\n(貴公子留下了50銅板"));
                    this.money+=50;
                }
                if ("布".equals(msg) & x == 2) {
                    this.write(new ChatMessage("吳吉郎", "你還太嫩了!"));
                }
                if ("布".equals(msg) & x == 3) {
                    this.write(new ChatMessage("吳吉郎", "哎呀!好可惜呀!"));
                }

            } else
                write(new ChatMessage("吳吉郎", "剪刀、石頭、布...嗯? 你出這什麼拳?耍我呀!?"));
        }catch(Exception e) {
            write(new ChatMessage("吳吉郎", "剪刀、石頭、布...嗯? 你出這什麼拳?耍我呀!?"));
        }
    }

    //問時間
    private void time() {   //用戶輸入time?時, 系統回應時間.
        String dataTime ="你問現在幾點? "+getDateTime()+"。";
        write(new ChatMessage("店小二", dataTime));

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

    //功能指令
    public void instruction(Message message) {
        String msg=((ChatMessage)message).MESSAGE;
        if(msg.startsWith("time?"))    //判斷是否以seat開頭.
            time();

        else if(msg.startsWith("seat")) {
            seat(msg);
        }

        else if(msg.startsWith("menu"))
            menu();

        else if(msg.startsWith("lookme"))
            lookme();

        else if(msg.startsWith("order")) {
            order(msg);
        }

        else if(msg.startsWith("eat")) {
            eat();
        }

        else if(msg.startsWith("pss")) {
            paperScissorsStone(msg);
        }

        else {
            //this.write(new ChatMessage("測試","choke="+choke+"\nchoketime="+choketime+"\n"));
            if (choke >0) {
                choke(msg);
                choketime--;
                if (choketime == 0) {
                    choketime=3;
                    choke--;
                }
            } else
                this.room.multicast(new ChatMessage("【"+this.source+"】", msg));
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