import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;

public final class Host {
    private static Socket ControlSocket;

    public static void main(String args[]) throws Exception {
	String server = "127.0.0.1";
	String username = "jstone";
	String hostname = "127.0.0.1";
	String itype = "T1";
	int port = 5568;

	try {
	    ControlSocket = new Socket(server, port);
	} catch(IOException ioEx) {
	    System.out.println("Unable to connect to " + server + ":" + port);
	    System.exit(1);
	}

	BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	while (true) {
	    DataOutputStream toServer = new DataOutputStream(ControlSocket.getOutputStream());
	    toServer.writeBytes("info" + " " + username + " " + hostname + " " + itype + '\n');
	    System.out.print(">> ");
	    String input = inFromUser.readLine();
	    toServer.writeBytes("close");
	    ControlSocket.close();
	    System.exit(0);
	}
    }
}
