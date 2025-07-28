import java.io.*;
import java.net.*;

public class Client {
    private String bootstrapHost;
    private int reqId = 1;
    private int clientId = 1;
    DataOutputStream out;


    private int REQUEST_ID = 5;
    private int STORE_ID = 6;
    private int RETRIEVE_ID = 7;
    
    private int OBJ_STORED_ID = 8;
    private int OBJ_RETRIEVED_ID = 9;
    private int OBJ_NOT_FOUND = 10;

    public static void main(String[] args) {
        String bootstrapHost = "";
        int delay = 0;
        int testcase = 0;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-b")) {
                i++;
                bootstrapHost = args[i];
            } else if (args[i].equals("-d")) {
                i++;
                delay = Integer.parseInt(args[i]) * 1000;
            } else if (args[i].equals("-t")) {
                i++;
                testcase = Integer.parseInt(args[i]);
            }
        }

        Client client = new Client(bootstrapHost);
        client.startClient(delay, testcase);
    }

    public Client(String bootstrapHost) {
        this.bootstrapHost = bootstrapHost;
    }

    public void startClient(int delay, int testcase) {
        try {
            new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(1234)) {
                    while (true) {
                        Socket oSocket = serverSocket.accept();
                        new Thread(() -> handleReceive(oSocket)).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Simulate a delay before sending requests
            Thread.sleep(delay);

            try {
                Socket socket = new Socket(bootstrapHost, 1234);
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            

            if (testcase == 3) {
                sendRequest(STORE_ID, 58, clientId);
            } else if (testcase == 4) {
                sendRequest(RETRIEVE_ID, 2, clientId);
            } else if (testcase == 5) {
                sendRequest(RETRIEVE_ID, 72, clientId);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(int operationType, int objectId, int clientId) {
        try  {

            out.writeInt(REQUEST_ID);
            out.writeInt(reqId++);
            out.writeInt(operationType);
            out.writeInt(objectId);
            out.writeInt(clientId);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void handleReceive(Socket socket) {
        try {
            while (true) {
                DataInputStream in = new DataInputStream(socket.getInputStream());
                int msg = in.readInt();
                int objectId = in.readInt();
    
                if (msg == OBJ_STORED_ID) {
                    System.out.println("STORED: " + objectId);
                } else if (msg == OBJ_RETRIEVED_ID) {
                    System.out.println("RETRIEVED: " + objectId);
                } else if (msg == OBJ_NOT_FOUND) {
                    System.out.println("NOT FOUND: " + objectId);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


