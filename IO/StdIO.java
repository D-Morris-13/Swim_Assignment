package IO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StdIO implements IOInterface {
    public StdIO(){
        stdInBuffer = new BufferedReader(
            new InputStreamReader(System.in));
    }

    public String GetNextMessage(){
        String message = null;
        try {
            message = stdInBuffer.readLine();
        } catch (IOException e) {
            System.out.println("Exception caught while listening to StdIn.");
            System.out.println(e.getMessage());
        }
        return message;
    }

    public void SendMessage(String message){
        System.out.println(message);
    }

    public void EndIO(){}

    private BufferedReader stdInBuffer;
}
