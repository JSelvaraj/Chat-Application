import common.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Resources
 */

public class ServiceMain implements Runnable {
    public static void main(String args[]) {

        ServiceMain program;
        if (args.length == 1) {
            program = new ServiceMain(args[0]);
        } else {
            program = new ServiceMain();
        }
        int choice = 0;
        program.setUsername();
        while (choice != 7) {
            choice = program.menu();
            switch (choice) {
                case 1:
                    program.setDestinationAddress();
                    break;
                case 2:
                    program.setPortNumber();
                    break;
                case 3:
                    try {
                        program.connectSocket();
                        new Thread(program).start();
                        program.sendMessages();
                        program.closeSocket();
                    } catch (ClientHasNotConnectedException e) {
                        System.out.println("Invalid Address, please check your destination before trying again...");
                    } catch (InvalidSocketAddressException e) {
                        System.out.println("Destination address and/or port number have are not valid.");
                    }
                    break;
                case 4:
                    try {
                        program.connectHostSocket();
                        new Thread(program).start();
                        program.sendMessages();
                        program.closeSocket();
                    } catch (ClientHasNotConnectedException e) {
                        System.out.println("Server Socket Timed out...");
                        System.out.println("Returning to menu...");
                    }
                    break;
                case 5:
                    try {
                        program.connectSocket();
                        program.sendFile();
                        program.closeSocket();
                    } catch (ClientHasNotConnectedException e) {
                        System.out.println("Invalid Address, please check your destination before trying again...");
                    } catch (InvalidSocketAddressException e) {
                        System.out.println("Destination address and/or port number have are not valid.");
                    }
                    break;
                case 6:
                    try {
                        program.connectHostSocket();
                        program.receiveFile();
                        program.closeSocket();
                    } catch (ClientHasNotConnectedException e) {
                        System.out.println("Server Socket Timed out...");
                        System.out.println("Returning to menu...");
                    }
                    break;
            }
        }
    }

    private static final String ESCAPE_CHARACTER = "q";
    private static final int HOST_SO_TIMEOUT = 10000;

    private Socket socket;
    private ServerSocket hostSocket;
    private String destinationAddress = "127.0.0.1";
    private int portNumber = 51638;
    private String username;

    boolean receiveMessagesThreadFlag = false;
    boolean sendMessagesThreadFlag = false;

    public ServiceMain() {

    }

    public ServiceMain(String s) {
        this.destinationAddress = s;
    }

    /**
     * Gets a port number from the user. Also does some basic checks to ensure it's acceptable.
     */
    protected void setPortNumber() {
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
    private void setDestinationAddress() {
        System.out.println();
        Scanner kb = new Scanner(System.in);
        System.out.print("Please enter your destination address/IP: ");
        destinationAddress = kb.nextLine();
    }

    /**
     * Attempts to connect to a socket and get an input and output stream.
     * @throws ClientHasNotConnectedException if it cannot connect to a host at the address provided
     */
    private void connectSocket() throws ClientHasNotConnectedException, InvalidSocketAddressException {
        int i = 0;
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
            if (i == 4) {
                throw new ClientHasNotConnectedException();
            }
        }
    }


    /**
     * Waits for a connection from another user and obtains input and output streams from that connection.
     */
    private void connectHostSocket() throws ClientHasNotConnectedException {
        try {
            hostSocket = new ServerSocket(portNumber);
            hostSocket.setSoTimeout(HOST_SO_TIMEOUT);
            System.out.println("Waiting for client to connect...");
            socket = hostSocket.accept();
            System.out.println("Client found....");
        } catch (IOException e) {
            throw new ClientHasNotConnectedException();
        }
    }

    /**
     * First this checks that the socket has some connections and that a username has been set.
     * Then gets input from the user's keyboard. Then it sends the username + the message to the connected
     * computer.
     * If the character 'q' is sent it means the user has finished sending messages.
     * at that time the user is informed and the program will exit..
     */
    private void sendMessages() {
        sendMessagesThreadFlag = true;
        try {
            if (socket == null) {
                System.out.println("You have not connected to a host...");
                System.out.println("Returning to menu...");
            } else {
                Scanner kb = new Scanner(System.in);
                String msg = "";
                PrintWriter sender = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                System.out.println("You may now enter your messages...");
                do  {
                    msg = kb.nextLine();
                    if (msg.length() > 0) {
                        sender.println(username + ": " + msg);
                        sender.flush();
                    }
                } while (!msg.equals(ESCAPE_CHARACTER) && receiveMessagesThreadFlag);
                if (msg.equals(ESCAPE_CHARACTER) ) {
                    System.out.println("Escape character detected... closing connection to client");
                    System.out.println("Returning to menu...");
                } else {
                    System.out.println();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessagesThreadFlag = false;
        System.out.println("flag change sending");
    }

    /**
     * Separates the message and username in the chat message.
     * @param chatMessage the entire string sent (<username>: <msg>)
     * @return the message part of the chat message.
     */
    private String extractMsg(String chatMessage) {
        int colonIndex = chatMessage.indexOf(":");
        String msg = chatMessage.substring(colonIndex + 1);
        return msg.trim();
    }

    /**
     * Is run in a separate thread. It continually waits for incoming messages while the main thread waits for user
     * input to send.
     * If the incoming message is null, this means that the connection has been terminated prematurely
     * and the method throws an exception and exits.
     */
    private void receiveMessages() {
        receiveMessagesThreadFlag = true;
        try {
            BufferedReader receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            msg = "<>";
            do {
                while (receiver.ready()) {
                    msg = receiver.readLine();
                    if (msg != null && !extractMsg(msg).equals(ESCAPE_CHARACTER)) {
                        System.out.println(msg);
                    } else if (msg == null)  {
                        System.out.println("Destination has terminated connection...");
                        System.out.println("Returning to menu...");
                        System.out.println("Press enter to return to menu...");
                    }
                }
            } while (!extractMsg(msg).equals(ESCAPE_CHARACTER) && sendMessagesThreadFlag);
            if (extractMsg(msg).equals(ESCAPE_CHARACTER)) {
                System.out.println("Escape Character Detected....");
                System.out.println("Other terminal has terminated connection...");
                System.out.println("Press enter twice to return to menu...");
            }
        } catch (IOException e) {
            System.out.println("Socket has not been connected...");
        }
        receiveMessagesThreadFlag = false;
    }

    public void run() {
        if (socket != null) {
            receiveMessages();
        }
    }


    private int menu() {
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

    private void setUsername() {
        System.out.print("Please Enter your username: ");
        Scanner kb = new Scanner(System.in);
        username = kb.nextLine();
    }

    /**
     * Adapted from https://gist.github.com/CarlEkerot/2693246
     *
     *
     */
    private void sendFile() {
        try {
            File myFile = null;
            FileInputStream fis = null;
            int i = 0; // counts how many times the user enters the wrong file address.
            do {
                try {
                    Scanner kb = new Scanner(System.in);
                    System.out.print("Enter the directory+name of the file you want to transfer: ");
                    String fileName = kb.nextLine();
                    myFile = new File(fileName);
                    fis = new FileInputStream(myFile);
                } catch (FileNotFoundException e) {
                    System.out.println("File does not exist, please try again...");
                    i++;
                    if (i == 4) {
                        System.out.println("Maximum attempts reached..");
                        System.out.println("Check the file exists and try again..");
                        System.out.println("Program closing..");
                        System.exit(-1);
                    }
                }
            } while (fis == null);
            byte[] buffer = new byte[4096];
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(myFile.getName().length());
            dos.writeChars(myFile.getName());

            dos.writeLong(myFile.length()); //sends the size of the file.

            while (fis.read(buffer) > 0) {
                dos.write(buffer);
            }
            System.out.println("File has been sent...");
            System.out.println("Program will now return to menu...");
        } catch (IOException e) {
            System.out.println("Connection Lost returning to menu...");
        }
    }

    private void receiveFile() {
        try {
            if (socket == null) {
                System.out.println("You have not connected to another user...");
                System.out.println("Returning to the menu...");
            } else {
                FileOutputStream fos = new FileOutputStream(getFileName());
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                long size = 0;
                size = dis.readLong();

                byte[] buffer = new byte[4096];
                int read = 0;
                int position = 0;
                int remaining = (int) size - read;
                while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                    position += read;
                    remaining -= read;
                    fos.write(buffer, 0, read);
                }
                dis.close();
                fos.close();
                System.out.println("File successfully received...");
                System.out.println("Program will now return to menu...");
            }
        } catch (IOException e) {
            System.out.println("Connection Lost returning to menu...");
        }

    }

    private String getFileName() throws IOException {

        DataInputStream dis = new DataInputStream(socket.getInputStream());
        char[] fileName = new char[dis.readInt()];
        for (int i = 0; i < fileName.length; i++) {
            fileName[i] = dis.readChar();
        }
        return new String(fileName);
    }
    private void closeSocket() {
        while (receiveMessagesThreadFlag || sendMessagesThreadFlag) {
        }
        try {
            socket.close();
            socket = null;
            if (hostSocket != null) {
                hostSocket.close();
                hostSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
