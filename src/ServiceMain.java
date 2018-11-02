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

        MenuUI MenuUI;
        if (args.length == 1) {
            MenuUI = new MenuUI(args[0]);
        } else {
            MenuUI = new MenuUI();
        }
        MenuUI.menu();
    }
}
