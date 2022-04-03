package SwimServer;

import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import IO.MockIO;
import IO.MockIOServer;

public class TestSwimServer {
    private class ServerRunner extends Thread{
        ServerRunner(){
            this.start();
        }

        public void run(){
            TestSwimServer.this.swimServer.ListenToIOServer();
        }
    }
    MockIOServer mockIOServer;
    SwimServer swimServer;
    ServerRunner serverRunner;

    @BeforeEach
    void SetUp(){
        mockIOServer = new MockIOServer();
        swimServer = new SwimServer(mockIOServer, 0);
        serverRunner = new ServerRunner();
    }

    @Test
    void AcceptingIOCreatesClientConnection(){
        MockIO mockIO = new MockIO();
        mockIOServer.AddToIOQueue(mockIO);

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert(swimServer.TestGetClientConnections().size() == 1);
    }

    @Test
    void NullMessageClosesConnection(){
        MockIO mockIO = new MockIO();
        mockIOServer.AddToIOQueue(mockIO);

        mockIO.AddNullMessageReceived();

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Queue<String> messagesSent = mockIO.GetMessagesSent();
        assert(messagesSent.size() == 0);
        assert(swimServer.TestGetClientConnections().size() == 0);
    }

    @Test
    void InvalidMessageReturnsErrorMessage(){
        MockIO mockIO = new MockIO();
        mockIOServer.AddToIOQueue(mockIO);

        mockIO.AddToMessagesReceivedQueue("\"get\"");

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Queue<String> messagesSent = mockIO.GetMessagesSent();
        assert(messagesSent.size() == 1);
        assert(messagesSent.peek().equals("\"\"get\"\" is not a valid command."));
    }

    @Test
    void InvalidCommandFieldReturnsErrorMessage(){
        MockIO mockIO = new MockIO();
        mockIOServer.AddToIOQueue(mockIO);

        mockIO.AddToMessagesReceivedQueue("{ \"command\": \"add\" }");

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Queue<String> messagesSent = mockIO.GetMessagesSent();
        assert(messagesSent.size() == 1);
        assert(messagesSent.peek().equals("\"{ \"command\": \"add\" }\" is not a valid command."));
    }

    @Test
    void GetCommandReturnsInitialValueOf0(){
        MockIO mockIO = new MockIO();
        mockIOServer.AddToIOQueue(mockIO);

        mockIO.AddToMessagesReceivedQueue("{ \"command\": \"get\" }");

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Queue<String> messagesSent = mockIO.GetMessagesSent();
        assert(messagesSent.size() == 1);
        assert(messagesSent.peek().equals("{ \"value\": 0 }"));
    }

    @Test
    void MissingSetValueReturnsErrorMessage(){
        MockIO mockIO = new MockIO();
        mockIOServer.AddToIOQueue(mockIO);

        mockIO.AddToMessagesReceivedQueue("{ \"command\": \"set\" }");

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Queue<String> messagesSent = mockIO.GetMessagesSent();
        assert(messagesSent.size() == 1);
        assert(messagesSent.peek().equals("\"{ \"command\": \"set\" }\" is not a valid command."));
    }
    
    @Test
    void SetWithPositiveCommandChangesServerValue(){
        MockIO mockIO = new MockIO();
        mockIOServer.AddToIOQueue(mockIO);

        mockIO.AddToMessagesReceivedQueue("{ \"command\": \"set\", \"value\": 5 }");

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert(swimServer.TestGetValue() == 5);
        Queue<String> messagesSent = mockIO.GetMessagesSent();
        assert(messagesSent.size() == 0);
    }
    
    @Test
    void SetWithNegativeCommandChangesServerValue(){
        MockIO mockIO = new MockIO();
        mockIOServer.AddToIOQueue(mockIO);

        mockIO.AddToMessagesReceivedQueue("{ \"command\": \"set\", \"value\": -10 }");

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert(swimServer.TestGetValue() == -10);
        Queue<String> messagesSent = mockIO.GetMessagesSent();
        assert(messagesSent.size() == 0);
    }
    
    @Test
    void SetCommandChangesValueReturnedByGet(){
        MockIO mockIO = new MockIO();
        mockIOServer.AddToIOQueue(mockIO);

        mockIO.AddToMessagesReceivedQueue("{ \"command\": \"set\", \"value\": 5 }");
        mockIO.AddToMessagesReceivedQueue("{ \"command\": \"get\" }");

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Queue<String> messagesSent = mockIO.GetMessagesSent();
        assert(messagesSent.size() == 1);
        assert(messagesSent.peek().equals("{ \"value\": 5 }"));
    }
    
    @Test
    void SubscriberReceivedMessagesWhenValueChangedBySelf(){
        MockIO mockIO = new MockIO();
        mockIOServer.AddToIOQueue(mockIO);

        mockIO.AddToMessagesReceivedQueue("{ \"command\": \"subscribe\" }");
        mockIO.AddToMessagesReceivedQueue("{ \"command\": \"set\", \"value\": 5 }");
        

        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Queue<String> messagesSent = mockIO.GetMessagesSent();
        assert(messagesSent.size() == 1);
        assert(messagesSent.peek().equals("{ \"value\": 5 }"));
    }
    
    @Test
    void SetCommandChangesValueReturnedToOtherByGet(){
        MockIO mockIO1 = new MockIO();
        mockIOServer.AddToIOQueue(mockIO1);
        MockIO mockIO2 = new MockIO();
        mockIOServer.AddToIOQueue(mockIO2);

        mockIO1.AddToMessagesReceivedQueue("{ \"command\": \"set\", \"value\": 5 }");
        
        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mockIO2.AddToMessagesReceivedQueue("{ \"command\": \"get\" }");
        
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Queue<String> messagesSent = mockIO2.GetMessagesSent();
        assert(messagesSent.size() == 1);
        assert(messagesSent.peek().equals("{ \"value\": 5 }"));
    }

    @Test
    void SubscriberReceivedMessagesWhenValueChangedByOther(){
        MockIO mockIO1 = new MockIO();
        mockIOServer.AddToIOQueue(mockIO1);
        MockIO mockIO2 = new MockIO();
        mockIOServer.AddToIOQueue(mockIO2);

        mockIO1.AddToMessagesReceivedQueue("{ \"command\": \"subscribe\" }");
        
        // Small pause to allow other threads to work.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mockIO2.AddToMessagesReceivedQueue("{ \"command\": \"set\", \"value\": 1 }");
        mockIO2.AddToMessagesReceivedQueue("{ \"command\": \"set\", \"value\": 2 }");
        mockIO2.AddToMessagesReceivedQueue("{ \"command\": \"set\", \"value\": 3 }");
        
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Queue<String> messagesSent = mockIO1.GetMessagesSent();
        assert(messagesSent.size() == 3);
        assert(messagesSent.poll().equals("{ \"value\": 1 }"));
        assert(messagesSent.poll().equals("{ \"value\": 2 }"));
        assert(messagesSent.poll().equals("{ \"value\": 3 }"));
    }
}
