import java.io.*;
import java.net.*;
import java.lang.*;

/**
 * Created by mitchcout on 11/20/2017.
 */
public class HostGUIFunctions {
    private static Socket ControlSocket;
    private DataOutputStream toServer;

    private static FTPServer ftpServer;
    private static FTPClient ftpClient;

    public HostGUIFunctions(){}

    /**
     * Connect to the server with the given parameters
     */
    public boolean connect(String server, String portString, String username, String hostname, String itype) {
        // make sure user data isnt empty
	//        if(server == null || server.isEmpty() ||
	//      portString == null || portString.isEmpty() ||
	//      username == null || username.isEmpty() ||
	//      hostname == null || hostname.isEmpty() ||
	//      itype == null || itype.isEmpty()) {
	//  return false;
        //}
        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (Exception e) {
            return false;
        }
        server = "127.0.0.1";
        username = "couturmi";
        hostname = "127.0.0.1";
        itype = "T1";
        port = 5568;

        // Connect
        try {
            ControlSocket = new Socket(server, port);
        } catch(IOException ioEx) {
            System.out.println("Unable to connect to " + server + ":" + port);
            return false;
        }

        // Send info to server
        try {
            toServer = new DataOutputStream(ControlSocket.getOutputStream());
            toServer.writeBytes("info" + " " + username + " " + hostname + " " + itype + '\n');
        } catch(Exception e) {
            return false;
        }

        // start FTP Server and Client
        setupFTPServer((port+2)+"");
        setupFTPClient(hostname,(port+2)+"");
        return true;
    }

    public boolean search(String keyword) {

        return true;
    }

    /**
     * Runs a command on the FTPClient
     * @param command
     * @return
     */
    public String enterCommand(String command, String[] args) {
        String response;
        try {
            response = FTPClient.runCommand(command, args);
        } catch (Exception e) {
            return "Error";
        }
        if(response.equals("close")){
            try {
                toServer.writeBytes("close");
		toServer.flush();
                toServer.close();
                ControlSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    /**
     * Sets up the FTPServer
     * @param port
     */
    private void setupFTPServer(String port) {
        String[] args = {port};
        ServerThread newThread = new ServerThread(args);
        newThread.start();
    }

    /**
     * Sets up the FTPClient
     * @param IPAddress
     * @param port
     */
    private void setupFTPClient(String IPAddress, String port) {
        String[] args = {IPAddress, port};
        try {
            FTPClient.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * The thread that runs the server
 */
class ServerThread extends Thread {

    private String[] args;

    public ServerThread(String[] args) {
        this.args = args;
    }

    public void run() {
        try {
            FTPServer.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
