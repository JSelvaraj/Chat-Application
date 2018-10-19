import common.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ServiceMain extends Thread {
    public static void main(String args[]) {
        ServiceMain program = new ServiceMain();
        int choice = 0;
        while (choice != 4) {
            choice = program.menu();
            switch (choice) {
                case 1: program.setDestinationAddress();
                    program.setPortNumber();
                    break;
                case 2: program.connectSocket();
                    program.sendMessages();
                    break;
                case 3:
                    program.connectHostSocket();
                    program.sendMessages();
                    break;
            }
        }




    }

    private Socket socket;
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

    public void connectSocket() {
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
            ServerSocket hostSocket = new ServerSocket();
            socket = hostSocket.accept();
            reader = socket.getInputStream();
            writer = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessages(){
        try {
            if (!socket.isConnected()) {
                throw new ClientHasNotConnectedException();
            } else if (username == null) {
                throw new UsernameNotSetException();
            } else {
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
        } catch (ClientHasNotConnectedException e) {
            System.out.println("You have not connected to a host");
        } catch (UsernameNotSetException e) {
            System.out.println("You have not set a username");
        }

    }

    public void sendMessage(String msg){
        try {
            if (!socket.isConnected()) {
                throw new ClientHasNotConnectedException();
            } else if (username == null) {
                throw new UsernameNotSetException();
            } else {
                PrintWriter sender = new PrintWriter(writer, true);
                sender.print(username + ": " + msg);
                System.out.println(username + ": " + msg);
                sender.close();
            }
        } catch (ClientHasNotConnectedException e) {
            System.out.println("You have not connected to a host");
        } catch (UsernameNotSetException e) {
            System.out.println("You have not set a username");
        }
    }

    private void receiveMessages() {
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

    public void run() { receiveMessages();}

    private int menu() {
        Scanner kb = new Scanner(System.in);
        int choice = 0;
        System.out.println();
        System.out.println("----------------------------------------------------------------------");
        System.out.println("----------------------------------------------------------------------");
        System.out.println();
        System.out.println("Options:");
        System.out.println();
        System.out.println("1. Set Destination Address and IP");
        System.out.println("2. Connect to another user");
        System.out.println("3. Host another user");
        System.out.println("4. Quit");
        System.out.println();
        System.out.print("Choose an Option 1/2/3/4: ");
        while (choice < 1 && choice > 4) {
            System.out.println();
            System.out.print("Choose an Option 1/2/3/4: ");
            try {
                choice = kb.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Usage: 1/2/3/4");
            }

        }
        return choice;

    }






}
