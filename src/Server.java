import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;


class Server {
    private int count = 0;
    private final int[] points = {0, 0};
    private int roundNo = 0;
    private final int[] choices = {-1, -1};
    private final ClientThread[] clientThreads = {null, null};
    private final Consumer<Consumer<ServerGUI>> callBack;
    private volatile boolean exit = false;
    private TheServer theServer;

    Server(int portNumber, int serverNo, Consumer<Consumer<ServerGUI>> callBack){
        this.callBack = callBack;
        theServer = new TheServer(portNumber, serverNo);
        theServer.start();
    }

    void close() {
        updateClients(-2);
        exit = true;
    }

    private void updateClients(Serializable message) {
        for(int i = 0; i < 2; i++) {
            if (clientThreads[i] != null) {
                try {
                    clientThreads[i].out.writeObject(message);
                }
                catch(Exception ignored) {}
            }
        }
    }

    class TheServer extends Thread {
        final int portNumber;
        final int serverNo;
        ServerSocket mySocket;

        TheServer(int portNumber, int serverNo) {
            this.portNumber = portNumber;
            this.serverNo = serverNo;
        }

        synchronized void  determineWinner() {
            if (choices[0] < 0 || choices[1] < 0)
                return;
            boolean iWon = false;
            int opp = choices[1];
            //ROCK = 0; PAPER = 1; SCISSORS = 2; LIZARD = 3; SPOCK = 4;
            switch (choices[0]) {
                case 0:
                    iWon = (opp == 2 || opp == 3); break;
                case 1:
                    iWon = (opp == 0 || opp == 4); break;
                case 2:
                    iWon = (opp == 3 || opp == 1); break;
                case 3:
                    iWon = (opp == 4 || opp == 1); break;
                case 4:
                    iWon = (opp == 2 || opp == 0); break;
            }
            if (iWon) {
                points[0] += 10;
                callBack.accept(e->e.updateWhoWon(0, true));
                callBack.accept(e->e.updateWhoWon(1, false));
            } else {
                points[1] += 10;
                callBack.accept(e->e.updateWhoWon(1, true));
                callBack.accept(e->e.updateWhoWon(0, false));
            }
            callBack.accept(e->e.updatePlayersScore(0, points[0]));
            callBack.accept(e->e.updatePlayersScore(1, points[1]));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            callBack.accept(e->e.updateRoundInfo(++roundNo));
            callBack.accept(e->e.resetPlayerInfo(0));
            callBack.accept(e->e.resetPlayerInfo(1));
            try {
                clientThreads[0].out.writeObject(new GameInfo(iWon, points[0], points[1], choices[0], choices[1]));
                clientThreads[1].out.writeObject(new GameInfo(!iWon, points[1], points[0], choices[1], choices[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }
            choices[0] = -1; choices[1] = -1;
        }

        public void run() {
            try {
                mySocket = new ServerSocket(portNumber);
                callBack.accept(e->e.stage.setScene(e.loadingScreen));
            } catch (IOException e) {
                System.out.println("Server socket did not launch");
                return;
            }
            while (!exit) {
                if (count < 2) {
                    roundNo = 0;
                    callBack.accept(e->e.updateRoundInfo(roundNo));
                    for (int i = 0; i < 2; ++i) {
                        if (clientThreads[i] == null) {
                            try {
                                clientThreads[i] = new ClientThread(mySocket.accept(), i);
                                int finalI = i;
                                callBack.accept(e-> e.playersJoined.getChildren().get(finalI).setVisible(true));
                                ++count;
                                clientThreads[i].start();
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                    if (count == 2) {
                        callBack.accept(e-> e.pause.play());
                    }
                }
            }
        }

    }

    class ClientThread extends Thread{
        int clientNo;
        ObjectInputStream in;
        ObjectOutputStream out;

        ClientThread(Socket connection, int clientNo){
            this.clientNo = clientNo;
            try {
                in = new ObjectInputStream(connection.getInputStream());
                out = new ObjectOutputStream(connection.getOutputStream());
                connection.setTcpNoDelay(true);
            }
            catch(Exception e) {
                System.out.println("Streams not open");
            }
        }
        void doExit() {
            clientThreads[clientNo] = null;
            --count;
            updateClients(-1);
            callBack.accept(e->e.updateConnection(clientNo));
            System.out.println("Client #" + clientNo + " has left the server!");
        }

        public void run(){
            updateClients(count == 2);
            while(!exit) {
                try {
                    int i = (Integer) in.readObject();
                    if (i == -2) {
                        doExit();
                        break;
                    }
                    choices[clientNo] = i;
                    callBack.accept(e->e.updatePlayerPlayed(clientNo, i));
                    theServer.determineWinner();
                }
                catch(Exception e) {
                    doExit();
                    break;
                }
            }
        }//end of run
    }//end of client thread
}






