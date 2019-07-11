import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.json.simple.*;

public class ClientHandler extends Thread {

    /* Server connection */
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket socket;
    
    /* Database connection */
    final Database database = new Database();
    Communication comm = new Communication();

    /* Noob connection */
    Master master;

    ClientHandler(Socket socket, DataInputStream dis, DataOutputStream dos, Master master) {
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        this.master = master;    
    }

    @Override
    public void run() {
        String message;
        boolean ownCard = true;
        JSONObject object;
        String ibanCheck = null;
        while (true) {
            try {
                // Get message from cliets
                message = dis.readUTF();
		System.out.println(message);
                // close connection when client send "EXIT"
                if (message.equals("Exit")) {
                    System.out.println("Closing client");
                    socket.close();
                    break;
                }

                // write on output stream based on the answer from the client
                switch (message) {
                    case "iban":
                        comm.setIban(dis.readUTF());
                        //System.out.println(dis.readUTF());
			String landCode = comm.getIban().substring(0, 2);
                        String bankCode = comm.getIban().substring(4, 8);
                        ibanCheck = landCode + bankCode;
			ibanCheck = ibanCheck.toLowerCase();
                        System.out.println(ibanCheck);
                        if (ibanCheck.equals("sumybk")) {
                            dos.writeBoolean(database.checkIban(comm.getIban()));
                            break;
                        } else {
                            master.setBankName(ibanCheck);
                            master.setIban(comm.getIban());
                            dos.writeBoolean(true);
                            break;
                        }
                    case "pin":
                        boolean checked;
                        comm.setPin(dis.readInt());
                        if (ibanCheck.equals("sumybk")) {
                            checked = database.checkPin(comm.getIban(), comm.getPin());
                        } else {
                            master.setPin(String.valueOf(comm.getPin()));
                            object = master.checkPin();
                            master.sendText(ibanCheck, object);
                            checked = Boolean.parseBoolean(master.getMessage());
                        }
                        dos.writeBoolean(checked);
                        break;
                    case "balance":
                        String iban = dis.readUTF();
                        int pin = dis.readInt();
                        if (ibanCheck.equals("sumybk")) {
                            int balance = database.checkSaldo(iban, pin);
                            dos.writeInt(balance);
                        } else {
                            dos.writeInt(500);
                        }
                        break;
                    case "withdraw":
                        comm.setAmount(Integer.parseInt(dis.readUTF()));
                        System.out.println(comm.getAmount());
			if (ibanCheck.equals("sumybk")) {
                            int balance2 = database.checkSaldo(comm.getIban(), comm.getPin());
                            if (balance2 - comm.getAmount() >= 0) {
                                database.withdraw(comm.getIban(), comm.getPin(), comm.getAmount());
                                dos.writeBoolean(true);
                                comm.setIban("");
                                comm.setPin(0);
                                comm.setAmount(0);
                            } else {
                                dos.writeBoolean(false);
                            }
                        } else {
                            master.setAmount(Double.parseDouble(dis.readUTF()));
                            object = master.withdraw();
                            master.sendText(ibanCheck, object);
                            if (Boolean.parseBoolean(master.getMessage())) {
                                dos.writeBoolean(true);
                                comm.setIban("");
                                comm.setPin(0);
                                comm.setAmount(0);
                            } else {
                                dos.writeBoolean(false);
                            }
                        }
                        break;
                    case "reset":
                        if (comm.getIban().isEmpty() && comm.getPin() == 0 && comm.getAmount() == 0) {
                            dos.writeBoolean(true);
                        } else {
                            dos.writeBoolean(false);
                        }
                        break;
                    case "updateAttempts":
                        if (ibanCheck.equals("sumybk")) {
                            comm.setAttempts(dis.readInt());
                            database.updateAttempts(comm.getAttempts(), comm.getIban());
                        } else {
                            comm.setAttempts(dis.readInt());
                        }
                        break;
                    case "checkBlocked":
                        dos.writeBoolean(database.checkBlocked(comm.getIban()));
                        break;
                    case "blockCard":
                        database.blockedCard(comm.getIban());
                        break;
                    default:
                        dos.writeUTF("Invalid input");
                        break;
                }
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
