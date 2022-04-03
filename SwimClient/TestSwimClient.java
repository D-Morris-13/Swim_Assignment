package SwimClient;

import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import IO.MockIO;

public class TestSwimClient {
    private class ClientRunner extends Thread{
        ClientRunner(){
            this.start();
        }

        public void run(){
            TestSwimClient.this.swimClient.ListenToStdIO();
        }
    }
    MockIO mockNetworkIO;
    MockIO mockStdIO;
    SwimClient swimClient;
    ClientRunner clientRunner;

    @BeforeEach
    void SetUp(){
        mockNetworkIO = new MockIO();
        mockStdIO = new MockIO();
        swimClient = new SwimClient(mockNetworkIO, mockStdIO);
        clientRunner = new ClientRunner();
    }

    @Test
    void InputToStdInIsSentAcrossNetwork(){
        mockStdIO.AddToMessagesReceivedQueue("Test message.");
        
        // Small pause to allow other threads to work.
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Queue<String> messagesSent = mockNetworkIO.GetMessagesSent();
        assert(messagesSent.size() == 1);
        assert(messagesSent.poll().equals("Test message."));
    }

    @Test
    void MessageFromNetworkIsSentToStdOut(){
        mockNetworkIO.AddToMessagesReceivedQueue("Test message.");

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Queue<String> messageWritten = mockStdIO.GetMessagesSent();
        assert(messageWritten.size() == 1);
        assert(messageWritten.poll().equals("Test message."));
    }

    @Test
    void NullMessageClosesClientListener(){
        mockNetworkIO.AddNullMessageReceived();

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert(swimClient.TestGeNetworkListener().isAlive() == false);
    }

    @Test
    void NullMessageClosesPortAfterNextInput(){
        mockNetworkIO.AddNullMessageReceived();

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mockStdIO.AddToMessagesReceivedQueue("Test message.");

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert(mockNetworkIO.EndCalled());
    }

    @Test
    void NullMessageEndsClientAfterNextInput(){
        mockNetworkIO.AddNullMessageReceived();

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mockStdIO.AddToMessagesReceivedQueue("Test message.");

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert(clientRunner.isAlive() == false);
    }
}
