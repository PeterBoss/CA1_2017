package ca1.chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter
 */
public class Client extends Observable implements Runnable{

    private Socket socket;
    private InetAddress serverAddress;
    private int port;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean keepRunning;

    public Client(String serverAddress, int port) throws UnknownHostException {
        this.serverAddress = InetAddress.getByName(serverAddress);
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(serverAddress, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        keepRunning = true;
    }

    public void send(String outgoing) {
        writer.println(outgoing);
    }

    public String receive() throws IOException {
        return reader.readLine();
    }

    public void terminate() throws IOException {
        reader.close();
        writer.close();
        socket.close();
    }

    @Override
    public void run() {
        try {
            connect();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (keepRunning) {
            
            try {
                String incoming = receive();
                setChanged();
                notifyObservers(incoming);
                
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        
    }

}
