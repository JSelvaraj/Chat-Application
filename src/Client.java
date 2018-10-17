import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private String destinationAddress;
    private int portNumber;


    public void getPortNumber() {
        System.out.println();
        Scanner kb = new Scanner(System.in);
        int tempPort = 0;
        while (tempPort < 1023 || tempPort > 65535) {
            System.out.print("What is the port number of your destination:");
            tempPort = kb.nextInt();
            if (tempPort < 1023 || tempPort > 65535) {
                System.out.println("Port numbers must be between 1024 and 65535");
                System.out.println();
            }
        }
        portNumber = tempPort;
    }
    public void getPortNumber(int number) {

    }
    public void getDestinationAddress() {

    }


}
