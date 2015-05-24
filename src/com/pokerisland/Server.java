package com.pokerisland;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;


public class Server extends Thread
{
	ServerSocket ss;

	//GUI Variables
	JTextArea serverLog;
	JFrame window;
	JLabel ipAddress;
	JButton refreshButton;

	//Things to manage
	Database db;
	ArrayList<String> words = new ArrayList<String>();

	//private parts
	Hashtable<String, NetworkedPlayer> players = new Hashtable<String, NetworkedPlayer>();
	//logged in players only
	Hashtable<Integer, Table> tables = new Hashtable<Integer, Table>(); 
	//all tables are stored server side
	DateFormat df = new SimpleDateFormat("h:mm");
	//used to format the date for timestamps
	
	Hashtable<String, Boolean> playerLogout;
	//only used when shutting the server down
	
	//temporary variables
	JButton button;

	public static void main(String[] args)
	{
		new Server();
	}

	public Server()
	{
		ss = null;
		instantiateTables();
		createGUI();
		loadWords();

		updateIPAddress();
		db = new Database(this);
		start();
	}

	/**
	 * Finds and sets the Server's IP Address to the Internal IP address
	 */
	private void updateIPAddress()
	{
		try
		{
			/*
			URL toCheckIp = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(toCheckIp.openStream()));
			String ip = in.readLine();
			 */

			String ip = InetAddress.getLocalHost().getHostAddress();
			ipAddress.setText("Server IP Address: " + ip);
			print("Server Refreshed");
		} catch (IOException e)
		{
			ipAddress.setText("ERROR: You are offline");
			//e.printStackTrace();
			print("You are offline!");
		}
	}

	private String generateRandomName()
	{
		if(words.size() >= 3) //confirm that the words have been loaded in and there are enough words
		{
			String name = new String();

			//adds four random words to the name
			for(int i = 0; i < 3; i++)
			{
				int index = (int)(Math.random() * words.size());
				name += words.get(index);
				words.remove(index); //to ensure that no two guests can have the same name
				//we remove this from the list of available words
				//there are 5000+ so it's not a big deal.
			}

			if(name.length() >= 12)
			{
				return generateRandomName();
			}
			else
			{
				return name;
			}
		}
		else
		{
			loadWords(); //reload the words list
			return generateRandomName();
		}
	}

	private void loadWords()
	{
		print("Loading in words...");
		try
		{
			BufferedReader wordReader = new BufferedReader(new FileReader(Constants.randomWordList));
			for(String line = wordReader.readLine(); !(line == null || line.equals("") ); line = wordReader.readLine())
			{
				//format the word properly
				line = Utils.titleCase(line);
				words.add(line);
			}
			wordReader.close();
		}
		catch (FileNotFoundException e)
		{
			this.print("Error reading in words file");
		}
		catch(IOException ioe)
		{
			this.print("Error reading in words file");
		}
		print("Finished loading in words");
	}

	private void instantiateTables()
	{
		//adding 4 tables 
		int[] bigBlinds = {2,5,10};
		int[] capacities = {6,2,9};
		for(int i = 0; i < 7; i++)
		{
			tables.put(i+1, new Table(i+1, bigBlinds[i%3], capacities[i%3]));
			tables.get(i+1).start();
		}
	}

	private void createGUI()
	{
		window = new JFrame("Poker Island Server");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(400,400);
		
		JPanel panel =  new JPanel();
		panel.setLayout(new BorderLayout());
		serverLog = new JTextArea();
		serverLog.setEditable(false);
		serverLog.setLineWrap(true);
		//make it so that serverLog automatically scrolls down when a new string is appended
		((DefaultCaret)serverLog.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scrollPane = new JScrollPane(serverLog);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
		{
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				((DefaultCaret)serverLog.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			}
		});
		panel.add(scrollPane, BorderLayout.CENTER);


		button = new JButton("Click Me");
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Server.this.doButtonAction();
			}
		});

		panel.add(button, BorderLayout.SOUTH);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		ipAddress = new JLabel("Loading IP Address...");

		refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				updateIPAddress();
			}
		});

		topPanel.add(ipAddress);
		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(refreshButton);
		panel.add(topPanel, BorderLayout.NORTH);

		window.add(panel);
		window.setVisible(true);
	}

	/**
	 * Waits for clients to connect to the Server socket
	 */
	public void run()
	{
		try
		{
			ss = new ServerSocket(Constants.serverPort);
			print("Waiting for a client to connect");
			while(true)
			{
				Socket s = ss.accept();
				//blocking statement to wait for a connection
				NetworkedPlayer np = new NetworkedPlayer(this, s);
				new Thread(np).start();
				print(np.getIPString() + " connected."); 
			}
		}
		catch(IOException ioe)
		{
			print("IOException in Server.run(): "+ioe.getMessage());
		}
		finally
		{
			if(ss != null)
			{
				try
				{
					ss.close();
				}
				catch(IOException ioe)
				{
					print(ioe.getMessage());
				}
			}
		}
	}

	/**
	 * Made to easily change what the "Click Me" button does in the server
	 * <pre><ul>@code private void doButtonAction()</pre></ul>
	 */
	private void doButtonAction()
	{
		print("Random Name: " + Server.this.generateRandomName());
	}

	/**
	 * Removes the specified {@code NetworkedPlayer} from the players array
	 * <pre><ul>{@code public void removeNetworkedPlayer(NetworkedPlayer np)}</pre></ul>
	 * @param np The {@code NetworkedPlayer} to remove 
	 */
	public void removeNetworkedPlayer(NetworkedPlayer np)
	{
		String user = np.getUsername();
		if(user != null && players.contains(user))
			players.remove(np.getUsername());
	}

	/**
	 * Prints the specified message to the {@code Server} log
	 * <pre><ul>{@code public void print(String message)}</pre></ul>
	 * @param message the {@code String} to display
	 */
	public void print(String message)
	{

		Date date = new Date();
		//need to create a new one every time because there's no update function

		serverLog.append("(" + df.format(date) + ") " + message + '\n');

		((DefaultCaret)serverLog.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		//just make sure we're updating to the bottom

	}

	/**
	 * Parses a message given by a {@code NetworkedPlayer} and runs the proper function
	 *  <pre><ul>{@code public void parse(NetworkedPlayer np, String message)}</pre></ul>
	 * @param np the {@code NetworkedPlayer} that sent the message to be parsed
	 * @param message the {@code String} to be parsed
	 */
	public void parse(NetworkedPlayer np, String message)
	{
		//output to the log
		if(!message.contains("getTables"))
		{
			this.print(np.getName() + " sent: " + "\"" + message+ "\"");
			
		}

		String[] info = message.split(";");
		//creates the array to be analyzed


		boolean found = false;
		//found is used to figure out whether or not to display an error message after server parsing
		//when an the message tag is found, found is set to true
		//if found is false, an error is displayed in the server log
		
		if (info.length == 4)
		{
			if (info[0].equals("game") || info[0].equals("table")) //getting a game-related message from client (such as raise, fold etc.) and handling it
			{
				found = true;
				parseGameMessage(np, message);
			}
			else if (info[0].equals("joinQuickplay"))
			{
				found = true;
				parseQuickplayMessage(np, info);
			}
			else if(info[0].equals("createGuestAccount"))
			{
				found = true;
				parseCreateGuestAccountMessage(np, info);
			}
		}
		else if(info.length == 3)
		{
			if(info[0].equals("changePassword"))
			{
				found = true; 
				parseChangePasswordMessage(np, info);
			}
			if(info[0].equals("login"))	//user is trying to log in	
			{
				found = true;
				parseLoginMessage(np, info);
			}
			else if(info[0].equals("createAccount")) //user is trying to create an account
			{
				found = true;
				parseCreateAccountMessage(np, info);
			}
			else if(info[0].equals("logout"))
			{
				found = true;
				parseLogoutMessage(np, info);
			}
			else if(info[0].equals("updateDB"))
			{
				found = true;
				parseUpdateDbMessage(np, info);
			}
		}//end info length = 3
		else if(info.length == 1)
		{
			if (info[0].equals("createGuest")) //user wants to log in as a guest
			{
				found = true;
				parseCreateGuest(np, info);
			}
			else if(info[0].equals("getTables")) //user wants updated table info
			{
				found = true;
				parseGetTablesMessage(np, info);
			}
			
		}

		if(!found)
		{
			this.print("Error Parsing Message from " + np.getUsername() + ": " + message);
		}
	}
	
	synchronized private void parseUpdateDbMessage(NetworkedPlayer np, String[] info)
	{
		np.setNetWorth(Integer.parseInt(info[2]));
		if(!np.getIsGuest()) //only update the db if the user is not a guest
			db.changeNetWorth(info[1], Integer.parseInt(info[2]));
	}

	private void parseLogoutMessage(NetworkedPlayer np, String[] info)
	{
		print("Logging out " + np.getUsername());
		int totalWorth = Integer.parseInt(info[2]);
		db.changeNetWorth(info[1], totalWorth);
		np.logout();
	}
		
	private void parseGameMessage(NetworkedPlayer np, String info) {
		/* SYNTAX
		 * game;tableID;action;extraInfo
		 * EXAMPLES
		 * game;4;bets;20
		 * game;1;folds;gucci
		 * game;7;raises;195
		 * game;2;calls;gucci
		 * table;4;joins;400
		 */
		this.tables.get(Integer.parseInt(info.split(";")[1])).message(info.split(";")[0].equals("game"), info, np);
	}

	private void parseLoginMessage(NetworkedPlayer np, String[] info)
	{
		//info[1] is username
		//info[2] is password (hashed)
		if(db.checkUserCredentials(info[1], info[2])) // checks the username/password combination with the database
		{
			//SUCCESSFUL LOGIN
			print(info[1]+" successfully logged in"); //print to the log for confirmation

			//update the player
			np.setUsername(info[1]);
			np.setNetWorth(db.getUserNetWorth(info[1]));
			np.setIsGuest(false);
			players.put(np.getUsername(), np);

			np.sendMessage("login;success;"+info[1]+";"+db.getUserNetWorth(info[1])); //send the message
		}
		else
		{
			//error
			print("Failure");
			np.sendMessage("login;failure;empty;empty");
			//let the client know the login was a failure
		}
	}

	private void parseCreateAccountMessage(NetworkedPlayer np, String[] info)
	{
		//info[1] is username
		//info[2] is password (hashed)
		this.print("Attempting to create account with the following information:");
		print("Username: " + info[1] + '\n' + "Password (hash): " + info[2]);
		//fairly self explanatory
		boolean success = db.createUser(info[1], info[2]);
		//tells the database to attempt to create a user with the given information

		if(success)
		{
			print("User successfully created");
			np.setUsername(info[1]);
			np.setNetWorth(Constants.startingNetWorth);
			np.sendMessage(info[0] + ";" + info[1] + ";" + info[2]); //let the client know that the account was successfully made
		}
		else
		{
			print("User creation failed");
			np.sendMessage("createAccount;failed;empty"); //let the client know the account was unsuccessfully made
			//empty because createAccount flag is under message array lengths of 3
		}
	}
	
	/**
	 * Parses a message from the client signifying that a guest wants to create an account
	 * @param np the NetworkedPlayer to interact with
	 * @param info data
	 */
	private void parseCreateGuestAccountMessage(NetworkedPlayer np, String[] info)
	{
		//info[1] is username
		//info[2] is password (hashed)
		this.print("Attempting to create account with the following information:");
		print("Username: " + info[1] + '\n' + "Password (hash): " + info[2] + '\n' + Constants.moneyName + ": " +info[3]);
		//fairly self explanatory
		boolean success = db.createUser(info[1], info[2]);
		//tells the database to attempt to create a user with the given information

		if(success)
		{
			print("User successfully created");
			np.setUsername(info[1]);
			np.setNetWorth(Integer.parseInt(info[3]));
			np.sendMessage(info[0] + ";" + info[1] + ";" + info[2] + ";" + info[3]); //let the client know that the account was successfully made
		}
		else
		{
			print("User creation failed");
			np.sendMessage("createGuestAccount;failed;empty;empty"); //let the client know the account was unsuccessfully made
			//empty because createAccount flag is under message array lengths of 4
		}
	}

	private void parseQuickplayMessage(NetworkedPlayer np, String[] info)
	{
		//info[1] is tableSize
		//info[2] is blindSize
		//info[3] is numTables
		this.print("Finding appropriate table/s...");
		int desiredTableSize = Integer.parseInt(info[1]);
		int blindDesired = Integer.parseInt(info[2]); 
		int numTablesDesired = Integer.parseInt(info[3]); 

		ArrayList<Table> matchingTables = new ArrayList<Table>();
		for (int i = 1; i <= tables.size(); i++)
		{
			if (tables.get(i).getCapacity() == desiredTableSize && tables.get(i).getBlindLevel() == blindDesired)
			{
				matchingTables.add(tables.get(i));
			}
			if (matchingTables.size() == numTablesDesired)
			{
				break;
			}
		}

		String quickplayResponse = "quickplay;";
		for (int i = 0; i < matchingTables.size(); i++)
		{
            quickplayResponse += matchingTables.get(i).getTableID() + ";" + matchingTables.get(i).getBlindLevel() + ";";
		}
		quickplayResponse = quickplayResponse.substring(0, quickplayResponse.length()-1);
		print(quickplayResponse);
		np.sendMessage(quickplayResponse);
	}

	private void parseCreateGuest(NetworkedPlayer np, String[] info)
	{
		String username = generateRandomName();
		this.print("Creating guest account with the following information:");
		print("Username: " + username);

		//set the proper info on the np
		np.setIsGuest(true);
		np.setUsername(username);
		np.setNetWorth(Constants.startingNetWorth);
		players.put(np.getUsername(), np);


		//create the message to send
		//info[1] is username
		//info[2] is net worth
		String userInfoMessage = "loginGuest;" + username + ";" + Constants.startingNetWorth + ";";
		np.sendMessage(userInfoMessage); //let the client know that the account was successfully made

	}

	private void parseChangePasswordMessage(NetworkedPlayer np, String[] info)
	{
		//info[1] is username
		//info[2] is newPassword
		db.changePassword(info[1],info[2]);
	}

	private void parseGetTablesMessage(NetworkedPlayer np, String[] info)
	{
		for(int i : this.tables.keySet())
		{
			np.sendMessage("refreshLobbyTables;"+tables.get(i).getTableID()+";"+((tables.get(i).getBlindLevel())/2)+" / "+tables.get(i).getBlindLevel()+";"+tables.get(i).getNumOfPlayers()+"/"+tables.get(i).getCapacity());
		}
	}
}//end class


