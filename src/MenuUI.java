import common.ClientHasNotConnectedException;
import common.InvalidSocketAddressException;

import java.awt.*;
import java.net.SocketTimeoutException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class MenuUI {

    private String username;
    private ConnectionHandler connectionHandler;

    public MenuUI(){
        this.username = setUsername();
        connectionHandler = new ConnectionHandler();
    }

    public MenuUI(String s) {
        this.username = setUsername();
        connectionHandler = new ConnectionHandler(s);
    }

    /**
     * This function creates objects and calls methods according to what function the users wants the applications to
     * complete. To see what number corresponds to what function go to line 108-114 of this code.
     *
     * 3. Connect to another user. The Client objects allows the user to send and receive messages as long as it is
     * provided a socket to write to
     * 4. Host another user. Same as 3. However the in this instance the connectionHandler hosts a connection rather
     * than connects to another users.
     * 5. Send a File. Sends a file to a given socket.
     * 6. Receives a file from a given socket.
     */
    public void menu() {
        int choice = 0;
        while (choice != 8) {
            choice = getChoice(connectionHandler.getDestinationAddress(), connectionHandler.getPortNumber());
            switch (choice) {
                case 1:
                    connectionHandler.setDestinationAddress();
                    break;
                case 2:
                    connectionHandler.setPortNumber();
                    break;
                case 3:
                    try {
                        Client client = new Client(connectionHandler.connectSocket(), username);
                        new Thread(client).start();
                        client.sendMessages();
                        connectionHandler.closeSockets();
                    } catch (ClientHasNotConnectedException e) {
                        System.out.println("Invalid Address, please check your destination before trying again...");
                    } catch (InvalidSocketAddressException e) {
                        System.out.println("Destination address and/or port number have are not valid.");
                    }
                    break;
                case 4:
                    try {
                        Client client = new Client(connectionHandler.connectHostSocket(), username);
                        new Thread(client).start();
                        client.sendMessages();
                        connectionHandler.closeSockets();
                    } catch (ClientHasNotConnectedException e) {
                        System.out.println("Server Socket Timed out...");
                        System.out.println("Returning to menu...");
                    }
                    break;
                case 5:
                    try {
                        FileShare fileShare = new FileShare(connectionHandler.connectSocket());
                        fileShare.sendFile();
                        connectionHandler.closeSockets();
                    } catch (ClientHasNotConnectedException e) {
                        System.out.println("Invalid Address, please check your destination before trying again...");
                    } catch (InvalidSocketAddressException e) {
                        System.out.println("Destination address and/or port number have are not valid.");
                    }
                    break;
                case 6:
                    try {
                        FileShare fileShare = new FileShare(connectionHandler.connectHostSocket());
                        fileShare.receiveFile();
                        connectionHandler.closeSockets();
                    } catch (ClientHasNotConnectedException e) {
                        System.out.println("Server Socket Timed out...");
                        System.out.println("Returning to menu...");
                    }
                    break;
                case 7:
                    try {
                        GroupChat groupChat = new GroupChat(connectionHandler.getPortNumber());
                        groupChat.Server();
                    } catch (SocketTimeoutException e) {
                        System.out.println("Socket timeout on group chat server...");
                        System.out.println("Returning to menu...");
                    }
            }
        }
    }

    /**
     * Simple text UI for getting the users choice of function. Choices are outlined lines 108-114.
     * @return choice - a number corresponding to a function offered by this application.
     */
    private int getChoice(String destinationAddress, int portNumber) {
        Scanner kb = new Scanner(System.in);
        int choice = 0;
        System.out.println();
        System.out.println("----------------------------------------------------------------------");
        System.out.println("----------------------------------------------------------------------");
        System.out.println("WELCOME: " + username);
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
        System.out.println("7. Start Group Chat Server (note: cannot send messages while hosting)");
        System.out.println("8. Exit");
        System.out.println();
        while (choice < 1 || choice > 8) {
            System.out.println();
            System.out.print("Choose an Option: ");
            try {
                choice = kb.nextInt();
            } catch (InputMismatchException e) {
                System.out.println();
                System.out.println("Usage: 1-8");
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
    private String setUsername() {
        System.out.print("Please Enter your username: ");
        Scanner kb = new Scanner(System.in);
        return kb.nextLine();
    }


}
