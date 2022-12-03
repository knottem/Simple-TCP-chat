package utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class tools {

    public static void closingEverything(Socket socket, BufferedReader bufferedReader, PrintWriter printWriter) {
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(printWriter != null){
                printWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
