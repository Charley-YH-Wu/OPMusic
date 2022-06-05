import java.io.*;
import java.net.Socket;
import java.sql.*;

public class ServerThread extends Thread {
	private PrintWriter dout;
	private BufferedReader din;
	private Server sv;
	private String user = null;

	public ServerThread(Socket s, Server sv)
	{
		try	{
			this.sv = sv;
			dout = new PrintWriter(s.getOutputStream(), true);
			din = new BufferedReader(new InputStreamReader(s.getInputStream()));
			start();
		}
		catch (IOException ex) {ex.printStackTrace();}
	}
	
	public void printDatabase() {
		String Message;
		String db = "jdbc:mysql://localhost/FinalProject";
		String user = "root";
		String pwd = "root";
		String sql = "select * from songList order by overallscore DESC";
		Message = "Music Browser: ";
		try (Connection conn = DriverManager.getConnection(db, user, pwd);
			  Statement stmt = conn.prepareStatement(sql);) {
			
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Message = Message + "\n" + rs.getString("songName") + "        " + rs.getInt("overallScore");
			}		
		} catch (SQLException sqle) {
			System.out.println ("SQLException: " + sqle.getMessage());
		}
		dout.println(Message);		
	}
	
	public int login(String username, String password) {
		String db = "jdbc:mysql://localhost/FinalProject";
		String user = "root"; String pwd = "root";
		String sql = "{CALL login(?,?,?)}";
		int count = 0;
		try (Connection conn = DriverManager.getConnection(db, user, pwd);
			  CallableStatement cs = conn.prepareCall(sql);) {
			cs.setString(1, username);
			cs.setString(2, password);
			cs.registerOutParameter(3, Types.INTEGER);
			
			cs.executeUpdate();
			count = cs.getInt(3);	
		} catch (SQLException sqle) {System.out.println ("SQLException: " + sqle.getMessage());}
		System.out.println("Login request: " + count);
		return count;		
	}
	
	public int register(String username, String password) {
		String db = "jdbc:mysql://localhost/FinalProject";
		String user = "root"; String pwd = "root";
		String sql = "{CALL register_check(?,?)}";
		String sql2 = "INSERT INTO userCred (username, password) VALUES (?,?)";
		int exist = 0;
		int register = 0;
		try (Connection conn = DriverManager.getConnection(db, user, pwd);
			  CallableStatement cs = conn.prepareCall(sql);
			  PreparedStatement ps = conn.prepareStatement(sql2);) {
			cs.setString(1, username);
			cs.registerOutParameter(2, Types.INTEGER);
			cs.executeUpdate();
			exist = cs.getInt(2);	
			if (exist == 0) {
				//execute sql2
				ps.setString(1, username);
				ps.setString(2, password);
				int row = ps.executeUpdate();
				System.out.println("Row affected: " + row);
				register = 1;
			}
		} catch (SQLException sqle) {System.out.println ("SQLException: " + sqle.getMessage());}
		return register;
	}
	
	public int add_score(String song,String score,String username){
		String db = "jdbc:mysql://localhost/FinalProject";
		String user = "root"; String pwd = "root";
		float s = Float.parseFloat(score);
		if(s > 10.0 || s < 1.0)
		{
			dout.println("Score must be a number within the range: 1-10 inclusive");
			return 0;
		}
		int ex = 0;
		int songID = -1;
		String command = "select count(*),songID from songlist  where songName = " + "'" +song+"'";
		try (Connection conn = DriverManager.getConnection(db, user, pwd);
				 Statement cs = conn.createStatement();
				ResultSet rs = cs.executeQuery(command);)
			{
				if(rs.next()){
					ex = rs.getInt("count(*)");
					songID = rs.getInt("songID");
				}
				if(ex == 0)
				{
					dout.println("Sorry! That song is not in our Music Library! "
							+ "Please try your command again with a different song ");
					return 0;
				}
				
			}
			catch(SQLException e)
			{
				System.out.println("Problem with internal database 1");
				e.printStackTrace();
				return 0;
				
			}
		
		
		String sql = "insert into profiles (username,songName,score) values(?,?,?) on duplicate key update  score = values(score);";
	
		try (Connection conn = DriverManager.getConnection(db, user, pwd);
			 PreparedStatement cs = conn.prepareStatement(sql);)
		{
			System.out.println("accesing profile score data base");
			cs.setString(1, username);
			cs.setString(2, song);
			cs.setFloat(3,s);
			cs.executeUpdate();
			System.out.println("finished adding profile score to database");
			//dout.println("added score");
			recalcOverallScore(song,songID);
			return 1;
		}
		catch(SQLException e)
		{
			System.out.println("Problem with internal database 2");
			return 0;
		}
	}
	public int recalcOverallScore(String song,int id)
	{
		String command = "select score,songName from profiles where songName = " + "'" + song + "'";
		int numPeopleScoredThisSong = 0;
		float runningTotal = 0;
		String db = "jdbc:mysql://localhost/FinalProject";
		String user = "root"; String pwd = "root";
		try (Connection conn = DriverManager.getConnection(db, user, pwd);
				 Statement cs = conn.createStatement();
				ResultSet rs = cs.executeQuery(command);)
			{
		
				while(rs.next())
				{
					numPeopleScoredThisSong++;
					runningTotal += rs.getFloat("score");
				
				}
			
				
			}
			catch(SQLException e)
			{
				System.out.println("Problem with internal database 3");
				
				return 0;
				
			}
		
		float average = runningTotal / numPeopleScoredThisSong; 
		String command2 = "update songlist set overallScore = " + average + "where songID = " + "'" +id +  "'";
		try (Connection conn = DriverManager.getConnection(db, user, pwd);
				 Statement cs = conn.createStatement();
				)
			{
		
				cs.executeUpdate(command2);
			
				
			}
			catch(SQLException e)
			{
				System.out.println("Problem with internal database 4");
				
				return 0;
				
			}

		return 1;
	}

	
	public String displayProfile(String username) {
		String Message;
		String db = "jdbc:mysql://localhost/FinalProject";
		String user = "root";
		String pwd = "root";
		String sql = "SELECT songName, score FROM profiles WHERE username = ? ORDER BY score DESC";
		Message = "Profile: \nSong Name		Score ";
		try (Connection conn = DriverManager.getConnection(db, user, pwd);
			  PreparedStatement ps = conn.prepareStatement(sql);) {
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Message = Message + "\n" + rs.getString("songName") + "		        " + rs.getInt("score");
			}		
		} catch (SQLException sqle) {
			System.out.println ("SQLException: " + sqle.getMessage());
		}
		return Message;
	}
	
	public void run()
	{
		try {
			printDatabase();
			while(true){
				dout.println("\nWhat would you like to do next:\n"
						+ "		Type: login username password, to login in\n" 
						+ "		Type: register username password, to register\n" 
						+ "		Type: look_up username, to look up other people's profile\n"
						+ "Once you are logged in, you can: \n"
						+ "		Type: my_profile, to view your profile\n" 
						+ "		Type: add_score songname score, to give your score to a song and add it to your profile\n"
						+ "		Type: music_library, to view our entire music catalouge");
				String line = din.readLine(); // line == addscore song1 5	
				String[] str = line.split(" ", 5);
				if(line == null) break;
				else if (str[0].toLowerCase().equals("login")) {
					System.out.println("New login request");
					if (user != null) {
						dout.println("You are already logged in");
					}
					else {
						if (login(str[1], str[2]) == 1) { 
							//sv.getUser(str[1]);
							user = str[1];
							dout.println("You are logged in!");
						}
						else { dout.println("Login failed. Please try again.");}
					}
				}
				else if (str[0].toLowerCase().equals("register")) {
					System.out.println("New register request");
					if (user != null) {
						dout.println("You are already logged in");
					}
					else {
						if (register(str[1], str[2]) == 1) { 
							//sv.addNewUser(str[1]);
							dout.println("You are register!");		
							// sv.getUser(str[1]);
							user = str[1];
							dout.println("You are logged in!");						
						}
						else { dout.println("Username already exists. Please try again.");}
					}
				}
				else if (str[0].toLowerCase().equals("add_score")) {
					if (user == null) {
						dout.println("You must login in order to give a score");
					}
					else {
						System.out.println("Request to score: " + user + " " + str[1] + " " + str[2]);
						if(add_score(str[1],str[2], user) == 1)	{
							dout.println("You have scored ");
						}
						else {
							dout.println("Failed to add score \n");			
						}
					}
				}
				else if (str[0].toLowerCase().equals("my_profile")) {
					if (user != null){
						dout.println(displayProfile(user));					
					}
					else {
						dout.println("You must login in first");
					}
				}
				else if (str[0].toLowerCase().equals("music_library")) {
					printDatabase();
				}
				else if (str[0].toLowerCase().equals("look_up")){
					dout.println(displayProfile(str[1]));
				}
			}
		}
		catch (Exception ex) {System.out.println("Connection reset"); }
		finally {
			try {
				dout.close();
				din.close();
			}
			catch (Exception ex) {ex.printStackTrace();}
		}//finally
	}
}