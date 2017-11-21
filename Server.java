import java.io.*;
import java.net.*;
import java.util.*;

public final class Server {
	private static ServerSocket welcomeSocket;

	public static void main(String args[]) throws Exception {
		UserTable userTable = new UserTable();
		FileTable fileTable = new FileTable();
		int port = 5568;
		try {
			welcomeSocket = new ServerSocket(port);
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
			System.exit(1);
		}
		System.out.println("Server running on port: " + port);
		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			HostHandler handler = new HostHandler(connectionSocket, userTable, fileTable);
			handler.start();
		}
	}
}

class HostHandler extends Thread {
	private PrintWriter output;
	private UserTable userTable;
	private FileTable fileTable;
	private Socket clientSocket;
	private Scanner input;
	private String username;

	public HostHandler(Socket socket, UserTable userTable, FileTable fileTable) {
		clientSocket = socket;
		this.userTable = userTable;
		this.fileTable = fileTable;
		try {
			input = new Scanner(clientSocket.getInputStream());
			output = new PrintWriter(clientSocket.getOutputStream(), true);
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}

	public void run() {
	tring fromClient;
		String command;
		do {
			fromClient = input.nextLine();
			StringTokenizer tokens = new StringTokenizer(fromClient);
			command = tokens.nextToken();

			if (command.equals("close")) {
				endConnection();
				return;
			}

			username = tokens.nextToken();
			String hostname = tokens.nextToken();
			String itype = tokens.nextToken();

			userTable.addUser(username, hostname, itype);
			output.println("Username and respective data added. Transferring files.");

			fromClient = input.nextLine();
			tokens = new StringTokenizer(fromClient);
			command = tokens.nextToken();
			int dataConnPort;

			try {
				dataConnPort = Integer.parseInt(tokens.nextToken());
			} catch (NumberFormatException e1) {
				System.out.println("Invalid port number. Aborting user registration.");
				output.println("Invalid port number. Aborting user registration.");
				userTable.removeUser(username);
				continue;
			}

			try {
				/* establish data connection */
				Socket dataSocket = new Socket(clientSocket.getInetAddress(), dataConnPort);
				ObjectInputStream inputStream = new ObjectInputStream(dataSocket.getInputStream());
				ArrayList<String> fileList;
				try {
					fileList = (ArrayList<String>) inputStream.readObject();
					for (String file : fileList) {
						fileTable.addFile(username, file, userTable.getName(username));
					}
					System.out.println("File list uploaded...");
				} catch (Exception e) {
					System.out.println("Could not fetch files.");
				}

				dataSocket.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				output.println(e.getMessage());
				userTable.removeUser(username);
				System.out.println("Failed to establish data connection.");
			}

			if (command.contentEquals("search")) {
				dataConnPort = Integer.parseInt(tokens.nextToken());
				String keyword = tokens.nextToken();

				ArrayList<String> results = fileTable.searchByKeyword(keyword);

				System.out.println("Search complete.\n" + results.size() + " results found. \n");

				try {
					// get our data connection going
					Socket dataSocket = new Socket(clientSocket.getInetAddress(), dataConnPort);

					ObjectOutputStream outputStream = new ObjectOutputStream(dataSocket.getOutputStream());

					outputStream.writeObject(results);

					dataSocket.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} while (true);

	
	}

	private void endConnection() {
		userTable.removeUser(username);
		System.out.println("Removing " + username);
		try {
			clientSocket.close();
			input.close();
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
		System.out.println("User " + clientSocket.getRemoteSocketAddress().toString() + " has left");
	}
}
	
	
class UserTable {
	private Map<String, String[]> userData;
	
	public UserTable() {
		userData = new HashMap<String, String[]>();
	}
	
	public void addUser(String username, String hostname, String itype) {
		String data[] = new String[2];
		data[0] = hostname;
		data[1] = itype;
		userData.put(username, data);
	}
	
	public void removeUser(String u) {
		userData.remove(u);
	}	
	
	public String getName(String username) {
		String s[] = userData.get(username);
		return s[0];
	}
}

class FileTable {

	private Map<String, String[]> fileData;
	
	public FileTable() {
		fileData = new HashMap<String, String[]>();
	}
	
	public void addFile(String username, String file, String userServer) {
		String data[] = new String[2];
		data[0] = file;
		data[1] = userServer;
		fileData.put(username, data);
	}

        public ArrayList<String> searchByKeyword(String keyword) {
		ArrayList<String> result = new ArrayList<String>();
		
		for (Map.Entry<String, String[]> entry : fileData.entrySet()) {
			String username = entry.getKey();
			String[] data = entry.getValue();
			if (data[0].contains(keyword)) {
				String toAdd = fileData.get(username)[0];
				result.add(toAdd);
			}
		}
		
		return result;
	}
}

