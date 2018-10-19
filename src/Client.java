import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import common.*;

public class Client {

    private Socket socket;
    private String destinationAddress;
    private int portNumber;

    private InputStream reader;
    private OutputStream writer;


    static int soTimeout = 10; //milliseconds - TcpClientSimpleNB.java
    static int bufferSize = 80;

    public int getPortNumber() {
        return portNumber;
    }
    public String getDestinationAddress() {
        return destinationAddress;
    }
    public Socket getSocket() {
        return socket;
    }
    public void setPortNumber() {
        System.out.println();
        Scanner kb = new Scanner(System.in);
        int tempPort = 0;
        while (tempPort < 1023 || tempPort > 65535) {
            System.out.print("What is the port number of your destination:");
            tempPort = kb.nextInt();
            if (tempPort < 1023 || tempPort > 65535) {
                System.out.println("Port numbers must be between 1024 and 65535");
                System.out.println();
            }
        }
        portNumber = tempPort;
    }
    public void setPortNumber(int number) throws InvalidPortNumberException {
        if (number < 1023 || number > 65535) {
            throw new InvalidPortNumberException();
        } else {
            portNumber = number;
        }
    }
    public void setDestinationAddress() {
        System.out.println();
        Scanner kb = new Scanner(System.in);
        System.out.print("Please enter your destination address/IP: ");
        destinationAddress = kb.nextLine();
    }
    public void setDestinationAddress(String Address) {
        destinationAddress = Address;
    }
    public void connectSocket() {
        if (destinationAddress == null || portNumber < 1023 || portNumber > 65535) {
            throw new IllegalArgumentException("Destination Address and/or port number has not been set.");
        }
        try {
            socket = new Socket(destinationAddress, portNumber);
            //socket.setSoTimeout(soTimeout);
            reader = socket.getInputStream();
            writer = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMessage() throws ClientHasNotConnectedException {
        if (!socket.isConnected()) {
            throw new ClientHasNotConnectedException();
        }
        System.out.print("Input the message you would like to send: ");






    }


    }
