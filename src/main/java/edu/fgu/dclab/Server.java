package edu.fgu.dclab;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final int PORT = 4004;

    private ChatRoom chatRoom;

    public Server() {
        this.chatRoom = new ChatRoom();

        new Thread(chatRoom).start();

        try (
            ServerSocket serverSocket = new ServerSocket(PORT);
        ) {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                this.chatRoom.enter(clientSocket);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Server.java
/*
Server等待連線>連線後ChatRoom>ChatRoom中呼叫Servant.
*/