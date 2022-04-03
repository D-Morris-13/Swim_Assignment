package SwimServer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import IO.IOInterface;
import IO.IOServerInterface;
import IO.NetworkIOServer;

public class SwimServer{
    public static void main(String[] args) {
        SwimServer server = new SwimServer(new NetworkIOServer(8080), 0);

        server.ListenToIOServer();
    }

    public SwimServer(IOServerInterface serverInterface, int initialValue){
        System.out.println("Started server");

        this.serverInterface = serverInterface;
        this.clientConnections = new LinkedList<ClientConnection>();
        this.value = initialValue;
        this.connectionListLock = new ReentrantLock(true);
    }

    public void ListenToIOServer(){
        while(true){
            IOInterface clientIO = serverInterface.AcceptConnection();
            ClientConnection connection = new ClientConnection(clientIO);
            clientConnections.add(connection);
        }
    }

    class ClientConnection extends Thread{
        public ClientConnection(IOInterface clientIO){
            System.out.println("Connected to new client");

            this.clientIO = clientIO;
            this.isSubscribed = false;

            this.start();
        }

        // Thread monitoring network connection for incoming commands.
        public void run(){
            String message;
            while((message = clientIO.GetNextMessage()) != null){
                this.ParseMessage(message);
            }
            System.out.println("Connection closed.");
            clientIO.EndIO();
            SwimServer.this.CloseConnection(this);
        }

        public boolean IsSubscribed(){
            return this.isSubscribed;
        }

        private void ParseMessage(String message){
            System.out.println(String.format("Recieved message: %s", message));

            Pattern commandPattern = Pattern.compile("\\{ \"command\": \"([A-Za-z]+)\"(, \"value\": -?[0-9]+)? \\}");
            Matcher commandMatcher = commandPattern.matcher(message);
            if(commandMatcher.find()){
                String command = commandMatcher.group(1);

                if(command.equals("set")){
                    String valueMessage = commandMatcher.group(2);
                    if(valueMessage == null){
                        SendErrorMessage(message);
                    }else{
                        this.ParseSetCommandValue(valueMessage);
                    }
                }else if(command.equals("get")){
                    this.SendValue(SwimServer.this.value);
                }else if(command.equals("subscribe")){
                    isSubscribed = true;
                }else{
                    SendErrorMessage(message);
                }
            }else{
                SendErrorMessage(message);
            }
        }

        private void ParseSetCommandValue(String message){
            Pattern valuePattern = Pattern.compile("(-?[0-9]+)");
            Matcher valueMatcher = valuePattern.matcher(message);
            valueMatcher.find();
            int value = Integer.parseInt(valueMatcher.group(1));

            SwimServer.this.SetValue(value);
        }

        void SendValue(int value){
            clientIO.SendMessage(CreateValueMessage(value));
        }

        void SendErrorMessage(String message){
            String errorMessage = String.format("\"%s\" is not a valid command.", message);
            clientIO.SendMessage(errorMessage);
        }

        private String CreateValueMessage(int value){
            return String.format("{ \"value\": %d }", value);
        }

        private IOInterface clientIO;
        private boolean isSubscribed;
    }

    private IOServerInterface serverInterface;
    private List<ClientConnection> clientConnections;
    private Lock connectionListLock;

    private int value;

    // SetValue and CloseConnection may be called by any ClientConnection thread.
    // SetValue needs alone needs a lock to ensure that the most recent value received
    // by a subscriber is the current server value.
    // CloseConnection needs to share the lock to avoid having one thread editing
    // the connections list as another is iterating over it.
    private void SetValue(int value){
        connectionListLock.lock();
        this.value = value;

        for(ClientConnection clientConnection : clientConnections){
            if(clientConnection.IsSubscribed()){
                clientConnection.SendValue(value);
            }
        }
        connectionListLock.unlock();
    }

    private void CloseConnection(ClientConnection clientConnection){
        connectionListLock.lock();
        clientConnections.remove(clientConnection);
        connectionListLock.unlock();
    }

    List<ClientConnection> TestGetClientConnections(){
        return clientConnections;
    }

    int TestGetValue(){
        return value;
    }
}