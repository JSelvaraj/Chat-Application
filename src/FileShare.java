import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class FileShare  {

    private Socket socket;

    public FileShare(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * Adapted from https://gist.github.com/CarlEkerot/2693246
     *
     * This method uses a byte stream to send a file to a receiving user.
     * It follows a simple protocol:
     * 1. It sends the length of the filename over the outputstream.
     * 2. It sends the filename over the outputstream
     * 3. It sends the length of the file over the outputstream
     * 4. It sends the file over the filestream.
     *
     */
    public void sendFile() {
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

    /**
     * This method uses a bytestream to receive a file from another terminal. It first creates a FileOutputStream that
     * uses the helper function getFileName.
     * Then it follows the protocol:
     * 1. Reads a long off the inputstream, representing the length of the file being sent.
     * 2. Reads the inputstream for the length specified above, creating a new file at $PROGRAM DIRECTORY/src/<filename>
     *
     */
    public void receiveFile() {
        try {
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
        } catch (IOException e) {
            System.out.println("Connection Lost returning to menu...");
        }
    }

    /**
     * Helper function that:
     * 1. reads an int from the inputstream, representing the lenght of the filename
     * 2. Reads that many characters off the inputstream.
     * 3. Turns the resulting character array into a String.
     * @return filename - a String containing the filename of the file being sent.
     * @throws IOException If something interrupts the stream.
     */
    private String getFileName() throws IOException {

        DataInputStream dis = new DataInputStream(socket.getInputStream());
        char[] fileName = new char[dis.readInt()];
        for (int i = 0; i < fileName.length; i++) {
            fileName[i] = dis.readChar();
        }
        return new String(fileName);
    }


}
