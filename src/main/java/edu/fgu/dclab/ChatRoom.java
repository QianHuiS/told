package edu.fgu.dclab;

import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ChatRoom implements Runnable {
    private int roomNumber = 0;

    private BlockingQueue<Message> messageQueue = new LinkedBlockingDeque<>();
    private Vector<Servant> servants = new Vector<>();  //Vector<>為動態陣列, 紀錄所有玩家.

    private Vector<Servant> seat_0= new Vector<>();    //桌位玩家.
    private Vector<Servant> seat_1= new Vector<>();
    private Vector<Servant> seat_2= new Vector<>();
    private Vector<Servant> seat_3= new Vector<>();
    private Vector<Servant> seat_4= new Vector<>();
    private Vector<Servant> seat_5= new Vector<>();
    private Vector<Servant> seat_6= new Vector<>();
    private Vector<Servant> seat_7= new Vector<>();
    private Vector<Servant> seat_8= new Vector<>();
    private Vector<Servant> seat_9= new Vector<>();
    private Vector<Servant> seat_10= new Vector<>();


    private int multicastFlag= 0;   //桌位廣播種類shout=0/seat=1/talk=2.
    private int seatNo= 0;  //桌位.
    //若我寫個 X桌陣列 寫個add桌(){X桌陣列.add(servant)} 寫個X桌廣播.

    public void enter(Socket client) {
        Servant servant = new Servant(client, this);

        servants.add(servant);  //加入整棟樓廣播
        seat_0.add(servant);    //加入0桌.

        new Thread(servant).start();
    } // enter()

    public int getNumberOfGuests() {
        return servants.size();
    }

    public int getRoomNumber() {
        return this.roomNumber;
    }

    //桌位對應陣列
    public Vector<Servant> returnSeatArr(int seat){  //給桌號回傳該桌陣列
        switch(seat){
            case 0:
                return seat_0;  //return後不可接break;, 因return後的程式碼unreachable statement.
            case 1:
                return seat_1;
            case 2:
                return seat_2;
            case 3:
                return seat_3;
            case 4:
                return seat_4;
            case 5:
                return seat_5;
            case 6:
                return seat_6;
            case 7:
                return seat_7;
            case 8:
                return seat_8;
            case 9:
                return seat_9;
            case 10:
                return seat_10;
            default:    //default必須有return, 否則出現missing return statement.
                return servants;
        }
    }

    //檢查是否坐滿5人
    public boolean checkSeat(int seat){
        if (returnSeatArr(seat).size()<5)   return false;
        else return true;
    }

    //加入某桌位
    public void addSeat(Servant servant, int seat){
        returnSeatArr(seat).add(servant);
    }

    //離開某桌
    public void removeSeat(Servant servant, int seat){
        returnSeatArr(seat).remove(servant);
    }

    //全體廣播(shout)
    public void multicast(Message message) {
        multicastFlag=0;
        try {
            this.messageQueue.put(message); //在佇列中放入訊息.
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //桌位or悄悄廣播(seat/talk)
    public void seatMulticast(int channel, int seat, Message message) {
        multicastFlag=channel;      //頻道設定, 1為桌位2為悄悄話.
        seatNo=seat;

        try {
            this.messageQueue.put(message); //在佇列中放入訊息.
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            while (true) {
                Message message = this.messageQueue.take(); //從佇列中取出訊息. //為何要放入佇列, 而不直接呼叫廣播()回傳msg@@?
                //foreach把訊息發給陣列的人.
                //寫個flag, 每個不同頻道的廣播用不同代號, 若為哪個代號就送不同陣列的人.

                switch(multicastFlag){  //廣播種類
                    case 0: //shout全樓層
                        for (Servant servant : servants) {  //Servant servant的內容為陣列servant.
                            servant.write(message);   //若廣播功能改為process中處理, 即可改為write();直接送出訊息.
                        }
                        break;
                    case 1: //seat桌位+前後桌
                        for (Servant servant : returnSeatArr(seatNo)) {
                            servant.write(message);
                        }
                        if(seatNo==0)   break;  //若為0桌就不用前後桌聽到
                        if(seatNo!=1){  //第一桌沒有前一桌
                            for (Servant servant : returnSeatArr(seatNo-1))
                                servant.write(message);
                        }
                        if(seatNo!=10){ //第十桌沒有後一桌
                            for (Servant servant : returnSeatArr(seatNo+1))
                                servant.write(message);
                        }
                        break;
                    case 2: //talk只同桌
                        for (Servant servant : returnSeatArr(seatNo)) {
                            servant.write(message);
                        }
                        break;
                    default:
                }

            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// ChatRoom.java