import common.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Resources
 *
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
//        program.setUsername();
        while (choice != 7) {
            choice = program.menu();
            switch (choice) {
                case 1: program.setDestinationAddress();
                    break;
                case 2: program.setPortNumber();
                    break;
                case 3: program.connectSocket();
                    new Thread(program).start();
                    program.sendMessages();
                    break;
                case 4:
                    program.connectHostSocket();
                    new Thread(program).start();
                    program.sendMessages();
                    break;
                case 5:
                    program.connectSocket();
                    program.sendFile();
                    break;
                case 6:
                    program.connectHostSocket();
                    program.receiveFile();
                    break;
            }
        }
    }

    private Socket socket;
    private ServerSocket hostSocket;
    private String destinationAddress = "127.0.0.1";
    private int portNumber = 51638;
    private String username;

    private ServiceMain() {

    }

    private ServiceMain (String s) {
        this.destinationAddress = s;
    }

    /**
     * Gets a port number from the user. Also does some basic checks to ensure it's acceptable.
     */
    private void setPortNumber() {
        System.out.println();
        Scanner kb = new Scanner(System.in);
        int tempPort = 0;
        while (tempPort < 1023 || tempPort > 65535) {
            System.out.print("What is the port number of your destination: ");
            tempPort = kb.nextInt();
            if (tempPort < 1023 || tempPort > 65535) {
                System.out.println("Port numbers must be between 1024 and 65535");
                System.out.println();
            }
        }
        portNumber = tempPort;
    }

    /** Gets a destination address from the user.*/
    private void setDestinationAddress() {
        System.out.println();
        Scanner kb = new Scanner(System.in);
        System.out.print("Please enter your destination address/IP: ");
        destinationAddress = kb.nextLine();
    }

    /**
     * Attempts to connect to a socket and get an input and output stream.
     */
    private void connectSocket(){
        try {
            if (destinationAddress == null || portNumber < 1023 || portNumber > 65535) {
                throw new InvalidSocketAddressException();
            }
            System.out.println("Searching for server...");
            socket = new Socket(destinationAddress, portNumber);
            System.out.println("Server found...");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidSocketAddressException e) {
            System.out.println("Destination address and/or port number have are not valid.");
        }
    }


    /**
     * Waits for a connection from another user and obtains input and output streams from that connection.
     */
    private void connectHostSocket() {
        if (hostSocket == null) {
            try {
                hostSocket = new ServerSocket(portNumber);
                System.out.println("Waiting for client to connect...");
                socket = hostSocket.accept();
                System.out.println("Client found....");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * First this checks that the socket has some connections and that a username has been set.
     * Then gets input from the user's keyboard. Then it sends the username + the message to the connected
     * computer.
     * If the character 'q' is sent it means the user has finished sending messages.
     * at that time the user is informed and the program will exit..
     */
    private void sendMessages(){
        try {
            if (socket == null) {
                throw new ClientHasNotConnectedException();
            } else if (username == null) {
                throw new UsernameNotSetException();
            } else {
                Scanner kb = new Scanner(System.in);
                String msg = "";
                PrintWriter sender = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                System.out.println("You may now enter your messages...");
                while (!msg.equals("q")) {
                        msg = kb.nextLine();
                    if (!msg.equals("q")) {
                        sender.println(username + ": " + msg);
                        sender.flush();
                    }
                }
            }
            System.out.println("Escape character detected... closing connection to client");
            System.out.println("Terminating program");
            System.exit(0);
        } catch (ClientHasNotConnectedException e) {
            System.out.println("You have not connected to a host");
        } catch (UsernameNotSetException e) {
            System.out.println("You have not set a username");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        try {
            if (socket == null) {
                throw new ClientHasNotConnectedException();
            }
            BufferedReader receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            msg = "<>";
            do {
                msg = receiver.readLine();
                if (msg != null) {
                        System.out.println(msg);
                } else {
                    throw new ConnectionLostException();
                }
            } while (msg != null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (ConnectionLostException e) {
            System.out.println("Destination has terminated connection...");
            System.out.println("Program will now terminate...");
            System.exit(0);
        }

    }

    public void run() {
            receiveMessages();
    }

    private int menu() {
        Scanner kb = new Scanner(System.in);
        int choice = 0;
        System.out.println();
        System.out.println("----------------------------------------------------------------------");
        System.out.println("----------------------------------------------------------------------");
        System.out.println();
        System.out.println("Current Destination: " + (destinationAddress == null ? "Not Set": destinationAddress));
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
        System.out.println("5. Quit");
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
     */
    private void sendFile() {
        try {
            if (socket == null) {
                throw new ClientHasNotConnectedException();
            }
            Scanner kb = new Scanner(System.in);
            System.out.print("Enter the directory+name of the file you want to transfer: ");
            String fileName = kb.nextLine();
            File myFile = new File(fileName);
            FileInputStream fis = new FileInputStream(myFile);
            byte[] buffer = new byte[4096];
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(myFile.getName().length());
            dos.writeChars(myFile.getName());

            dos.writeLong(myFile.length()); //sends the size of the file.

            while (fis.read(buffer) > 0) {
                dos.write(buffer);
            }
            System.out.println("File has been sent...");
            System.out.println("Program will now terminate...");
            fis.close();
            dos.close();
            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile() {
        try {
            if (socket == null) {
                throw new ClientHasNotConnectedException();
            }
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            char[] fileName = new char[dis.readInt()];
            for (int i = 0; i < fileName.length; i++) {
                fileName[i] = dis.readChar();
            }
            String file = new String(fileName);
            FileOutputStream fos = new FileOutputStream(file);

            long size = 0;
            size = dis.readLong();

            byte[] buffer = new byte[4096];
            int read = 0;
            int position = 0;
            int remaining = (int) size - read;
            while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0){
                position += read;
                remaining -= read;
                fos.write(buffer, 0, read);
            }
            dis.close();
            fos.close();
            System.out.println("File successfully received...");
            System.out.println("Program will now terminate...");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
