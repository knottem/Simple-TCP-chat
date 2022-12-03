package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    static final int port = 12345;
    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        int counter = 0;
        try{
            while(true){
                Socket socket = serverSocket.accept();
                counter++;
                System.out.println("A new client has connected: " + socket.getInetAddress().getHostName() + " Nr: " + counter);
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
