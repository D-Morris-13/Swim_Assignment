package IO;

import java.util.LinkedList;
import java.util.Queue;

public class MockIO implements IOInterface {
    public MockIO(){
        this.messagesReceived = new LinkedList<>();
        this.messagesSent = new LinkedList<>();
        this.messagesReceivedCount = 0;
        this.endCalled = false;
    }

    public void AddToMessagesReceivedQueue(String message){
        synchronized(this){
            messagesReceived.add(message);
            ++messagesReceivedCount;
            this.notify();
        }

    }

    // Will effectively be the last message in the queue
    public void AddNullMessageReceived(){
        synchronized(this){
            ++messagesReceivedCount;
            this.notify();
        }
    }

    // Want to be able to simulate sending null messages, so a BlockingQueue is not suitable.
    public String GetNextMessage(){
        synchronized(this){
            if(messagesReceivedCount == 0){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            --messagesReceivedCount;
            return messagesReceived.poll();
        }
    }

    public void SendMessage(String message){
        messagesSent.add(message);
    }

    public Queue<String> GetMessagesSent(){
        return messagesSent;
    }

    public void EndIO(){
        endCalled = true;
    }

    public boolean EndCalled(){
        return endCalled;
    }

    private Queue<String> messagesReceived;
    private Queue<String> messagesSent;
    private int messagesReceivedCount;
    private boolean endCalled;
}
