import java.io.*;
import java.net.*;
import java.util.*;


public final class Server {
    private static ServerSocket welcomeSocket;
    public static void main(String args[]) throws Exception {
	Users.data = new TreeMap<String, String>();
	int port = 5568;
	try {
	    welcomeSocket = new ServerSocket(port);
	} catch(IOException ioEx) {
	    ioEx.printStackTrace();
	    System.exit(1);
	}
	System.out.println("Server running on port: " + port);
	while(true) {
	    Socket connectionSocket = welcomeSocket.accept();
	    ClientHandler handler = new ClientHandler(connectionSocket);
	    handler.start();
	}
    }
}
class Users {
    public static Map<String, String> data;
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private Scanner input;
    private String username;

    public ClientHandler(Socket socket) {
	clientSocket = socket;
	try {
	    input = new Scanner(clientSocket.getInputStream());
	} catch (IOException ioEx) {
	    ioEx.printStackTrace();
	}
    }
    public void run() {
	String fromClient;
	String command;
	do {
	    fromClient = input.nextLine();
	    StringTokenizer tokens = new StringTokenizer(fromClient);
	    command = tokens.nextToken();
	    if(command.equals("close")) {
		endConnection();
		return;
	    }
	    username = tokens.nextToken();
	    String hostname = tokens.nextToken();
	    String itype = tokens.nextToken();

	    Users.data.put(username, hostname);

	    for(Map.Entry<String, String> entry : Users.data.entrySet()) {
		System.out.println(entry.getKey() + " => " + entry.getValue());
	    }
	} while(true);
	    
    }

    private void endConnection() {
	Users.data.remove(username);
	System.out.println("Removing " + username + " " + Users.data);
	try {
	    clientSocket.close();
	    input.close();
	} catch(IOException ioEx) {
	    ioEx.printStackTrace();
	}
	System.out.println("User " + clientSocket.getRemoteSocketAddress().toString() + " has left");
    }
}
