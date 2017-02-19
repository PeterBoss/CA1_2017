package ca1.chatserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Peter
 */
public class Server extends Observable {

    private String ip;
    private int port;

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    boolean keepRunning = false;
//    ExecutorService service = Executors.newCachedThreadPool();
    ConcurrentMap<String, ClientThread> users = new ConcurrentHashMap<>();  //for keeping track of usernames

    public void startServer() throws IOException {

        ServerSocket socket = new ServerSocket();
        socket.bind(new InetSocketAddress(InetAddress.getByName(ip), port));
        keepRunning = true;
        System.out.println("Server started. Listening...");
        do {
            Socket connection = socket.accept();
            System.out.println("Connected to a client");
            ClientThread client = new ClientThread(this, connection);
            client.start();

        } while (keepRunning);

    }

    public void stopServer() {
        keepRunning = false;
    }

    public synchronized void login(ClientThread client) {  //might be better to do try-catch and throw an exception
        if (!users.containsKey(client.getUsername())) {
            users.put(client.getUsername(), client);
            setChanged();
            notifyObservers("UPDATE#" + client.getUsername());
            addObserver(client);
            client.update(this, getUsers());
        } else {
            client.update(this, "FAIL");
        }
    }

    synchronized public void logout(String username) {
        users.remove(username);
        deleteObserver(users.get(username));
        setChanged();
        notifyObservers("DELETE#" + username);
    }

    synchronized void forwardMessageToUser(String message, String recipient, String sender) {
        Observer target = users.get(recipient);
        target.update(this, "MSG#" + sender + "#" + message);
    }

    synchronized void forwardMessageToAll(String message, String sender) {
        setChanged();
        notifyObservers("MSG#" + sender + "#" + message);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(args[0], Integer.valueOf(args[1]));
        server.startServer();
    }

    private String getUsers() {
        StringBuilder ok = new StringBuilder("OK#");
        for (Map.Entry<String, ClientThread> entry : users.entrySet()) {
            ok.append(entry.getValue().getUsername());
            ok.append("#");
        }
        ok.deleteCharAt(ok.length() - 1); //dont need the last #.
        return ok.toString();
    }

}
