import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler extends Thread {

	/* Server connection */
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket socket;
    final Database database = new Database();
    Communication comm = new Communication();
	
	/* Noob connection */
	Master master = new Master();
	Slave slave = new Slave();
	Thread masterThread = new Thread(master);
	Thread slaveThread = new Thread(slave);
	
    ClientHandler(Socket socket, DataInputStream dis, DataOutputStream dos) {
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
		start();
    }

	public void start() {
		masterThread.start();
		slaveThread.start();
	}
	
    @Override
    public void run() {
        String message;
		String ibanCheck;
        while (true) {
            try {
                // Get message from cliets
                message = dis.readUTF();

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
						String landCode = comm.getIban().substring(0, 2);
						String bankCode = comm.getIban().substring(3, 8);
						ibanCheck = landCode + bankCode;
						System.out.println(ibanCheck);
						if (ibanCheck.equals("SUMYBK")) {
							dos.writeBoolean(database.checkIban(comm.getIban()));
                        	break;	
						} else {
							master.setBankName(ibanCheck);
							master.setIban(comm.getIban();
							dos.writeBoolean(true);
							break;
						}
                    case "pin":
                        comm.setPin(dis.readInt());
						if (ibanCheck.equals("SUMYBK)) {
							dos.writeBoolean(database.checkPin(comm.getIban(), comm.getPin()));
							break;
						} else {
							master.setPin(String.valueOf(comm.getPin()));
							master.setMessage("checkPin");
							break;
						}
                    case "amount":
                        comm.setAmount(Integer.parseInt(dis.readUTF()));
                        break;
                    case "balance":
                        String iban = dis.readUTF();
                        int pin = dis.readInt();
                        int balance = database.checkSaldo(iban, pin);
                        dos.writeInt(balance);
                        break;
                    case "withdraw":
                        comm.setAmount(Integer.parseInt(dis.readUTF()));
						int balance = database.checkSaldo(comm.getIban(), comm.getPin());
						if (balance - comm.getAmount() >= 0) {
                            database.withdraw(comm.getIban(), comm.getPin(), comm.getAmount());
                            dos.writeBoolean(true);
                            comm.setIban("");
                            comm.setPin(0);
                            comm.setAmount(0);
						} else {
			    			dos.writeBoolean(false);
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
                        comm.setAttempts(dis.readInt());
                        database.updateAttempts(comm.getAttempts(), comm.getIban());
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
