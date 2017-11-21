import java.io.*;
import java.net.*;
import java.lang.*;

/**
 * Created by mitchcout on 11/20/2017.
 */
public class HostGUIFunctions {
    private static Socket ControlSocket;

    public HostGUIFunctions(){}

    /**
     * Connect to the server with the given parameters
     */
    public boolean connect(String server, String portString, String username, String hostname, String itype) {
        // make user data isnt empty
        if(server == null || server.isEmpty() ||
                portString == null || portString.isEmpty() ||
                username == null || username.isEmpty() ||
                hostname == null || hostname.isEmpty() ||
                itype == null || itype.isEmpty()) {
            return false;
        }
        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (Exception e) {
            return false;
        }
//        server = "127.0.0.1";
//        username = "jstone";
//        hostname = "127.0.0.1";
//        itype = "T1";
//        port = 5568;

        // Connect
        try {
            ControlSocket = new Socket(server, port);
        } catch(IOException ioEx) {
            System.out.println("Unable to connect to " + server + ":" + port);
            return false;
        }

        // Send info to server
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        try {
            DataOutputStream toServer = new DataOutputStream(ControlSocket.getOutputStream());
            toServer.writeBytes("info" + " " + username + " " + hostname + " " + itype + '\n');
//            System.out.print(">> ");
//            String input = inFromUser.readLine();
//            toServer.writeBytes("close");
//            ControlSocket.close();
//            System.exit(0);
        } catch(Exception e) {
            return false;
        }
        return true;
    }
}