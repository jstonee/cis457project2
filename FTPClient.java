import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;


public class FTPClient {
    private static Socket ControlSocket;
    private static int port;

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

    public static String runCommand(String input) throws Exception {
        DataOutputStream toServer = new DataOutputStream(ControlSocket.getOutputStream());
        DataInputStream fromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));

        if (input.startsWith("retr")) {
            tokens = new StringTokenizer(input);
            String file = tokens.nextToken();
            file = tokens.nextToken();
            System.out.println("[FTPClient] Requesting " + file + " from server");

            int port1 = port + 2;
            ServerSocket welcomeData = new ServerSocket(port1);
            toServer.writeBytes(port1 + " " + input + " " + '\n');
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

        } else if (input.equals("close")) {
            System.out.println("[FTPClient] Closing Control Socket");
            toServer.writeBytes(0 + " " + input + " " + '\n');
            ControlSocket.close();
            return "close";
        } else {
            return "Invalid command";
        }
        return null;
    }

    public static void exitClient() {
        System.exit(0);
    }
}
