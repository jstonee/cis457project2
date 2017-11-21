import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.ArrayList;

/**
 * Created by mitchcout on 11/20/2017.
 */
public class HostGUIFunctions {
    private static Socket ControlSocket;
    private DataOutputStream toServer;
    int port;

    private static FTPServer ftpServer;
    private static FTPClient ftpClient;

    public HostGUIFunctions(){}

    /**
     * Connect to the server with the given parameters
     */
    public boolean connect(String server, String portString, String username, String hostname, String itype) {
        // make sure user data isnt empty
	if(server == null || server.isEmpty() ||
	      portString == null || portString.isEmpty() ||
	      username == null || username.isEmpty() ||
	      hostname == null || hostname.isEmpty() ||
	      itype == null || itype.isEmpty()) {
	  return false;
        }
        try {
            port = Integer.parseInt(portString);
        } catch (Exception e) {
            return false;
        }

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

	    // start FTP Server and Client
	    setupFTPServer((port+2)+"");
	    setupFTPClient(hostname,(port+2)+"");

	    // send file list
	    enterCommand("list:", null);
	    toServer.writeBytes("list "+(port+6)+'\n');
	    
        } catch(Exception e) {
	    e.printStackTrace();
            return false;
        }
        
        return true;
    }

    public ArrayList<ArrayList<String>> search(String keyword) {
	ArrayList<ArrayList<String>> results = null;
	try {
	    toServer.writeBytes("search "+(port+4)+" "+keyword+'\n');
	    ServerSocket newSocket = new ServerSocket(port+4);
	    Socket dataConnectionSocket = newSocket.accept();
	    ObjectInputStream objectInputStream = new ObjectInputStream(dataConnectionSocket.getInputStream());
	    results = (ArrayList<ArrayList<String>>) objectInputStream.readObject();
	    newSocket.close();
	    dataConnectionSocket.close();
	} catch (Exception e) {
	    return null;
	}
        return results;
    }

    /**
     * Runs a command on the FTPClient
     * @param command
     * @return
     */
    public String enterCommand(String command, String[] args) {
        String response = null;
	try {
	    response = FTPClient.runCommand(command, args);
	} catch (Exception e) {
	    e.printStackTrace();
	    return "Error";
	}
        if(response != null && response.equals("close")){
            try {
                toServer.writeBytes("close\n");
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
