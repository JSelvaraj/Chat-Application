
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Hashtable;

public class GroupChat {

    private static final int MAX_NUMBER_OF_MESSAGES = 20;
    private static final int SO_TIMEOUT = 10;
    private static final String ESCAPE_CHARACTER = "q";

    private int portNumber;
    private ServerSocket hostSocket;
    private Socket socket;
    private SimpleObjectQueue clientQ;
    private SimpleObjectQueue messageQ;

    private Hashtable<Socket, String> socketToUser;

    /**
     * When an instance of the group chat server is started it makes a client and message queue and
     * it waits 10 seconds for the first connection.
     * @param portNumber the portnumber the server is listening on
     * @throws SocketTimeoutException if no client connects in the first 10 seconds.
     */
    public GroupChat(int portNumber) throws SocketTimeoutException {
        clientQ = new SimpleObjectQueue("ClientQ"); //max clients is 10 by default
        messageQ = new SimpleObjectQueue("MessageQ", MAX_NUMBER_OF_MESSAGES);
        this.portNumber = portNumber;
        try {
            socketToUser = new Hashtable<>();
            hostSocket = new ServerSocket(portNumber);
            hostSocket.setSoTimeout(10000);
            System.out.println("Server waiting for connection.");
            socket = hostSocket.accept();
            clientQ.add(socket);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String username = reader.readLine();
            socketToUser.put(socket, username);
        } catch (QueueFullException e) { //Impossible for this exception to be called at this time.
        } catch (SocketTimeoutException e) {
            try {
                hostSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            throw new SocketTimeoutException();
        } catch (IOException e) { e.printStackTrace();}
    }

    /**
     * Goes through the following process:
     * 1. Waits for new clients.
     * 2. Goes through the client queue, if the client it gets is null it skips ahead to next iteration.
     * 3. If there is a client at the index, it checks whether there is anything on that client's input stream.
     * 4a. If the message is null that means the stream has connected abnormally and the client is removed from the queue
     * 4b. If the message is the ESCAPE_CHARACTER it means the client wishes to disconnect. So it removes the socket from
     * the queue and adds a message to the message queue to inform the other users that that individual has left.
     * 4c. In all other cases it adds the message to the message queue.
     * 5. It goes through the message queue sending messages out
     */
    public void Server() {
        try {
            hostSocket.setSoTimeout(SO_TIMEOUT);
        } catch (SocketException e) {
        }

        while (clientQ.count() > 0) {

            getClient();

            try {
                for (int i = 0; i < clientQ.size(); i++) {
                    socket = (Socket) clientQ.get(i);
                    if (socket != null) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        if (reader.ready()) {
                            String message = reader.readLine();
                            if (message != null ) {

                                if (!Client.extractMsg(message).equals(ESCAPE_CHARACTER)) {
                                    messageQ.add(message);
                                } else {
                                    messageQ.add(extractUsername(message) + " has left the group chat");
                                    System.out.println(extractUsername(message) + "has left the server");
                                    removeSocket(socket);
                                }
                            } else { // if message = null - This means connection has been lost so it can be terminated
                                removeSocket(socket);
                            }
                        }
                    }
                }
                messageClients();
                if (clientQ.count() == 0) {
                    System.out.println("All users have left the group chat server");
                    hostSocket.close();
                }
            } catch (IOException e) {
            } catch (QueueFullException e) {
            } catch (QueueEmptyException e) {
            }


        }
    }

    /**
     * Helper function that closes a socket, deletes it from the clientQ and removes it from the Hashtable to free up space.
     * @param socket the socket to be closed.
     */
    private void removeSocket(Socket socket) {
        socketToUser.remove(socket);
        try {
            socket.close();
            clientQ.delete(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to add new client for 10ms.
     *
     * If a new client is found, according to the messaging protocol they will send their username to the server.
     * the socket added is put into a hashtable with the username as the value. (<Key, Value> = <Socket, Username> )
     */
    private void getClient() {
        Socket newClient = null;
        try {
            newClient = hostSocket.accept();
            if (newClient != null) {
                newClient.setTcpNoDelay(true);
                clientQ.add(newClient);
                BufferedReader reader = new BufferedReader(new InputStreamReader(newClient.getInputStream()));
                String username = reader.readLine();
                System.out.println(username + " has joined the server...");
                socketToUser.put(newClient, username);
            }
        }   catch (SocketTimeoutException e) {
        }   catch (IOException e) {
            e.printStackTrace();
        }   catch (QueueFullException e) {
        }
    }

    /**
     * Takes a message and returns the username from the message. If there isn't a colon it means the message is a server
     * message informing the connected users a previous user has left. Therefore getting the ' has' index will allow
     * use to get the substring containing the username.
     * @param chatMessage The message being checked.
     * @return The username in the message.
     */
    private String extractUsername(String chatMessage) {
        int index = chatMessage.indexOf(":");
        if (index == -1) {
            index = chatMessage.indexOf(" has");
        }
        return chatMessage.substring(0, index);
    }

    /**
     * Iterates through the message queue.
     *
     * For each message it goes through the client queue of sockets and sends the message out to each.
     *
     * Before it sends the message it checks the current socket's associated username in the hashtable, against the
     * username of the message.
     * @throws QueueEmptyException
     * @throws IOException
     */
    private void messageClients() throws QueueEmptyException, IOException {
        if (!messageQ.isEmpty() && !clientQ.isEmpty()) {
            String message = (String) messageQ.remove();
            for (int i = 0; i < clientQ.size(); i++) {
                socket = (Socket) clientQ.get(i);
                if (socket != null) {
                    PrintWriter writer = new PrintWriter((socket.getOutputStream()));
                    if (socketToUser.get(socket) != null && !socketToUser.get(socket).equals(extractUsername(message))) {
                        writer.println(message);
                        writer.flush();
                    }
                }
            }
        }
    }
}

