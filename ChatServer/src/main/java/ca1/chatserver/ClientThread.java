package ca1.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter
 */
public class ClientThread extends Thread implements Observer {

    Server server;
    Socket socket;
    PrintWriter writer;
    BufferedReader reader;
    String username;

    public ClientThread(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        boolean isValid = true;
        try {
            while (isValid) {
                String input = reader.readLine();
                String[] pieces = input.split("#");
                switch (pieces[0]) {
                    case "LOGIN": {
                        if (pieces.length == 2) { //in case someone tries [LOGIN#] with no username to follow, not sure if necessary
                            username = pieces[1];
                            server.login(this);
                            break;
                        } else {
                            isValid = false;
                            break;
                        }

                    }
                    case "MSG": {
                        if (pieces[1].equals("ALL")) {
                            server.forwardMessageToAll(pieces[2], username);
                            break;
                        } else if (!pieces[1].isEmpty()) {  //dont want null pointers, but should propably use try-catch instead?
                            server.forwardMessageToUser(pieces[2], pieces[1], username);
                            break;
                        } else {
                            isValid = false;
                            break;
                        }
                    }
                    default: {
                        isValid = false;
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            server.logout(username);
        }

    }

    public String getUsername() {
        return username;
    }
    
    @Override
    public void update(Observable o, Object arg) {
        writer.println(arg);
    }

}
