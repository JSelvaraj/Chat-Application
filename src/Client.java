import common.ClientHasNotConnectedException;
import common.InvalidSocketAddressException;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable{

    protected static final String ESCAPE_CHARACTER = "q";
    private static final int MAX_CONNECTION_ATTEMPTS = 4;

    protected Socket socket;
    private String destinationAddress = "127.0.0.1";
    protected int portNumber = 51638;
    protected String username;

    protected boolean receiveMessagesThreadFlag = false;
    protected boolean sendMessagesThreadFlag = false;


    public Client(String destinationAddress, int portNumber, String username) {
        this.destinationAddress = destinationAddress;
        this.portNumber = portNumber;
        this.username = username;
    }

    public Client() {
    }

    public Client(int portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * Attempts to connect to a socket and get an input and output stream.
     * @throws ClientHasNotConnectedException if it cannot connect to a host at the address provided
     */
    public void connectSocket() throws ClientHasNotConnectedException, InvalidSocketAddressException {
        int i = 0;
        while (i < MAX_CONNECTION_ATTEMPTS && socket == null) {
            try {
                if (destinationAddress == null || portNumber < 1023 || portNumber > 65535) {
                    throw new InvalidSocketAddressException();
                }
                System.out.println("Searching for server...");
                socket = new Socket(destinationAddress, portNumber);
                System.out.println("Server found...");
            } catch (IOException e) {
                System.out.println("Server not found... Retrying...");
                i++;
                if (i == MAX_CONNECTION_ATTEMPTS) {
                    throw new ClientHasNotConnectedException();
                }
            }
        }
    }

    public Socket connectSocket(String destinationAddress, int portNumber) throws ClientHasNotConnectedException, InvalidSocketAddressException {
        int i = 0;
        while (i < MAX_CONNECTION_ATTEMPTS && socket == null) {
            try {
                Socket socket;
                if (destinationAddress == null || portNumber < 1023 || portNumber > 65535) {
                    throw new InvalidSocketAddressException();
                }
                System.out.println("Searching for server...");
                socket = new Socket(destinationAddress, portNumber);
                System.out.println("Server found...");
                return socket;
            } catch (IOException e) {
                System.out.println("Server not found... Retrying...");
                i++;
                if (i == MAX_CONNECTION_ATTEMPTS) {
                    throw new ClientHasNotConnectedException();
                }
            }
        }
        return null;
    }

    /**
     * First this checks that the socket has some connections and that a username has been set.
     * Then gets input from the user's keyboard. Then it sends the username + the message to the connected
     * computer.
     * If the character 'q' is sent it means the user has finished sending messages.
     * at that time the user is informed and the program will exit..
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
    }

    /**
     * Separates the message and username in the chat message.
     * @param chatMessage the entire string sent (<username>: <msg>)
     * @return the message part of the chat message.
     */
    protected String extractMsg(String chatMessage) {
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
    protected void receiveMessages() {
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
        }
        receiveMessagesThreadFlag = false;
    }

    public void run() {
        receiveMessages();
    }

    /**
     * This method closes all active sockets.
     *
     * I currently do not know why, but if there is not a Thread.sleep call, the program will sometimes
     * not continue even if both flags are false.
     */
    public void closeSocket() {
        while (receiveMessagesThreadFlag || sendMessagesThreadFlag) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {

            }
        }
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method closes a provided socket.
     * @param socket the socket that is closed.
     */
    public void closeSocket(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
