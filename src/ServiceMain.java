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
        program.setUsername();
        while (choice != 5) {
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
            }
        }
    }

    private Socket socket;
    private ServerSocket hostSocket;
    private String destinationAddress;
    private int portNumber;
    private String username;

    private InputStream reader;
    private OutputStream writer;

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
            socket = new Socket(destinationAddress, portNumber);
            //socket.setSoTimeout(soTimeout);
            reader = socket.getInputStream();
            writer = socket.getOutputStream();
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
                socket = hostSocket.accept();
                reader = socket.getInputStream();
                writer = socket.getOutputStream();
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
                PrintWriter sender = new PrintWriter(new OutputStreamWriter(writer), true);
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
        }
    }

    private void receiveMessages() {
        try {
            if (socket == null) {
                throw new ClientHasNotConnectedException();
            }
            BufferedReader receiver = new BufferedReader(new InputStreamReader(reader));
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
        System.out.println("5. Quit");
        System.out.println();
        while (choice < 1 || choice > 4) {
            System.out.println();
            System.out.print("Choose an Option 1/2/3/4/5: ");
            try {
                choice = kb.nextInt();
            } catch (InputMismatchException e) {
                System.out.println();
                System.out.println("Usage: 1/2/3/4/5");
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
        System.out.print("Please Enter your username:");
        Scanner kb = new Scanner(System.in);
        username = kb.nextLine();
    }

    /**
     * Adapted from http://www.rgagnon.com/javadetails/java-0542.html
     */
    private void sendFile() {
        if (!socket.isConnected()) {
            throw new ClientHasNotConnectedException();
        }
        String directory;
        Scanner kb = new Scanner(System.in);
        System.out.print("Please enter the directory/filename of the file you wish to transfer: ");
        directory = kb.nextLine();

        try {
            File myFile = new File(directory);
            byte[] fileByteArray = new byte[(int)myFile.length()];
            FileInputStream fileReader = new FileInputStream(myFile);
            BufferedInputStream bufferedFileReader = new BufferedInputStream(fileReader);
            bufferedFileReader.read(fileByteArray, 0, fileByteArray.length);
            System.out.println("Sending" + directory + "(" + fileByteArray.length + " bytes)");
            writer.write(fileByteArray, 0, fileByteArray.length);
            writer.flush();
            System.out.println("Complete...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile() {
        if (!socket.isConnected()) {
            throw new ClientHasNotConnectedException();
        }
        String directory;
        Scanner kb = new Scanner(System.in);
        System.out.print("Please enter the directory/filename you wish to save a file to: ");
        directory = kb.nextLine();

        try {
            byte[] byteArray = new byte[800000];
            FileOutputStream fileWriter = new FileOutputStream(directory);
            BufferedOutputStream bufferedFileWriter = new BufferedOutputStream(fileWriter);
            int bytesRead = reader.read(byteArray, 0, byteArray.length);
            int current = bytesRead;

            do {
                bytesRead = reader.read(byteArray, current, (byteArray.length-current));
                if (bytesRead >= 0) current += bytesRead;
            } while (bytesRead > -1);

            bufferedFileWriter.write(byteArray, 0, current);
            bufferedFileWriter.flush();
            System.out.println("File " + directory + " downloaded (" + current + " bytes read)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
