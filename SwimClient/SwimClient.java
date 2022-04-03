package SwimClient;

import java.io.IOException;
import java.net.Socket;

import IO.IOInterface;
import IO.NetworkIO;
import IO.StdIO;

public class SwimClient {
    public static void main(String[] args) {
        String hostName = "127.0.0.1";// args[0];
        int portNumber = 8080;// Integer.parseInt(args[1]);

        RunClient(hostName, portNumber);
    }

    public static void RunClient(String hostName, int portNumber){
        try{
            Socket socket = new Socket(hostName, portNumber);
            IOInterface networkIO = new NetworkIO(socket);
            IOInterface stdIO = new StdIO();

            SwimClient client = new SwimClient(networkIO, stdIO);

            client.ListenToStdIO();
        }catch (IOException e) {
            System.out.println("Exception caught when connect to port "
                + portNumber);
            System.out.println(e.getMessage());
        }
    }

    public SwimClient(IOInterface networkIO, IOInterface stdIO){
        this.networkIO = networkIO;
        this.stdIO = stdIO;

        this.listener = new NetworkListener();
    }

    public void ListenToStdIO(){
        while(listener.isAlive()){
            String message = stdIO.GetNextMessage();
            networkIO.SendMessage(message);
        }
        OutputCloseNotice();
        networkIO.EndIO();
    }

    void OutputCloseNotice(){
        stdIO.SendMessage("Connection closed by server.");
    }

    class NetworkListener extends Thread{
        private NetworkListener(){
            this.start();
        }

        public void run(){
            String input = null;
            while((input = SwimClient.this.networkIO.GetNextMessage()) != null){
                if(!input.isEmpty()){
                    stdIO.SendMessage(input);
                }
            }
        }
    }

    private IOInterface networkIO;
    private IOInterface stdIO;
    private NetworkListener listener;

    NetworkListener TestGeNetworkListener(){
        return listener;
    }
}
