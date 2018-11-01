import common.ClientHasNotConnectedException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Client {

    protected ServerSocket hostSocket;
    protected static final int SO_TIMEOUT = 10000;

    public Server( int portNumber, String userName) {
        this.portNumber = portNumber;
        this.username = userName;
    }

    public Server () {

    }

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
