package Server;

import Client.ClientV2;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;

import static utility.tools.closingEverything;

public class ClientHandler extends Thread {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private String clientUsername;

    public ClientHandler(Socket socket){
        try {
            this.socket = socket;

            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.clientUsername = bufferedReader.readLine();
            broadcastMessage("SERVER: "+ clientUsername + " has entered the chat!");
            clientHandlers.add(this);

        } catch (Exception e) {
            closeEverything(socket, bufferedReader,printWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        try{
            while((messageFromClient = bufferedReader.readLine()) != null){
                if(messageFromClient.startsWith("/nick")){
                    String[] messageSplit = messageFromClient.split(" ", 2);
                    if(messageSplit.length == 2){
                        broadcastMessage(clientUsername + " renamed themselves to " + messageSplit[1].trim());
                        System.out.println(clientUsername + " renamed themselves to " + messageSplit[1].trim());
                        clientUsername = messageSplit[1].trim();
                    }
                } else if(messageFromClient.startsWith("/quit")){
                    closeEverything(socket,bufferedReader,printWriter);
                    break;
                } else if(messageFromClient.startsWith("/msg")){
                    String[] messageSplit = messageFromClient.split(" ", 2);
                    if(messageSplit.length == 2){
                        broadcastMessage(clientUsername + ": " + messageSplit[1]);
                    }
                } else {
                    broadcastMessage(clientUsername +": "+ messageFromClient);
                }
            }
        }catch (IOException e){
                closeEverything(socket, bufferedReader, printWriter);
        }
    }


    public void broadcastMessage(String messageToSend){
        if(clientHandlers != null) {
            for (ClientHandler clientHandler : clientHandlers) {
                    clientHandler.printWriter.println(LocalTime.now().withNano(0) + ": " + messageToSend);
            }
        }
    }


    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, PrintWriter printWriter){
        removeClientHandler();
        closingEverything(socket, bufferedReader, printWriter);
    }
}
