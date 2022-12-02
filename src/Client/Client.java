package Client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;


public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private String username;

    private final JFrame frame = new JFrame();
    private final JPanel topPanel = new JPanel();
    private final JPanel bottomPanel = new JPanel();
    private static final JButton button = new JButton("Koppla ner");
    private final JTextArea jTextArea = new JTextArea(20,40);
    private final JTextField jTextField = new JTextField();
    private String messageToSend;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        }catch (IOException e){
            closeEverything(socket,bufferedReader,printWriter);
        }
    }

    public void sendMessage(){
        try{
            printWriter.println(username);
            while(socket.isConnected()){
                if(messageToSend != null){
                    if(messageToSend.startsWith("/nick")){
                        String[] nameChange = messageToSend.split(" ");
                        if(nameChange.length == 2){
                            username = nameChange[1].trim();
                            sendMessage(messageToSend);
                            frame.setTitle("Chat: " + username);
                        } else {
                            jTextArea.append("Username can only be one word\n");
                            messageToSend = null;
                        }
                    } else if(messageToSend.startsWith("/quit") || button.getModel().isPressed()){
                            sendMessage("/quit");
                            closeEverything(socket,bufferedReader,printWriter);
                            System.exit(0);
                    } else if (messageToSend.startsWith("/help")) {
                        jTextArea.append("HELP\n/nick to change your nickname\n/quit to quit the program\n");
                        jTextField.setText("");
                        messageToSend = null;
                    } else {
                        sendMessage(messageToSend);
                    }
                }
            }
        }catch (Exception e){
            closeEverything(socket,bufferedReader,printWriter);
        }
    }

    public void listenForMessage(){
        new Thread(() -> {
            String msgFromGroupChat;

            while(socket.isConnected()){
                try{
                    msgFromGroupChat = bufferedReader.readLine();
                    jTextArea.append(msgFromGroupChat + "\n");

                }catch (IOException e){
                    closeEverything(socket,bufferedReader,printWriter);
                }
            }
        }).start();
    }

    public void sendMessage(String message){
        printWriter.println(message);
        messageToSend = null;
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, PrintWriter printWriter){
        jTextArea.append("\n You've been disconnected");
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

    public void setupWindow(String username){

        frame.setTitle("Chat: " + username);
        frame.setLayout(new BorderLayout());
        topPanel.add(button);
        frame.add(topPanel,BorderLayout.NORTH);
        frame.add(jTextArea);
        JScrollPane scrollBar = new JScrollPane(jTextArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(scrollBar);
        bottomPanel.add(jTextField);
        bottomPanel.setLayout(new GridLayout(1,1));
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.pack();

        button.addActionListener(e -> System.exit(0));

        jTextField.addActionListener(e -> {
            if(!(jTextField.getText().equals(""))) {
                messageToSend = jTextField.getText();
                jTextField.setText("");
            }
        });

    }

    public static void main(String[] args) throws IOException {

        String username = JOptionPane.showInputDialog("What's your username?");
        Socket socket = new Socket("localhost",12345);
        Client client = new Client(socket,username);
        client.setupWindow(username);
        client.listenForMessage();
        client.sendMessage();

    }

}
