import common.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLOutput;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Resources
 */

public class ServiceMain {
    public static void main(String args[]) {

        ConnectionHandler connectionHandler;
        if (args.length == 1) {
            connectionHandler = new ConnectionHandler(args[0]);
        } else {
            connectionHandler = new ConnectionHandler();
        }
        connectionHandler.menu();
    }
}
