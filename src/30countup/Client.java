import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.io.PrintWriter;
// import java.net.Socket;

public class Client {
    private JFrame frame;
    private JTextField numberInput;
    private JTextArea textArea;
    private JButton[] numberButtons;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public Client(String serverAddress, int port) {
        setupGUI();
        setupConnection(serverAddress, port);
        handleServerResponse();
    }

    private void setupGUI() {
        frame = new JFrame("30 Count Up Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);

        frame.getContentPane().setBackground(Color.decode("#107be3")); // Set background color

        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        textArea.setFont(new Font("ARCADE", Font.PLAIN, 32));
        // textArea.setFont(new Font("Seven Segment", Font.PLAIN, 32));
        textArea.setBackground(Color.decode("#899774")); // set background color
        textArea.setForeground(Color.decode("#000000")); // set font is black
        JScrollPane scrollPane = new JScrollPane(textArea);

        JPanel buttonPanel = new JPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 10 pixels padding in all directions
        gbc.gridx = 0;
        gbc.gridy = 0;

        numberButtons = new JButton[3];
        for (int i = 0; i < 3; i++) {
            final int number = i + 1;
            numberButtons[i] = new RoundButton(String.valueOf(number), Color.decode("#e8f100"), Color.decode("#107be3"));
            numberButtons[i].addActionListener(e -> sendNumber(number));
            // numberButtons[i].setBackground(Color.decode("#e8f100"));
            // numberButtons[i].setForeground(Color.decode("#107be3"));
            // buttonPanel.add(new RoundButton(String.valueOf(number), Color.decode("#e8f100"), Color.decode("#107be3")));
            buttonPanel.add(numberButtons[i], gbc);
        }

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void setupConnection(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            System.out.println("Connected");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error connecting to server: " + e.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void sendNumber(int number) {
        out.println(number);
    }

    private void handleServerResponse() {
        new Thread(() -> {
            try {
                String fromServer;
                while ((fromServer = in.readLine()) != null) {
                    final String message = fromServer;
                    SwingUtilities.invokeLater(() -> {
                        textArea.append(message + "\n");
                        if (message.contains("You lose!") || message.contains("You won!")) {
                            JOptionPane.showMessageDialog(frame, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        }
                    });
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Lost connection to the server: " + e.getMessage(),
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                });
            }
        }).start();
    }

    // private void handleServerResponse() {
    //     new Thread(() -> {
    //         try {
    //             String fromServer;
    //             while ((fromServer = in.readLine()) != null) {
    //                 String message = fromServer;
    //                 SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
    //             }
    //         } catch (IOException e) {
    //             SwingUtilities.invokeLater(() -> {
    //                 JOptionPane.showMessageDialog(frame, "Lost connection to the server: " + e.getMessage(),
    //                     "Connection Error", JOptionPane.ERROR_MESSAGE);
    //                 System.exit(1);
    //             });
    //         }
    //     }).start();
    // }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client("127.0.0.1", 5165));
    }
}
