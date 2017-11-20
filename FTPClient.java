import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.*;


public class FTPClient {
    private static Socket ControlSocket;

    public static void main(String argv[]) throws Exception {
        String input;
        System.out.println("Type connect <ip> <port>");

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("FTPClient >> ");
        input = inFromUser.readLine();
        StringTokenizer tokens = new StringTokenizer(input);

        if (input.startsWith("connect")) {
            String serverName = tokens.nextToken(); // connect
            serverName = tokens.nextToken(); // serverIP
            int port = Integer.parseInt(tokens.nextToken()); // port
            System.out.println("Connecting to " + serverName + ":" + port);
            try {
                ControlSocket = new Socket(serverName, port);
            } catch (IOException ioEx) {
                System.out.println("Unable to connect to " + serverName + ":" + port);
                System.exit(1);
            }
            while (true) {
                DataOutputStream toServer = new DataOutputStream(ControlSocket.getOutputStream());
                DataInputStream fromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
                System.out.print("\nFTPClient >> ");
                input = inFromUser.readLine();

                if (input.equals("list:")) {
                    int port1 = port + 2;
                    ServerSocket welcomeData = new ServerSocket(port1);
                    toServer.writeBytes(port1 + " " + input + " " + '\n');

                    Socket dataSocket = welcomeData.accept();
                    System.out.println("\nThe files on this server are:");
                    BufferedReader inData = new BufferedReader(new InputStreamReader(new BufferedInputStream(dataSocket.getInputStream())));

                    String dataFromServer = inData.readLine();
                    while(dataFromServer != null) {
                        System.out.println(dataFromServer);
                        dataFromServer = inData.readLine();
                    }
                    inData.close();
                    dataSocket.close();
                    welcomeData.close();

                } else if (input.startsWith("retr")) {
                    tokens = new StringTokenizer(input);
                    String file = tokens.nextToken();
                    file = tokens.nextToken();
                    System.out.println("Requesting " + file + " from server");

                    int port1 = port + 2;
                    ServerSocket welcomeData = new ServerSocket(port1);
                    toServer.writeBytes(port1 + " " + input + " " + '\n');
                    Socket dataSocket = welcomeData.accept();
                    System.out.println("Data socket open, retrieving file now");

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

                } else if (input.startsWith("stor")) {
                    tokens = new StringTokenizer(input);
                    String file = tokens.nextToken();
                    file = tokens.nextToken();
		            File openFile = new File(file);
		            if(openFile.exists()) {
                        System.out.println("Storing " + file + " to server");
                        byte[] buffer = new byte[8192];
                        int port1 = port + 2;
                        ServerSocket welcomeData = new ServerSocket(port1);
                        toServer.writeBytes(port1 + " " + input + " " + '\n');
                        Socket dataSocket = welcomeData.accept();
                        System.out.println("Data socket open, sending file now");
                        DataOutputStream sendFile = new DataOutputStream(dataSocket.getOutputStream());
                        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                        int count;
                        while((count = in.read(buffer)) > 0) {
                            sendFile.write(buffer, 0, count);
                        }
                        in.close();
                        sendFile.close();
                        dataSocket.close();
                        welcomeData.close();
                    } else {
                        System.out.println("File does not exist.");
                    }

                } else if (input.equals("close")) {
                    System.out.println("Closing Control Socket");
                    toServer.writeBytes(0 + " " + input + " " + '\n');
                    ControlSocket.close();
                    System.out.println("Exiting");
                    System.exit(0);

                } else {
                    System.out.println("Invalid command");
                }
            }

        } else {
            System.out.println("You must connect to a server");
            System.exit(1);
        }

    }
}
