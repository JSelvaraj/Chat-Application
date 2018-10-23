import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Multicast {
    private MulticastSocket socket;

    public void joingroup(InetAddress address, int portNumber) {
        try {
            socket = new MulticastSocket(portNumber);
            socket.joinGroup(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
