import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int PORT = 5165;
    public static volatile int currentNumber = 0;
    public static volatile int currentPlayer = 1; // Player 1 or Player 2
    // public static AtomicInteger connectionCount = new AtomicInteger(0); //
    // handling multithread : no interrupt from another thread
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is listening on port " + PORT + "...");

            ClientThread[] threads = new ClientThread[2];

            Socket clientSocket = serverSocket.accept();
            threads[0] = new ClientThread(clientSocket, 1);
            threads[0].start();
            System.out.println("Player 1 connected from " + clientSocket.getInetAddress().getHostAddress());

            clientSocket = serverSocket.accept();
            threads[1] = new ClientThread(clientSocket, 2);
            threads[1].start();
            System.out.println("Player 2 connected from " + clientSocket.getInetAddress().getHostAddress());

            for (ClientThread thread : threads) {
                thread.join();
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Error in server setup: " + e.getMessage());
        }
    }

    public static class ClientThread extends Thread {
        private Socket clientSocket;
        private int playerNumber;

        public ClientThread(Socket socket, int playerNumber) {
            this.clientSocket = socket;
            this.playerNumber = playerNumber;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                out.println("Welcome to 30CountUp Game. You're Player " + playerNumber);

                while (currentNumber < 30) {
                    // only one thread can work under this block
                    synchronized (Server.class) {
                        while (currentPlayer != playerNumber && currentNumber < 30) {
                            Server.class.wait();
                        }

                        if (currentNumber >= 30) {
                            out.println("You won!");
                            break;
                        }

                        out.println("Your turn. Current number is " + currentNumber);
                        int addedValue = Integer.parseInt(in.readLine());
                        // String input = in.readLine();
                        // int addedValue = Integer.parseInt(input);
                        while (addedValue < 1 || addedValue > 3) {
                            out.println("False Value, Please Retry");
                            addedValue = Integer.parseInt(in.readLine());
                        }

                        currentNumber += addedValue;
                        out.println("Now number is " + currentNumber);

                        if (currentNumber >= 30) {
                            out.println("You lose!");
                            currentPlayer = playerNumber == 1 ? 2 : 1;
                            Server.class.notifyAll();
                            break;
                        }

                        currentPlayer = playerNumber == 1 ? 2 : 1;
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
