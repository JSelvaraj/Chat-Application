import common.ClientHasNotConnectedException;
import common.InvalidSocketAddressException;

import java.util.InputMismatchException;
import java.util.Scanner;

public class ConnectionHandler {

    private String username;
    private String destinationAddress = "127.0.0.1";
    private int portNumber = 51638;

    public ConnectionHandler(){
        this.username = setUsername();
    }

    public ConnectionHandler(String s)  {
        this.username = setUsername();
        this.destinationAddress = s;
    }

    public void menu() {
        int choice = 0;
        while (choice != 7) {
            choice = getChoice(destinationAddress, portNumber);
            switch (choice) {
                case 1:
                    setDestinationAddress();
                    break;
                case 2:
                    setPortNumber();
                    break;
                case 3:
                    try {
                        Client client = new Client(destinationAddress, portNumber, username);
                        client.connectSocket();
                        new Thread(client).start();
                        client.sendMessages();
                        client.closeSocket();
                    } catch (ClientHasNotConnectedException e) {
                        System.out.println("Invalid Address, please check your destination before trying again...");
                    } catch (InvalidSocketAddressException e) {
                        System.out.println("Destination address and/or port number have are not valid.");
                    }
                    break;
                case 4:
                    try {
                        Server server = new Server(portNumber, username);
                        server.connectHostSocket();
                        new Thread(server).start();
                        server.sendMessages();
                        server.closeSocket();
                    } catch (ClientHasNotConnectedException e) {
                        System.out.println("Server Socket Timed out...");
                        System.out.println("Returning to menu...");
                    }
                    break;
                case 5:
                    try {
                        Client client = new Client();
                        FileShare fileShare = new FileShare(client.connectSocket(destinationAddress, portNumber));
                        fileShare.sendFile();
                        client.closeSocket(fileShare.getSocket());
                    } catch (ClientHasNotConnectedException e) {
                        System.out.println("Invalid Address, please check your destination before trying again...");
                    } catch (InvalidSocketAddressException e) {
                        System.out.println("Destination address and/or port number have are not valid.");
                    }
                    break;
                case 6:
                    try {
                        Server server = new Server();
                        FileShare fileShare = new FileShare(server.connectHostSocket(portNumber));
                        fileShare.receiveFile();
                        server.closeSocket(fileShare.getSocket());
                    } catch (ClientHasNotConnectedException e) {
                        System.out.println("Server Socket Timed out...");
                        System.out.println("Returning to menu...");
                    }
                    break;
            }
        }
    }

    /**
     * Simple text UI for getting the users choice of function.
     * @return choice - a number corresponding to a function offered by this application.
     */
    private int getChoice(String destinationAddress, int portNumber) {
        Scanner kb = new Scanner(System.in);
        int choice = 0;
        System.out.println();
        System.out.println("----------------------------------------------------------------------");
        System.out.println("----------------------------------------------------------------------");
        System.out.println();
        System.out.println("Current Destination: " + (destinationAddress == null ? "Not Set" : destinationAddress));
        System.out.println("Current Port Number: " + portNumber);
        System.out.println();
        System.out.println("Options:");
        System.out.println();
        System.out.println("1. Set Destination Address");
        System.out.println("2. Set Port Number");
        System.out.println("3. Connect to another user");
        System.out.println("4. Host another user");
        System.out.println("5. Send a file.");
        System.out.println("6. Receive a file");
        System.out.println("7. Quit");
        System.out.println();
        while (choice < 1 || choice > 7) {
            System.out.println();
            System.out.print("Choose an Option: ");
            try {
                choice = kb.nextInt();
            } catch (InputMismatchException e) {
                System.out.println();
                System.out.println("Usage: 1-7");
                break;
            }
        }
        System.out.println();
        System.out.println("----------------------------------------------------------------------");
        System.out.println("----------------------------------------------------------------------");
        System.out.println();
        return choice;
    }

    /**
     * Gets username from user.
     */
    public String setUsername() {
        System.out.print("Please Enter your username: ");
        Scanner kb = new Scanner(System.in);
        return kb.nextLine();
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
}
