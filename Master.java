import javax.websocket.*;
import java.net.URI;

@ClientEndpoint
public class Slave implements Runnable {
    private static Object waitLock = new Object ();

    @OnMessage
    public void onMessage( String message) {
        System.out.println ("Received slave msg: " + message);
    }

    private static void  waitForTerminationSignal () {
        synchronized (waitLock) {
            try {
                waitLock.wait ();
            }
            catch (InterruptedException exception) {
                exception.printStackTrace ();
            }
        }
    }

    @Override
    public void run() {
        WebSocketContainer container = null;//
        Session session = null;
        try{
            container = ContainerProvider.getWebSocketContainer ();
            session = container.connectToServer (this, URI.create ("ws://145.24.222.24:8080"));
            session.getAsyncRemote () .sendText ("[\"register\", \"slave\", \"MYBK\"]");
            waitForTerminationSignal ();
        }
        catch (Exception exception) {
            exception.printStackTrace ();
        }
        finally {
            if (session != null) {
                try {
                    session.close ();
                }
                catch (Exception exception) {
                    exception.printStackTrace ();
                }
            }
        }
    }
}
