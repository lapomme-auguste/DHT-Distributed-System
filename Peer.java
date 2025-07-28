import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {
    private int peerId;
    private int predecessor;
    private int successor;
    private String bootstrapHost;
    private String objectsFile;
    private Map<Integer, Integer> objectStore = new HashMap<>();
    private ServerSocket serverSocket;

    private DataOutputStream bootstrapServerOut;
    private DataOutputStream successorOut;
    private DataOutputStream predecessorOut;

    private int JOIN_ID = 1;
    private int UPDATE_SUCCESSOR_ID = 2;
    private int UPDATE_PREDECESSOR_ID = 3;

    private int REQUEST_ID = 5;
    private int STORE_ID = 6;
    private int RETRIEVE_ID = 7;
    
    private int OBJ_STORED_ID = 8;
    private int OBJ_RETRIEVED_ID = 9;
    private int OBJ_NOT_FOUND = 10;

    public static void main(String[] args) {
        String bootstrapHost = "";
        int delay = 0;
        String objectsFile = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-b")) {
                i++;
                bootstrapHost = args[i];
            } else if (args[i].equals("-d")) {
                i++;
                delay = Integer.parseInt(args[i]) * 1000;
            } else if (args[i].equals("-o")) {
                i++;
                objectsFile = args[i];
            }

        }

        Peer peer = new Peer(bootstrapHost, objectsFile);
        peer.startPeer(delay);
    }

    public Peer(String bootstrapHost, String objectsFile) {
        this.bootstrapHost = bootstrapHost;
        this.objectsFile = objectsFile;
        loadObjects();
    }

    private void loadObjects() {
        try (BufferedReader br = new BufferedReader(new FileReader(objectsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("::");
                int clientId = Integer.parseInt(parts[0]);
                int objectId = Integer.parseInt(parts[1]);
                objectStore.put(objectId, clientId);
            }
        } catch (IOException e) {
            System.out.println("Failed to load objects.");
        }
    }

    public void startPeer(int delay) {
        try {
            Thread t = new Thread(() -> startListener());
            t.start();

            Thread.sleep(delay);
            peerId = Integer.parseInt(InetAddress.getLocalHost().getHostName().substring(1));

            Socket socket = new Socket(bootstrapHost, 1234);
            bootstrapServerOut = new DataOutputStream(socket.getOutputStream());
            bootstrapServerOut.writeInt(JOIN_ID);
            bootstrapServerOut.writeInt(peerId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startListener() {
        try {

            serverSocket = new ServerSocket(1234);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleRequest(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket socket) {
        while (true) {
            try {
                DataInputStream in = new DataInputStream(socket.getInputStream());
                int action = in.readInt();
    
                if (action == REQUEST_ID) {
                    int reqId = in.readInt();
                    int operationType = in.readInt();
                    int objectId = in.readInt();
                    int clientId = in.readInt();
    
                    if (operationType == STORE_ID) {
                        storeObject(objectId, clientId, reqId);
                    } else if (operationType == RETRIEVE_ID) {
                        retrieveObject(objectId, clientId, reqId);
                    }
    
                } else if (action == UPDATE_SUCCESSOR_ID) {
                    successor = in.readInt();
                    Socket successorSocket = new Socket("n" + successor, 1234);
                    successorOut = new DataOutputStream(successorSocket.getOutputStream());
                    System.err.print("Predecessor: n" + predecessor + ", Successor: n" + successor + "\n");
                } else if (action == UPDATE_PREDECESSOR_ID) {
                    predecessor = in.readInt();
                    Socket predSocket = new Socket("n" + predecessor, 1234);
                    predecessorOut = new DataOutputStream(predSocket.getOutputStream());
                    System.err.print("Predecessor: n" + predecessor + ", Successor: n" + successor + "\n");
                } else if (action == JOIN_ID) {
                    predecessor = in.readInt();
                    successor = in.readInt();

                    if (predecessor != peerId) {
                        Socket predSocket = new Socket("n" + predecessor, 1234);
                        predecessorOut = new DataOutputStream(predSocket.getOutputStream());
                    }
                    if (successor != peerId) {
                        Socket successorSocket = new Socket("n" + successor, 1234);
                        successorOut = new DataOutputStream(successorSocket.getOutputStream());
                    }

                    System.err.print("Predecessor: n" + predecessor + ", Successor: n" + successor + "\n");
                }
    
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void storeObject(int objectId, int clientId, int reqId) {
        if (objectId > peerId && successor != 1) {
            try {
                    successorOut.writeInt(REQUEST_ID);
                    successorOut.writeInt(reqId);
                    successorOut.writeInt(STORE_ID);
                    successorOut.writeInt(objectId);
                    successorOut.writeInt(clientId);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            objectStore.put(objectId, clientId);
            saveObjectsToFile();
            try {
                bootstrapServerOut.writeInt(OBJ_STORED_ID);
                bootstrapServerOut.writeInt(objectId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void retrieveObject(int objectId, int clientId, int reqId) throws IOException {
        if (objectStore.containsKey(objectId) && objectStore.get(objectId) == clientId) {
            bootstrapServerOut.writeInt(OBJ_RETRIEVED_ID);
            bootstrapServerOut.writeInt(objectId);
        } else if (successor == 1) {
            bootstrapServerOut.writeInt(OBJ_NOT_FOUND);
            bootstrapServerOut.writeInt(objectId);
        } else {
            successorOut.writeInt(REQUEST_ID);
            successorOut.writeInt(reqId);
            successorOut.writeInt(RETRIEVE_ID);
            successorOut.writeInt(objectId);
            successorOut.writeInt(clientId);
        }
    }

    private void saveObjectsToFile() {
        try {
            FileWriter writer = new FileWriter(objectsFile);
            for (Map.Entry<Integer, Integer> entry : objectStore.entrySet()) {
                writer.write(entry.getKey() + "::" + entry.getValue() + "\n");
                System.err.print(entry.getKey() + "::" + entry.getValue() + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
