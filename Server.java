import java.io.*;
import java.net.*;
import java.util.*;

public final class Server {
	private static ServerSocket welcomeSocket;
    public static UserTable userTable;
    public static FileTable fileTable;

	public static void main(String args[]) throws Exception {
		userTable = new UserTable();
		fileTable = new FileTable();
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
			HostHandler handler = new HostHandler(connectionSocket);
			handler.start();
		}
	}
}

class HostHandler extends Thread {
	private PrintWriter output;
	private Socket clientSocket;
	private Scanner input;
	private String username;

	public HostHandler(Socket socket) {
		clientSocket = socket;
		try {
			input = new Scanner(clientSocket.getInputStream());
			output = new PrintWriter(clientSocket.getOutputStream(), true);
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
			int dataConnPort;

			if (command.equals("close") || command.equals("c")) {
				endConnection();
				return;
			}
			else if(command.equals("info")){
			    username = tokens.nextToken();
			    String hostname = tokens.nextToken();
			    String itype = tokens.nextToken();

			    Server.userTable.addUser(username, hostname, itype);
			    System.out.println(username + " added.");
			    //output.println("Username and respective data added. Transferring files.");
			    continue;
			}
			else if(command.equals("list")){
			    String portString = tokens.nextToken();
			    try {
				dataConnPort = Integer.parseInt(portString);
			    } catch (NumberFormatException e1) {
				System.out.println("Invalid port number. Aborting user registration.");
				output.println("Invalid port number. Aborting user registration.");
				Server.userTable.removeUser(username);
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
					if(file != null){
					    Server.fileTable.addFile(username, file, Server.userTable.getName(username), Server.userTable.getItype(username));
					}
				    }
				    System.out.println("File list uploaded...");
				} catch (Exception e) {
				    System.out.println("Could not fetch files.");
				}

				dataSocket.close();
			    } catch (Exception e) {
				System.out.println(e.getMessage());
				output.println(e.getMessage());
				Server.userTable.removeUser(username);
				System.out.println("Failed to establish data connection.");
			    }
			}
			else if (command.contentEquals("search")) {
				dataConnPort = Integer.parseInt(tokens.nextToken());
				String keyword = tokens.nextToken();

				ArrayList<ArrayList<String>> results = Server.fileTable.searchByKeyword(keyword);

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
		Server.userTable.removeUser(username);
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

    public String getItype(String username) {
	String s[] = userData.get(username);
	return s[1];
    }
}

class FileTable {

	private ArrayList<ArrayList<String>> fileData;
	
	public FileTable() {
		fileData = new ArrayList<ArrayList<String>>();
	}
	
    public void addFile(String username, String file, String userServer, String itype) {
	    ArrayList<String> fileObject = new ArrayList<String>();
	    fileObject.add(0, file);
	    fileObject.add(1, username);
	    fileObject.add(2, userServer);
	    fileObject.add(3, itype);
	    fileData.add(fileObject);
	}

        public ArrayList<ArrayList<String>> searchByKeyword(String keyword) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		
		for (ArrayList<String> object: fileData) {
		    if (object.get(0).contains(keyword)) {
			result.add(object);
		    }
		}
		
		return result;
	}
}


