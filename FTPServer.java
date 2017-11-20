import java.io.*;
import java.net.*;
import java.util.*;

public final class FTPServer {

    private static ServerSocket welcomeSocket;

    public static void main(String argv[]) throws Exception {
		try {
			welcomeSocket = new ServerSocket(5568);
		} catch (IOException ioEx) {
			System.out.println("Unable to set up port!");
			System.exit(1);
		}
		System.out.println("[FTPServer] Server running on port: 5568");
		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			System.out.println("[Client] Connected to " + connectionSocket.getRemoteSocketAddress().toString());

			// Create ClientHandler thread to handle client
			ClientHandler handler = new ClientHandler(connectionSocket);
			handler.start();
		}
	}
}

class ClientHandler extends Thread {

	private Socket clientSocket;
	private Scanner input;

	public ClientHandler(Socket socket) {
        //Set up reference to associated socket
		clientSocket = socket;

		try
		{
			input = new Scanner(clientSocket.getInputStream());
		}
		catch(IOException ioEx)
		{
			ioEx.printStackTrace();
		}
	}

	public void run() {
		String fromClient;
		String clientCommand;
		byte[] data;
		String frstln;

		do {
			// read in initial command line from client
			fromClient = input.nextLine();

			StringTokenizer tokens = new StringTokenizer(fromClient);

			frstln = tokens.nextToken();
			int port = Integer.parseInt(frstln);
			clientCommand = tokens.nextToken();

			//if the command is "close", end this thread
			if(clientCommand.equals("close")){
				endConnection();
				return;
			}

			try {
			    if (clientCommand.equals("list:")) {
				// connect to client's Data Socket
				Socket dataSocket = new Socket(clientSocket.getInetAddress(), port);
				DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
				System.out.println("[List] Opened data socket on port: " + port);
				// get local files
				File folder = new File(".");
				File[] listOfFiles = folder.listFiles();
				String temp;
				// get data for each file
				for (int i = 0; i < listOfFiles.length; i++) {
				    if (listOfFiles[i].isFile()) {
					temp = listOfFiles[i].getName() + '\n';
					data = temp.getBytes();
					dataOutToClient.write(data, 0, data.length);
				    }
				}
				dataOutToClient.close();
				dataSocket.close();
				System.out.println("[List] Closed data socket on port: " + port);
			    }
			    else if(clientCommand.equals("retr")) {
				clientCommand = tokens.nextToken();
				Socket dataSocket = new Socket(clientSocket.getInetAddress(), port);
				DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
				
				System.out.println("[Retr] Opened data socket on port: " + port);
				File openFile = new File(clientCommand);
				if (openFile.exists()) {
				    byte[] buffer = new byte[8192];
				    BufferedInputStream in = new BufferedInputStream(new FileInputStream(clientCommand));
				    int count;
				    while((count = in.read(buffer)) > 0) {
					dataOutToClient.write(buffer, 0, count);
				    }
				    
				    in.close();
				    dataOutToClient.close();
				    dataSocket.close();
				    System.out.println("[Retr] Closed data socket on port: " + port);
				}
			    }
			    else if(clientCommand.equals("stor")) {
				clientCommand = tokens.nextToken();
				Socket dataSocket = new Socket(clientSocket.getInetAddress(), port);
				BufferedInputStream dataFromClient = new BufferedInputStream(new DataInputStream(dataSocket.getInputStream()));
				
				System.out.println("[Stor] Opened data socket on port: " + port);
				FileOutputStream file = new FileOutputStream(new File(clientCommand));
				byte[] buffer = new byte[8192];
				int count;
				while((count = dataFromClient.read(buffer)) > 0) {
				    file.write(buffer, 0, count);
				}
				file.close();
				dataFromClient.close();
				dataSocket.close();
				System.out.println("[Stor] Closed data socket on port: " + port);
			    }
			} catch (IOException ex) {
			    ex.printStackTrace();
			}
		} while (true);
	}
    
    /**
     * Closes the thread
     */
    private void endConnection() {
	System.out.println("[Quit] Disconnecting from client "+clientSocket.getRemoteSocketAddress().toString());
	input.close();
	try {
	    clientSocket.close();
	} catch(IOException ioEx) {
	    System.out.println("Unable to disconnect!");
	}
	System.out.println("[Quit] Disconnected from client");
    }
}
