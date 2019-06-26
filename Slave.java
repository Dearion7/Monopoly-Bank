import javax.websocket.*;
import java.net.URI;
import java.io.IOException;

@ClientEndpoint
public class Slave implements Runnable {
    private static Object waitLock = new Object ();
    private String[] valuables;
    private Database database;
    private String IDRecBank = "";
    Communication comm = new Communication();
    
    Slave(Database database) {
        this.database = database;
    }
    
    @OnMessage
    public void onMessage( String message, Session session) {
        if (session != null) {
            System.out.println ("Received slave msg: " + message);
            valuables = message.split("\"");
            if (!(message.equals("true")) && !(message.equals("false"))) {
                switch (valuables.length) {
                    case 25:
                        sendMessage(session, withdrawDatabase());
                        break;
                    case 21:
                        sendMessage(session, pinCheckDatabase());
                        break;
                    default:
                        System.out.println("no variables available");
                        System.out.println(valuables.length);
                        break;
                }
            }
        }
    }
    
    public void sendMessage(Session session, String result) {
        try {
            System.out.println("Result: " + result);
            session.getAsyncRemote().sendText(result);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    
    public String pinCheckDatabase() {
        String IDSendBank = valuables[3];
        String accountCheck = valuables[5];
        String IBAN = valuables[7];
        String Pin = valuables[11];
        String Func = valuables[15];
        String IDRecBank = valuables[19];
        System.out.println(IDSendBank + " ask for a pin check.");
        if (!(database.checkBlocked(IBAN))) {
            if (database.checkPin(IBAN, Integer.parseInt(Pin))) {
                return "true";
            } else {
                return "false";
            }
        } else {
            return "false";
        }
    }

    public String withdrawDatabase() {
        String IDSendBank = valuables[3];
        String IBAN = valuables[7];
        String Pin = valuables[11];
        String Amount = valuables[19];
        int amount = Integer.parseInt(Amount);
        System.out.println(IDSendBank + " ask for a withdraw");
        if (database.checkSaldo(IBAN, Integer.parseInt(Pin)) >= amount) {
            if (database.withdraw(IBAN, Integer.parseInt(Pin), amount)) {
                return "true";
            }
        }
        return "false";
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
            session.getAsyncRemote () .sendText ("[\"register\", \"slave\", \"SUMYBK\"]");
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
