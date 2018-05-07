package edu.fgu.dclab;

import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ChatRoom implements Runnable {
    private int roomNumber = 0;

    private BlockingQueue<Message> messageQueue = new LinkedBlockingDeque<>();
    private Vector<Servant> servants = new Vector<>();

    public void enter(Socket client) {
        Servant servant = new Servant(client, this);

        servants.add(servant);

        new Thread(servant).start();
    } // enter()

    public int getNumberOfGuests() {
        return servants.size();
    }

    public int getRoomNumber() {
        return this.roomNumber;
    }

    public void multicast(Message message) {
        try {
            this.messageQueue.put(message);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message message = this.messageQueue.take();

                for (Servant servant : servants) {
                    servant.process(message);
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// ChatRoom.java