import java.io.IOException;
import java.net.*;
import java.util.*;

public class Server {
	private List<ServerThread> serverThreads;
	private ServerSocket ss;

	public Server(int port)
	{
		try	{
			System.out.println("Starting the main server");
			ss = new ServerSocket(port);
			serverThreads = new ArrayList<>();
			while(true){
				Socket s = ss.accept();   //  Accept the incoming request
				ServerThread st = new ServerThread(s, this);
				System.out.println("New Guest Comes In");
				serverThreads.add(st);
			}
		}
		catch (Exception ex) {}
		finally {
			try {
				ss.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String [] args)
	{
		new Server(6789);
	}
}
