package IO;

import java.net.ServerSocket;
import java.net.Socket;

public class NetworkIOServer implements IOServerInterface {
    public NetworkIOServer(int portNumber){
        try{
            ServerSocket serverSocket = new ServerSocket(portNumber);  
            this.serverSocket = serverSocket;
        }catch (Exception e) {
            System.out.println("Exception caught when opening server socket.");
            System.out.println(e.getMessage());
        }
    }

    public IOInterface AcceptConnection(){
        IOInterface connection = null;
        try{
            Socket clientSocket = serverSocket.accept(); 
            connection = new NetworkIO(clientSocket);
        }catch (Exception e) {
            System.out.println("Exception caught whilst trying to listen for a connection");
            System.out.println(e.getMessage());
        }
        return connection;
    }

    private ServerSocket serverSocket;
}
