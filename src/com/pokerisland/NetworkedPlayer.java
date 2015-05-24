package com.pokerisland;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;


public class NetworkedPlayer extends Player implements Runnable
{
	Server server; //server this player/client is connected to
	Socket s; //socket used for the connection to the client
	BufferedReader br;
	OutputStreamWriter bw;
	String IPString;
	//printwriter and bufferedreader are hooked up to a socket
	//socket is connected to *A* client

	//HashMap<Table, SeatedPlayer> playerTables;
	//The tables a player is seated at

	/*NetworkedPlayer(NetworkedPlayer np) {
		this(np.getUsername(), np.getNetWorth(), np.server, np.s);
	}*/

	/**A {@code NetworkedPlayer} is a Player with a socket allowing it to communicate with a Client
	 * <pre><ul>{@code NetworkedPlayer(String username, int netWorth, Server server, Socket s)}</pre></ul>
	 * @param username the {@code String} that represents this {@code NetworkedPlayer}'s username
	 * @param netWorth  the {@code int} that represents this {@code NetworkedPlayer}'s netWorth
	 * @param server the parent {@code Server} that this {@code NetworkedPlayer} uses for communication
	 * @param s the {@code Socket} that this  this {@code NetworkedPlayer} communicates through
	 */
	NetworkedPlayer(String username, int netWorth, Server server, Socket s)
	{
		super(username, netWorth);
		this.server = server;
		this.s = s;
		this.IPString = s.getInetAddress().getHostAddress() + ":"+ s.getPort();

		try
		{
			br = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
			bw = new OutputStreamWriter(
				    s.getOutputStream(), "UTF-8");
			//printwriter and bufferedreader are hooked up to a socket
			//socket is connected to *A* client
		}
		catch(IOException ioe)
		{
			server.print("IOE: " + ioe.getMessage());
		}
	}
	/**
	 * A {@code NetworkedPlayer} with only networking functionality
	 * To be used upon creation before the user logs in
	 * <pre><ul>{@code NetworkedPlayer(Server server, Socket s)}</pre></ul>
	 * @param server the parent {@code Server} that this {@code NetworkedPlayer} uses for communication
	 * @param s the {@code Socket} that this  this {@code NetworkedPlayer} communicates through
	 */
	NetworkedPlayer(Server server, Socket s)
	{
		this.netWorth = 0;
		this.username = null;
		this.server = server;
		this.s = s;
		this.IPString = s.getInetAddress().getHostAddress() + ":"+ s.getPort();

		try
		{
			br = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
			bw = new OutputStreamWriter(
				    s.getOutputStream(), "UTF-8");
			//printwriter and bufferedreader are hooked up to a socket
			//socket is connected to *A* client
		}
		catch(IOException ioe)
		{
			server.print("IOE: " + ioe.getMessage());
		}
	}


	/** run() is the threaded functionality that attempts to read messages from the client
	 * 
	 */
	@Override
	public void run()
	{
		try
		{
			while(true)
			{
				String line = br.readLine();
				//reads a line from the client
				//this line blocks until a line is sent
				if (line != null)
				{
					server.parse(this, line);
					//tells the server who sent the message and then sends it for parsing (At the server)
				}
				else
				{
					//line is null meaning there was some error
					server.print(getName() + " disconnected.");
					server.removeNetworkedPlayer(this);
					
					break;
				}
			}
		}
		catch(IOException ioe)
		{
			server.print(getName() + " disconnected.");
			server.removeNetworkedPlayer(this);
		}
	}

	/**Sends a message to the associated Client
	 * <pre><ul>{@code public boolean sendMessage(String message)}</pre></ul>
	 * @param message the {@code String} being sent to the client
	 * @return whether or not the message was successfully sent
	 */
	public boolean sendMessage(String message)
	{
		try 
		{
			bw.write(message+"\n");
			bw.flush();
		} 
		catch(SocketException se)
		{
			server.print("Attempted to send a message to " + getName() + " but they disconnected\nMessage: " + message);
			//server.removeAllTraces(this);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Gets the name to refer to this NetworkedPlayer by
	 * @return username if assigned, else IP address
	 */
	public String getName()
	{
		if(username == null)
			return getIPString();
		else
			return this.username;
	}
	
	public String getIPString()
	{
		return IPString;
	}

	/**
	 * Removes and resets a NetworkedPlayer
	 */
	public void logout()
	{
		server.removeNetworkedPlayer(this);
		//remove this np from the server list because
		//that is for logged in players only
		super.logout();
	}

	public String toString()
	{
		return this.getUsername() + " is at " + this.getIPString();
	}
	
	/*public int getTableWorth()
	{
		int tableWorth = 0;
		for(Table t : playerTables.keySet())
		{
			SeatedPlayer sp = playerTables.get(t);
			tableWorth += sp.getStackSize();
		}
		return tableWorth;
	}*/
}
