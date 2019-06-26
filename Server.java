// File name Server.java
import java.net.*;
import java.io.*;

public class Server {
    public static void main(String[] args) throws IOException {
        // server is listening on port 8080
        ServerSocket serverSocket = new ServerSocket(8080);
        Master master = new Master();
        new Thread(master).start();
        new Thread(new Slave(new Database())).start();

        // running infinite loop for getting client requests
        while (true) {
            Socket socket = null;

            try {
                socket = serverSocket.accept();
                System.out.println("A new client is connected: " + socket);

                // obtaining input and output streams
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                System.out.println("Assigning new thread for this client");

                // create a new thread object
                Thread thread = new ClientHandler(socket, dataInputStream, dataOutputStream, master);

                // Start thread
                thread.start();
            } catch (Exception e) {
                socket.close();
                e.printStackTrace();
            }
        }
    }
}
