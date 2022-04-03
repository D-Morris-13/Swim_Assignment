package IO;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

public class TestNetworkIO {
    class NetworkIOPair{
        NetworkIOPair(NetworkIOServer ioServer, int portNumber){
            try {
                Socket clientSocket = new Socket(InetAddress.getLoopbackAddress(), portNumber);
                serverInterface = ioServer.AcceptConnection();
                clientInterface = new NetworkIO(clientSocket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        IOInterface serverInterface;
        IOInterface clientInterface;
    }

    @Test
    void MessageSentFromClientIsReceivedByServer(){
        final int portNumber = 8080;
        NetworkIOServer ioServer = new NetworkIOServer(portNumber);
        NetworkIOPair ioPair = new NetworkIOPair(ioServer, portNumber);

        String clientMessage = "Test message.";
        ioPair.clientInterface.SendMessage(clientMessage);

        String serverMessage = ioPair.serverInterface.GetNextMessage();
        assert(serverMessage.equals(clientMessage));
    }

    @Test
    void MessagesSentFromClientAreAllReceivedByServer(){
        final int portNumber = 8081;
        NetworkIOServer ioServer = new NetworkIOServer(portNumber);
        NetworkIOPair ioPair = new NetworkIOPair(ioServer, portNumber);

        // Use a HashSet as the order messages are received is not
        // generally guaranteed to be consistent.
        HashSet<String> clientMessages = new HashSet<>();
        for(int i = 0; i < 10; ++i){
            String clientMessage = "i = " + i;
            clientMessages.add(clientMessage);
            ioPair.clientInterface.SendMessage(clientMessage);
        }

        HashSet<String> serverMessages = new HashSet<>();
        for(int i = 0; i < 10; ++i){
            String serverMessage = ioPair.serverInterface.GetNextMessage();
            serverMessages.add(serverMessage);
        }
        
        assert(clientMessages.equals(serverMessages));
    }

    @Test
    void ClosingClientSendsNullToServer(){
        final int portNumber = 8082;
        NetworkIOServer ioServer = new NetworkIOServer(portNumber);
        NetworkIOPair ioPair = new NetworkIOPair(ioServer, portNumber);

        ioPair.clientInterface.EndIO();

        String serverMessage = ioPair.serverInterface.GetNextMessage();
        
        assert(serverMessage == null);
    }
    
    class IOInterfaceListener extends Thread{
        IOInterfaceListener(IOInterface clientInterface){
            this.clientInterface = clientInterface;

            this.start();
        }

        public void run(){
            while(true){
                clientInterface.GetNextMessage();
            }
        }

        IOInterface clientInterface;
    }

    @Test
    void ClientCanSendMessageWhileAnotherThreadListens(){
        final int portNumber = 8083;
        NetworkIOServer ioServer = new NetworkIOServer(portNumber);
        NetworkIOPair ioPair = new NetworkIOPair(ioServer, portNumber);

        IOInterfaceListener listener = new IOInterfaceListener(ioPair.clientInterface);

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String clientMessage = "Test message.";
        ioPair.clientInterface.SendMessage(clientMessage);

        String serverMessage = ioPair.serverInterface.GetNextMessage();
        assert(serverMessage.equals(clientMessage));
        assert(listener.isAlive());
    }

}
