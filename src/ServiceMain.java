import common.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServiceMain {
    public static void main(String args[]) {

    }

    private Socket socket;
    private ServerSocket hostSocket;
    private String destinationAddress;
    private int portNumber;
    private String username;

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

    public void connectSocket() throws ClientHasNotConnectedException {
        if (destinationAddress == null || portNumber < 1023 || portNumber > 65535) {
            throw new ClientHasNotConnectedException();
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

    public void connectHostSocket() {
        try {
            socket = hostSocket.accept();
            reader = socket.getInputStream();
            writer = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessages() throws ClientHasNotConnectedException, UsernameNotSetException {
        if (!socket.isConnected()) {
            throw new ClientHasNotConnectedException();
        }
        if (username == null) {
            throw new UsernameNotSetException();
        }
        Scanner kb = new Scanner(System.in);
        String msg = "";
        PrintWriter sender = new PrintWriter(writer, true);
        while (!msg.equals("q")) {
            System.out.print("Enter your message:");
            if (!msg.equals("q")) {
                msg = kb.nextLine();
                sender.print(username + ": " + msg);
                System.out.println(username + ": " + msg);
            }
        }
        sender.close();
    }

    public void sendMessage(String msg) throws ClientHasNotConnectedException, UsernameNotSetException {
        if (!socket.isConnected()) {
            throw new ClientHasNotConnectedException();
        }
        if (username == null) {
            throw new UsernameNotSetException();
        }
        PrintWriter sender = new PrintWriter(writer, true);
        sender.print(username + ": " + msg);
        System.out.println(username + ": " + msg);
        sender.close();
    }

    private void receiveMessages() throws ClientHasNotConnectedException {
        if (!socket.isConnected()) {
            throw new ClientHasNotConnectedException();
        }
        BufferedReader receiver = new BufferedReader(new InputStreamReader(reader));
        try {
            while (true) {
                System.out.println(receiver.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    public void run() throws ClientHasNotConnectedException {
        receiveMessages();
    }

}
