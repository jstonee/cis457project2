import java.io.*;
import java.net.*;
import java.lang.*;

/**
 * Created by mitchcout on 11/20/2017.
 */
public class HostGUIFunctions {
    private static Socket ControlSocket;

    public HostGUIFunctions(){}

    public boolean connect(String server, String portString, String username, String hostname, String itype) {
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

        try {
            ControlSocket = new Socket(server, port);
        } catch(IOException ioEx) {
            System.out.println("Unable to connect to " + server + ":" + port);
            return false;
        }

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

    /**
     *
     */
}