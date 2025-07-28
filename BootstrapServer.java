import java.io.*;
import java.net.*;
import java.util.*;

public class BootstrapServer {
    private final int PORT = 1234;
    //private TreeMap<Integer, String> peers = new TreeMap<>();
    private DataOutputStream clientOut;

    private int JOIN_ID = 1;
    private int UPDATE_SUCCESSOR_ID = 2;
    private int UPDATE_PREDECESSOR_ID = 3;

    private int REQUEST_ID = 5;
    
    private int OBJ_STORED_ID = 8;
    private int OBJ_RETRIEVED_ID = 9;
    private int OBJ_NOT_FOUND = 10;

    private Map<Integer, Socket> peersToSocket = new HashMap<>();
    private Map<Integer, DataOutputStream> peersToOutput = new HashMap<>();

    private List<Integer> peers = new ArrayList<>();

    public static void main(String[] args) {
        BootstrapServer server = new BootstrapServer();
        server.startServer();
    }

    public void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void handleClient(Socket socket) {
        try {
            while (true) {
                DataInputStream in = new DataInputStream(socket.getInputStream());

                int action = in.readInt();

                if (action == JOIN_ID) {
                    int peerId = in.readInt();
                    Socket peerSocket = new Socket("n" + peerId, 1234);
                    peersToOutput.put(peerId, new DataOutputStream(peerSocket.getOutputStream()));
                    peers.add(peerId);
                    Collections.sort(peers);

                    printRing();

                    int index = peers.indexOf(peerId);
                    int predecessor;
                    int successor;
                    if (index == 0) {
                        predecessor = peers.get(peers.size() - 1);
                    } else {
                        predecessor = peers.get(index - 1);
                    }

                    if (index == peers.size() - 1) {
                        successor = peers.get(0);
                    } else {
                        successor = peers.get(index + 1);
                    }

                    peersToOutput.get(peerId).writeInt(JOIN_ID);
                    peersToOutput.get(peerId).writeInt(predecessor);
                    peersToOutput.get(peerId).writeInt(successor);

                    if (predecessor != peerId || successor != peerId) {
                        notifyPeers(predecessor, successor, peerId);
                    }
                }

                // Handle requests from the client for storing and retrieving objects
                else if (action == REQUEST_ID) {
                    if (clientOut == null) {
                        Socket clientSocket = new Socket("client", 1234);
                        clientOut = new DataOutputStream(clientSocket.getOutputStream());
                    }
                    int reqId = in.readInt();
                    int operationType = in.readInt();
                    int objectId = in.readInt();
                    int clientId = in.readInt();

                    forwardRequestToPeer(peers.get(0), operationType, objectId, clientId, reqId);
                } else if (action == OBJ_STORED_ID) {
                    int objectId = in.readInt();
                    clientOut.writeInt(OBJ_STORED_ID);
                    clientOut.writeInt(objectId);
                } else if (action == OBJ_RETRIEVED_ID) {
                    int objectId = in.readInt();
                    clientOut.writeInt(OBJ_RETRIEVED_ID);
                    clientOut.writeInt(objectId);
                } else if (action == OBJ_NOT_FOUND) {
                    int objectId = in.readInt();
                    clientOut.writeInt(OBJ_NOT_FOUND);
                    clientOut.writeInt(objectId);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void forwardRequestToPeer(int peerId, int operationType, int objectId, int clientId, int reqId) {
        try {
            DataOutputStream out = peersToOutput.get(peerId);
            out.writeInt(REQUEST_ID);
            out.writeInt(reqId);
            out.writeInt(operationType);
            out.writeInt(objectId);
            out.writeInt(clientId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyPeers(int predecessor, int successor, int newPeerId) {
        try {
            notifyPeer(predecessor, UPDATE_SUCCESSOR_ID, newPeerId);
            notifyPeer(successor, UPDATE_PREDECESSOR_ID, newPeerId);
        } catch (Exception e) {
        }
    }

    private void notifyPeer(int peerId, int action, int targetPeer) {
        try {
            DataOutputStream out = peersToOutput.get(peerId);
            out.writeInt(action);
            out.writeInt(targetPeer);
        } catch (IOException e) {
        }
    }

    private void printRing() {
        System.err.print("Ring: [");
        int i = 0;
        for (Integer peer : peers) {
            i++;
            if (i == peers.size()) {
                System.err.print("n" + peer);
            } else {
                System.err.print("n" + peer + " ");
            }
        }
        System.err.print("]\n");;
    }
}
