import common.ClientHasNotConnectedException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Client {

    private ServerSocket hostSocket;
    private static final int SO_TIMEOUT = 10000;

    public Server( int portNumber, String userName) {
        this.portNumber = portNumber;
        this.username = userName;
    }

    public Server () {

    }

    /**
     * Waits for a connection from another terminal
     * @throws ClientHasNotConnectedException If there is a socketTimeoutException
     */
    public void connectHostSocket() throws ClientHasNotConnectedException {
        try {
            hostSocket = new ServerSocket(portNumber);
            hostSocket.setSoTimeout(SO_TIMEOUT);
            System.out.println("Waiting for client to connect...");
            socket = hostSocket.accept();
            System.out.println("Client found....");
        } catch (IOException e) {
            throw new ClientHasNotConnectedException();
        }
    }

    /**
     * Listens at a port number and provides a socket.
     * @param portNumber The portnumber the ServerSocket is listening on.
     * @return the socket representing the input and outputstreams
     * @throws ClientHasNotConnectedException If there is a socketTimeoutException
     */
    public Socket connectHostSocket(int portNumber) throws ClientHasNotConnectedException {
        try {
            Socket socket;
            hostSocket = new ServerSocket(portNumber);
            hostSocket.setSoTimeout(SO_TIMEOUT);
            System.out.println("Waiting for client to connect...");
            socket = hostSocket.accept();
            System.out.println("Client found....");
            return socket;
        } catch (IOException e) {
            throw new ClientHasNotConnectedException();
        }
    }

    /**
     * Similar to method defined in superclass, but also closes and nulls the ServerSocket.
     */
    @Override
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
            if (hostSocket != null) {
                hostSocket.close();
                hostSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Similar to method defined in superclass, but also closes and nulls the ServerSocket.
     * @param socket the socket that is closed.
     */
    @Override
    public void closeSocket(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
            if (hostSocket != null) {
                hostSocket.close();
                hostSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
