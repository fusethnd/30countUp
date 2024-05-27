import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// import java.io.IOException;
// import java.net.ServerSocket;
// import java.net.Socket;
// import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int PORT = 5165;
    public static volatile int currentNumber = 0;
    public static volatile int currentPlayer = 1;  // Player 1 or Player 2
    public static AtomicInteger connectionCount = new AtomicInteger(0); // handling multithread : no interrupt from another thread
    private static ServerSocket server;

    public static void main(String[] args) {
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server is listening on port " + PORT + "...");

            ClientHandler[] handlers = new ClientHandler[2];

            Socket clientSocket = server.accept(); 
            handlers[0] = new ClientHandler(clientSocket, 1);
            handlers[0].start();
            System.out.println("Player 1 connected from " + clientSocket.getInetAddress().getHostAddress());

            clientSocket = server.accept();
            handlers[1] = new ClientHandler(clientSocket, 2);
            handlers[1].start();
            System.out.println("Player 2 connected from " + clientSocket.getInetAddress().getHostAddress());
            
            // for (int i = 0; i < 2; i++) {
            //     Socket clientSocket = server.accept();
            //     handlers[i] = new ClientHandler(clientSocket, i + 1);
            //     handlers[i].start();
            //     System.out.println("Player " + (i + 1) + " connected from " + clientSocket.getInetAddress().getHostAddress());
            // }

            for (ClientHandler handler : handlers) {
                handler.join();
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Error in server setup: " + e.getMessage());
        }
    }

    public static class ClientHandler extends Thread {
        private Socket clientSocket;
        private int playerNumber;
    
        public ClientHandler(Socket socket, int playerNumber) {
            this.clientSocket = socket;
            this.playerNumber = playerNumber;
        }
    
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                
                out.println("Welcome to 30CountUp Game. You're Player " + playerNumber);
    
                while (Server.currentNumber < 30) {
                    // only one thread can work under this block
                    synchronized (Server.class) {
                        while (Server.currentPlayer != playerNumber && Server.currentNumber < 30) {
                            Server.class.wait();
                        }
    
                        if (Server.currentNumber >= 30) {
                            out.println("You won!");
                            break;
                        }
    
                        out.println("Your turn. Current number is " + Server.currentNumber);
                        int addedValue = Integer.parseInt(in.readLine());
                        // String input = in.readLine();
                        // int addedValue = Integer.parseInt(input);
                        while (addedValue < 1 || addedValue > 3) {
                            out.println("False Value, Please Retry");
                            addedValue = Integer.parseInt(in.readLine());
                        }
                        
                        Server.currentNumber += addedValue;
                        out.println("Now number is " + Server.currentNumber);
    
                        if (Server.currentNumber >= 30) {
                            out.println("You lose!");
                            Server.currentPlayer = playerNumber == 1 ? 2 : 1;
                            Server.class.notifyAll();
                            break;
                        }
                        
                        Server.currentPlayer = playerNumber == 1 ? 2 : 1;
                        Server.class.notifyAll();
                    }
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error in client connection: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }    
}
