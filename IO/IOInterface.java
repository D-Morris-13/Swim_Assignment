package IO;

public interface IOInterface {
    // Can block until message is received.
    public String GetNextMessage();

    public void SendMessage(String message);

    public void EndIO();
}
