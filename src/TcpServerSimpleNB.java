
import java.io.*;
import java.net.*;

/**
 * Simple, non-blocking TCP server, checks before reading
 * <p>
 * Saleem Bhatti, https://saleem.host.cs.st-andrews.ac.uk/
 * September 2018
 * February 2007
 */
public class TcpServerSimpleNB {

    static int port_ = ZZZZ; // You need to change this!
    static ServerSocket server_;
    static int sleepTime_ = 100; // milliseconds
    static int bufferSize_ = 80; // a line
    static int soTimeout_ = 10; // 10 ms

    public static void main(String[] args) {
        startServer();

        try {
            Socket connection;
            OutputStream tx;
            InputStream rx;

            connection = server_.accept(); // waits for connection
            tx = connection.getOutputStream();
            rx = connection.getInputStream();
            server_.close(); // no need to wait now

            System.out.println("New connection ... " +
                    connection.getInetAddress().getHostName() + ":" +
                    connection.getPort());

            byte[] buffer = new byte[bufferSize_];
            int b = 0;
            while (b < 1) {
                Thread.sleep(sleepTime_);

                buffer = new byte[bufferSize_];
                b = rx.read(buffer);
            }

            if (b > 0) {
                String s = new String(buffer);
                System.out.println("Received " + b + " bytes --> " + s);

                System.out.println("Sending " + b + " bytes");
                tx.write(buffer, 0, b); // send data back to client

                connection.close(); // finished
            }

        } catch (SocketTimeoutException e) {
            // no incoming data - just ignore
        } catch (InterruptedException e) {
            System.err.println("Interrupted Exception: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }
    }


    public static void startServer() {
        try {
            server_ = new ServerSocket(port_); // make a socket
            System.out.println("--* Starting server " + server_.toString());
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }
    }

    protected void finalize() { // tidy up when program ends
        try {
            server_.close();
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }
    }

}
