import common.ClientHasNotConnectedException;
import common.InvalidSocketAddressException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ConnectionHandler {

    private static final int MAX_CONNECTION_ATTEMPTS = 4;
    private static final int SO_TIMEOUT = 10000;

    private String destinationAddress = "127.0.0.1";
    private int portNumber = 51638;
    private Socket socket;
    private ServerSocket hostSocket;


    public ConnectionHandler() {

    }

    public ConnectionHandler(String s) {
        this.destinationAddress = s;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    /**
     * Gets a port number from the user. Also does some basic checks to ensure it's acceptable.
     */
    public void setPortNumber() {
        System.out.println();
        Scanner kb = new Scanner(System.in);
        int tempPort = 0;
        try {
            while (tempPort < 1023 || tempPort > 65535) {
                System.out.print("What is the port number of your destination: ");
                tempPort = kb.nextInt();
                if (tempPort < 1023 || tempPort > 65535) {
                    System.out.println("Port numbers must be between 1024 and 65535");
                    System.out.println();
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("Port numbers must be a NUMBER between 1024 and 65535, given in digits");
        }
        portNumber = tempPort;
    }

    /**
     * Gets a destination address from the user.
     */
    public void setDestinationAddress() {
        System.out.println();
        Scanner kb = new Scanner(System.in);
        System.out.print("Please enter your destination address/IP: ");
        destinationAddress = kb.nextLine();
    }

    /**
     * Attempts to connect to a socket according to the current objects destination address and port number
     * @return a socket.
     * @throws ClientHasNotConnectedException if the method cannot cannot to the host server for whatever reason.
     * @throws InvalidSocketAddressException if the values for port number and destination address are invalid.
     */
    public Socket connectSocket() throws ClientHasNotConnectedException, InvalidSocketAddressException {
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
        return socket;
    }

    /**
     * Waits for a connection from another terminal
     * @throws ClientHasNotConnectedException If there is a socketTimeoutException
     */
    public Socket connectHostSocket() throws ClientHasNotConnectedException {
        try {
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
     * Closes any active sockets in the object.
     */
    public void closeSockets() {
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
