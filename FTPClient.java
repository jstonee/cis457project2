import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;


public class FTPClient {
    private static Socket ControlSocket;
    private static int port;
    private static DataOutputStream toServer;

    private static StringTokenizer tokens;

    public static void main(String argv[]) throws Exception {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        // Get params
        String serverName = argv[0]; // serverIP
        try {
            port = Integer.parseInt(argv[1]); // port
        } catch (Exception e) {
            return;
        }
        System.out.println("[FTPClient] Connecting to " + serverName + ":" + port);
        try {
            ControlSocket = new Socket(serverName, port);
        } catch (IOException ioEx) {
            System.out.println("[FTPClient] Unable to connect to " + serverName + ":" + port);
            System.exit(1);
        }
    }

    public static String runCommand(String input, String args[]) throws Exception {
        toServer = new DataOutputStream(ControlSocket.getOutputStream());

        // Establish P2P connection with remote FTP Server and retrieve file
        if (input.equals("list:")) {
                    int port1 = port + 2;
                    ServerSocket welcomeData = new ServerSocket(port1);
                    toServer.writeBytes(port1 + " " + input + " " + '\n');

                    Socket dataSocket = welcomeData.accept();
                    BufferedReader inData = new BufferedReader(new InputStreamReader(new BufferedInputStream(dataSocket.getInputStream())));

                    ArrayList<String> dataFromServer = new ArrayList<>();
		    String line = inData.readLine();
		    dataFromServer.add(line);
                    while(line != null) {
			line = inData.readLine();
                        dataFromServer.add(line);
                    }

		    // connect to main server
		    ServerSocket newSocket = new ServerSocket(port+4);
		    Socket dataConnectionSocket = newSocket.accept();
		    ObjectOutputStream dataOutToClient = new ObjectOutputStream(dataConnectionSocket.getOutputStream());
		    dataOutToClient.writeObject(dataFromServer);
		    
		    welcomeData.close();
		    dataSocket.close();
		    inData.close();
		    newSocket.close();
		    dataConnectionSocket.close();
		    dataOutToClient.close();

            } else if (input.startsWith("retr")) {
            Socket remoteFTPServerSocket;
            // Get params for new remote server
            String remoteServerName = args[0]; // remote ftp server IP
            int remoteServerPort;
	    remoteServerPort = port; // remote ftp server port
            System.out.println("[FTPClient] Connecting to " + remoteServerName + ":" + remoteServerPort);
            try {
                remoteFTPServerSocket = new Socket(remoteServerName, remoteServerPort);
            } catch (IOException ioEx) {
                System.out.println("[FTPClient] Unable to connect to " + remoteServerName + ":" + remoteServerPort);
                return "Error";
            }
            DataOutputStream toRemoteServer = new DataOutputStream(remoteFTPServerSocket.getOutputStream());

            tokens = new StringTokenizer(input);
            tokens.nextToken();
            String file = tokens.nextToken();
            System.out.println("[FTPClient] Requesting " + file + " from server");

            int port1 = remoteServerPort + 2;
            ServerSocket welcomeData = new ServerSocket(port1);
            toRemoteServer.writeBytes(port1 + " " + input + " " + '\n');
            Socket dataSocket = welcomeData.accept();
            System.out.println("[FTPClient] Data socket open, retrieving file now");

            BufferedInputStream inData = new BufferedInputStream(new DataInputStream(dataSocket.getInputStream()));
            FileOutputStream newFile = new FileOutputStream(new File(file));
            byte[] buffer = new byte[8192];
            int count;
            while((count = inData.read(buffer)) > 0) {
                newFile.write(buffer, 0, count);
            }

            newFile.close();
            inData.close();
            dataSocket.close();
            welcomeData.close();
            remoteFTPServerSocket.close();
            toRemoteServer.close();

	    return "Retrieved "+file;
        }
        // close and send close message to local FTP Server
        else if (input.equals("close")) {
            System.out.println("[FTPClient] Closing Control Socket");
            toServer.writeBytes(0 + " " + input + " " + '\n');
            ControlSocket.close();
            return "close";
        } else {
            return "Invalid command";
        }
        return null;
    }
}
