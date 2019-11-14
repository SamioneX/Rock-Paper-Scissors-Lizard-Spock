import javafx.scene.control.Button;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.function.Consumer;

public class Client extends Thread{
    private ObjectOutputStream out;
    private String ipAddress;
    private int portNumber;
    volatile boolean exit = false;
    private Consumer<Consumer<ClientGUI>> callBack;

    Client(String ipAddress, int portNumber, Consumer<Consumer<ClientGUI>> callBack){
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.callBack = callBack;
    }
    Client(Server.TheServer s, Consumer<Consumer<ClientGUI>> callBack) {
        this.callBack = callBack;
        try {
            this.ipAddress = InetAddress.getLocalHost().getHostAddress();
            this.portNumber = s.portNumber;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    void close() {
        exit = true;
        send(-2);
    }

    public void run() {
        ObjectInputStream in;
        try {
            Socket socketClient = new Socket(ipAddress, portNumber);
            callBack.accept(e->e.stage.setScene(e.loadingScreen));
            out = new ObjectOutputStream(socketClient.getOutputStream());
            in = new ObjectInputStream(socketClient.getInputStream());
            socketClient.setTcpNoDelay(true);
        } catch (Exception e) {
            callBack.accept(ClientGUI::updateSceneOnServerClose);
            System.out.println("Client socket did not launch");
            return;
        }
        while (!exit) {
            try {
                Object o = in.readObject();
                if (o instanceof Integer && (int)o == -2) {
                    callBack.accept(ClientGUI::updateSceneOnServerClose);
                    return;
                }
                if (((Boolean) o)) {
                    callBack.accept(ClientGUI::updateOppThick);
                    break;
                }
            } catch (Exception ignored) {}
        }
        while (!exit) {
            try {
                Object o = in.readObject();
                if (o instanceof Boolean) {
                    if ((Boolean)o)
                        callBack.accept(ClientGUI::updateOppThick);
                }
                else if (o instanceof Integer) {
                    int i = (int) o;
                    if (i == -2) {
                        callBack.accept(ClientGUI::updateSceneOnServerClose);
                        break;
                    }
                    else if (i == -1)
                        callBack.accept(ClientGUI::doOppDisconnected);
                }
                else
                    callBack.accept(e-> e.displayResult((GameInfo) o));
            } catch (Exception e) {
                break;
            }
        }
    }
    void send(int data) {
        try {
            out.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
