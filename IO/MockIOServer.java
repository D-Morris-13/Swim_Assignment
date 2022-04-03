package IO;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MockIOServer implements IOServerInterface {
    public MockIOServer(){
        this.ioQueue = new LinkedBlockingQueue<IOInterface>();
    }

    public void AddToIOQueue(IOInterface ioInterface){
        ioQueue.add(ioInterface);
    }

    public IOInterface AcceptConnection(){
        IOInterface ioInterface = null;
        try{
            ioInterface = ioQueue.take();
        }catch(Exception e) {
            System.out.println("Exception caught when opening socket input stream.");
            System.out.println(e.getMessage());
        }
        return ioInterface;
    }

    private BlockingQueue<IOInterface> ioQueue;
}
