package IO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkIO implements IOInterface {
    public NetworkIO(Socket socket) throws IOException{
        this.socket = socket;

        listeningBuffer = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));

        sendingWriter =
            new PrintWriter(socket.getOutputStream(), true);
    }

    public String GetNextMessage(){
        String message = null;
        try{
            message = listeningBuffer.readLine();
        }catch(IOException e){
            System.out.println("Exception caught when reading from network.");
            System.out.println(e.getMessage());
        }

        return message;
    }

    public void SendMessage(String message){
        try{
            sendingWriter.println(message);
        }catch (Exception e) {
            System.out.println("Exception caught while writing to network.");
            System.out.println(e.getMessage());
        }
    }

    public void EndIO(){
        if(!socket.isClosed()){
            try{
                socket.close();
            }catch(IOException e){
                System.out.println("Exception caught when closing socket.");
                System.out.println(e.getMessage());
            }
        }
    }

    private Socket socket;
    // Closing an opened input or output stream closes the socked, so
    // the reader and writer need to live as long as the socket.
    private BufferedReader listeningBuffer;
    private PrintWriter sendingWriter;
}
