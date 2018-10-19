
import java.io.*;
import java.net.*;

/**
 * Simple, non-blocking TCP client with input from keybaord,
 * using socket timeout.
 * Saleem Bhatti, https://saleem.host.cs.st-andrews.ac.uk/
 * September 2018
 * February 2007
 *
 */
public class TcpClientSimpleNB {
	static int sleepTime_ = 5000; // milliseconds
	static int bufferSize_ = 80; // a line
	static int soTimeout_ = 10; // milliseconds

	public static void main(String[] args) {
		if (args.length != 2) { // user has not provided arguments
			System.out.println("\n TcpClientSimpleNB <servername> <portnumber> \n");
			System.exit(0);
		}

		try {
			Socket       connection;
			OutputStream tx;
			InputStream	 rx;
			byte[]       buffer;
			int          b ;

			connection = startClient(args[0], args[1]);
			tx = connection.getOutputStream();
			rx = connection.getInputStream();
			b = 0;

			System.out.print("You have " + sleepTime_ + " milliseconds to type something -> ");
			Thread.sleep(sleepTime_); // wait

			buffer = new byte[bufferSize_];
			if (System.in.available() > 0) {
				b = System.in.read(buffer); // keyboard
			}

			if (b > 0) {
				tx.write(buffer, 0, b); // send to server
				System.out.println("Sending " + b + " bytes");
			}

			Thread.sleep(sleepTime_); // wait

			buffer = new byte[bufferSize_];
			b = rx.read(buffer); // from server
			if (b > 0) {
				String s = new String(buffer); /// assume it is a printable string
				System.out.println("Received " + b + " bytes --> " + s);
			}

			connection.close();
		}

		catch (SocketTimeoutException e) {
			// no incoming data - just ignore
		}
		catch (InterruptedException e) {
			System.err.println("Interrupted Exception: " + e.getMessage());
		}
		catch (IOException e) {
			System.err.println("IO Exception: " + e.getMessage());
		}
	} // main()

	static Socket startClient(String hostname, String portnumber) {
		Socket connection = null;

		try {
			InetAddress address;
			int         port;

			address = InetAddress.getByName(hostname);
			port = Integer.parseInt(portnumber);

			connection = new Socket(address, port); // server
			connection.setSoTimeout(soTimeout_);

			System.out.println("--* Connecting to " + connection.toString());
		}

		catch (IOException e) {
			System.err.println("IO Exception: " + e.getMessage());
		}

		return connection;
	}

}
