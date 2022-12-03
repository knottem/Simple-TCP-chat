package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static utility.tools.closingEverything;


public class ClientV2 {

    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private static String username;

    private final JFrame frame = new JFrame();
    private final JPanel topPanel = new JPanel(),bottomPanel = new JPanel(), ipPanel = new JPanel();
    private final JLabel ipLabel = new JLabel("IP:"), portLabel = new JLabel("Port:");
    private static final JButton button = new JButton("Connect");
    private final JTextArea jTextArea = new JTextArea(20,40);
    private final JTextField jTextField = new JTextField(), ipText = new JTextField(), portText = new JTextField();

    boolean quit = true;
    boolean success = false;
    String ip = "localhost";
    int port = 12345;

    public void startServices(String username){
        try{
            this.socket = new Socket(ip,port);
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ClientV2.username = username;
            new Thread(listenForMessage).start();
            printWriter.println(username);
            success = true;
        }catch (IOException e){
            jTextArea.append("Something went wrong, try again\n");
            closeEverything(socket,bufferedReader,printWriter);
        }
    }

    public void broadcastMessages(String messageToSend){
        if(!quit) {
            try {
                if (messageToSend != null) {
                    if (messageToSend.startsWith("/nick")) {
                        String[] nameChange = messageToSend.split(" ");
                        if (nameChange.length == 2) {
                            username = nameChange[1].trim();
                            sendMessage(messageToSend);
                            frame.setTitle("Chat: " + username);
                        } else {
                            jTextArea.append("Username can only be one word\n");
                        }
                    } else if (messageToSend.startsWith("/quit") || button.getModel().isPressed()) {
                        sendMessage("/quit");
                        closeEverything(socket, bufferedReader, printWriter);
                        System.exit(0);
                    } else if (messageToSend.startsWith("/help")) {
                        jTextArea.append("HELP\n/nick to change your nickname\n/quit to quit the program\n");
                        jTextField.setText("");
                    } else {
                        sendMessage(messageToSend);
                    }
                }
            } catch (Exception e) {
                closeEverything(socket, bufferedReader, printWriter);
            }
        }
    }

    private final Runnable listenForMessage = () -> {
        String msgFromGroupChat;
        try {
            while ((msgFromGroupChat = bufferedReader.readLine()) != null) {
                if (quit) {
                    break;
                } else {
                    jTextArea.append(msgFromGroupChat + "\n");
                    jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
                }
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, printWriter);
        }
    };


    public void sendMessage(String message){
        printWriter.println(message);
        jTextField.setText("");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, PrintWriter printWriter){
        closingEverything(socket, bufferedReader, printWriter);
    }

    public void setupWindow(String username){

        frame.setTitle("Chat: " + username);
        frame.setLayout(new BorderLayout());

        ipText.setText("localhost");
        portText.setText("12345");

        ipPanel.setLayout(new GridLayout(2,2));
        ipPanel.add(ipLabel);
        ipPanel.add(ipText);
        ipPanel.add(portLabel);
        ipPanel.add(portText);

        topPanel.setLayout(new GridLayout(2,1));
        topPanel.add(ipPanel);
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

        button.addActionListener(e -> buttonFunction());


        jTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    broadcastMessages(jTextField.getText());
                    jTextField.setText("");
                }
            }
        });

    }
    private void buttonFunction(){
        if(!quit){
            quit = true;
            jTextArea.append("You've disconnected from " + socket.getInetAddress().getHostName() + "\n");
            button.setText("Connect");
        } else {
            button.setEnabled(false);
            quit = false;
            ip = ipText.getText();
            port = Integer.parseInt(portText.getText());
            startServices(username);
            if(success) {
                button.setText("Disconnect");
            } else {
                quit = true;
            }
            button.setEnabled(true);
            success = false;
        }
    }


    public static void main(String[] args) throws IOException {
        username = JOptionPane.showInputDialog("What's your username?");
        ClientV2 client = new ClientV2();
        client.setupWindow(username);
    }

}
