import common.ClientHasNotConnectedException;
import common.InvalidSocketAddressException;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable{

    private static final String ESCAPE_CHARACTER = "q";


    private String username;
    private Socket socket;

    private boolean receiveMessagesThreadFlag = false;
    private boolean sendMessagesThreadFlag = false;


    public Client(Socket socket, String username) {
        this.username = username;
        this.socket = socket;
    }

    /**
     * This takes in input from the user's keyboard and outputs it to the outputstream of the current socket.
     * It continuously takes input until the escape character is sent or until the receiveMessages method closes.
     */
    public void sendMessages() {
        sendMessagesThreadFlag = true;
        try {
            Scanner kb = new Scanner(System.in);
            String msg = "";
            PrintWriter sender = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            System.out.println("You may now enter your messages...");
            do  {
                msg = kb.nextLine();
                if (msg.length() > 0) {
                    sender.println(username + ": " + msg);
                    sender.flush();
                }
            } while (!msg.equals(ESCAPE_CHARACTER) && receiveMessagesThreadFlag);
            if (msg.equals(ESCAPE_CHARACTER) ) {
                System.out.println("Escape character detected... Closing connection to client");
            } else {
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessagesThreadFlag = false;
        synchronizer();
    }

    /**
     * Separates the message and username in the chat message.
     * @param chatMessage the entire string sent (<username>: <msg>)
     * @return the message part of the chat message.
     */
    private String extractMsg(String chatMessage) {
        int colonIndex = chatMessage.indexOf(":");
        String msg = chatMessage.substring(colonIndex + 1);
        return msg.trim();
    }


    /**
     * Is run in a separate thread. It continually waits for incoming messages while the main thread waits for user
     * input to send.
     * If the incoming message is null, this means that the connection has been terminated prematurely
     * and the method throws an exception and exits.
     */
    private void receiveMessages(Socket socket) {
        receiveMessagesThreadFlag = true;
        try {
            BufferedReader receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            msg = "<>";
            do {
                msg = receiver.readLine();
                if (msg != null && !extractMsg(msg).equals(ESCAPE_CHARACTER)) {
                    System.out.println(msg);
                } else if (msg == null)  {
                    throw new IOException();
                }
            } while (msg != null && !extractMsg(msg).equals(ESCAPE_CHARACTER) && sendMessagesThreadFlag);
            if (extractMsg(msg).equals(ESCAPE_CHARACTER)) {
                System.out.println("Other terminal has terminated connection...");
                System.out.println("Press enter twice to return to menu...");
            }
        } catch (IOException e) {
            System.out.println("Connection has been terminated");
            System.out.println("Press enter twice to return to menu...");
        }
        receiveMessagesThreadFlag = false;
        synchronizer();
    }

    private void synchronizer() {
        try {
            while (sendMessagesThreadFlag || receiveMessagesThreadFlag) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method required by the Runnable interface.
     */
    public void run() {
        receiveMessages(socket);
    }

}
