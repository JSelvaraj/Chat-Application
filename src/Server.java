import common.ClientHasNotConnectedException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Client {

    private ServerSocket hostSocket;
    private static final int SO_TIMEOUT = 10000;

    public Server () {

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

}
