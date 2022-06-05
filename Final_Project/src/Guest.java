import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Guest extends Thread {
	
	private BufferedReader din;
	private PrintWriter dout;
	private Socket s;
	private String username = "";

	public Guest(String hostname, int port)
	{
		try	{
			System.out.println("Welcome to OPMusic");
			s = new Socket(hostname, port);
			din = new BufferedReader(new InputStreamReader(s.getInputStream()));
			dout = new PrintWriter(s.getOutputStream(), true);
			start();
			
			Scanner scan = new Scanner(System.in);
			
			while(true) {
				String line = scan.nextLine();
				String[] str = line.split(" ", 5);
				if (str[0].equals("quit")){
					try {dout.close(); din.close(); s.close();}
					catch (IOException ex) {ex.printStackTrace();}
					break;
				}
				else if (str[0].toLowerCase().equals("login")) {
					if (str.length == 3) { // check for invalid login input (Example: "login a b c d", "login a")
						username = str[1];
						dout.println(line);
					}
					else {System.out.println("Invalid Login Info. Please Try Again.");}
				}
				else if (str[0].toLowerCase().equals("register")){
					if (str.length == 3) {dout.println(line);}
					else {System.out.println("Invalid Register Info. Please Try Again.");}
				}
				else if (str[0].toLowerCase().equals("add_score")) {
					if (str.length == 3) {dout.println(line);}
					else {System.out.println("Invalid Input. Please Try Again.");}
				}
				else if (str[0].toLowerCase().equals("my_profile")) {
					dout.println(line + " " + username);
				}
				else if (str[0].toLowerCase().equals("music_library")) {
					dout.println(line);
				}
				else if (str[0].toLowerCase().equals("look_up")) {
					if (str.length == 2) {
						dout.println(line);
					}
					else {System.out.println("Please enter a valid username");}
				}
				else {
					System.out.println("Invalid Input. Please Try Again");
				}
			}
		}
		catch (IOException ex) {ex.printStackTrace();}
	}

	
	public void run()
	{
		try {
			while(true)	{
				String line = din.readLine();
				System.out.println(line);
			}
		}
		catch (IOException ex) {System.out.println("Connection closed");}
	}

	public static void main(String [] args)
	{
		new Guest("localhost", 6789);
	}
}