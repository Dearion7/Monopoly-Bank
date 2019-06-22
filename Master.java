import javax.websocket.*;
import java.net.URI;
import org.json.simple.JSONObject;

@ClientEndpoint
public class Master implements Runnable {

    private static Object waitLock = new Object ();

    private String bankName = "SUMYBK";
    private String iban = "SU74MYBK320461";
    private String pin = "5246";
    private String amount = "10";

    /* Setters */
    public void setAmount(double amount) {
        this.amount = String.valueOf(amount);
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    /* NOOB server connection */
    @SuppressWarnings("unchecked")
    public JSONObject withdraw() {
        JSONObject object = new JSONObject();

        object.put("IDRecBank", bankName);
        object.put("IDRecBank", bankName);
        object.put("IDSenBank", "SUMYBK");
        object.put("Func", "withdraw");
        object.put("IBAN", iban);
        object.put("PIN", pin);
        object.put("Amount", amount);

        return object;
    }
    public JSONObject checkPin() {
        JSONObject object = new JSONObject();

        object.put("IDRecBank", bankName);
        object.put("IDSenBank", "SUMYBK");
        object.put("Func", "checkPin");
        object.put("IBAN", iban);
        object.put("PIN", pin);

        return object;
    }

    @OnMessage
    public void onMessage( String message) {
        System.out.println ("Received msg: " + message);
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
            container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer (this, URI.create ("ws://145.24.222.24:8080"));
            session.getAsyncRemote () .sendText ("[\"register\", \"master\", \"SUMYBK\"]");
            session.getAsyncRemote().sendText("[\"COBBCO\"," + withdraw() + "]");
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
