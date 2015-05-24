package com.pokerisland;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.pokerisland.Constants;

public class Database
{
	// JDBC driver name and database URL
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	private final String DB_URL = "jdbc:mysql://pokerid.cwrxzkqo1kn1.us-west-2.rds.amazonaws.com/poker?useUnicode=true&characterEncoding=UTF-8";

	//  Database credentials
	private String USER = null;
	private String PASS = null;

	Connection conn = null;
	Statement stmt = null;
	
	//server to output messages
	Server server;
	
	public Database(Server server)
	{
		this.server = server;
		readCredentials();
		
		connectToDatabase();
	}

	public Database ()
	{
		readCredentials();
		connectToDatabase();
	}
	
	/**
	 * Reads in the database credentials file and stores it in the appropriate variables
	 */
	private void readCredentials()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(Constants.credentialsFileName));
			USER = reader.readLine();
			PASS = reader.readLine();
			reader.close();
			
		}
		catch (FileNotFoundException e)
		{
			server.print("Database credentials not found!");
		}
		catch(IOException ioe)
		{
			server.print("An error occurred while reading in database credentials!");
		}
	}
	
	private void connectToDatabase()
	{
		try
		{
			//Register JDBC driver
			Class.forName(JDBC_DRIVER);

			//Open a connection
			if(server == null)
				System.out.println("Connecting to database...");
			else
				server.print("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			//Execute a query
			if(server == null)
				System.out.println("Connected");
			else
				server.print("Connected to database");
			
			stmt = conn.createStatement();
		}
		catch(SQLException sql)
		{
			System.out.println("SQLException: " + sql.getMessage());
		}
		catch(ClassNotFoundException cnfe)
		{
			System.out.println("ClassNotFoundException: " + cnfe.getMessage());
		}
	}
	
	/**
	 * Checks whether the specified username and password combination are valid
	 * @param username the username
	 * @param password the user's password
	 * @return Whether or not the two match
	 */
	public synchronized boolean checkUserCredentials(String username, String password)
	{
		try
		{
			ResultSet results = stmt.executeQuery("SELECT * FROM poker.users WHERE username = \"" + username + "\" AND password = \"" + password +"\"");
			if(results.isBeforeFirst())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(SQLException sql)
		{
			System.out.println("SQLException: " + sql.getMessage());
			return false;
		}
	}
	
	/**
	 * Creates a user with specified username and password
	 * @param username the username
	 * @param password the user's password
	 * @return whether or not account creation was successful
	 */
	public synchronized boolean createUser(String username, String password)
	{
		try
		{
			stmt.executeUpdate("INSERT INTO users " + "VALUES ('0', \"" + username + "\", \"" + password + "\", "+ Constants.startingNetWorth +")");	
			return true;
		}
		catch (SQLException se)
		{
			//Handle errors for JDBC
			//System.out.println("SQLException: " + se.getMessage());
			return false;
		}
		
	}
	/**
	 * Returns the net worth of the specified user in the database
	 * @param username the username to look up in the database
	 * @return the net worth of the user; returns -1 if there is an error
	 */
	public int getUserNetWorth(String username)
	{
		ResultSet resultSet;
		try 
		{
			resultSet = stmt.executeQuery("SELECT netWorth from poker.users where username like '"+username+"'");
			while (resultSet.next()) 
			{
				return resultSet.getInt(1);
			}
		} 
		catch (SQLException e) 
		{
			System.out.println("SQLException in Database.getUserNetWorth(): " + e.getMessage());
		}
		return -1;
	}
	
	/**
	 * Outputs the database for debugging purposes
	 */
	public void outputDatabase()
	{
		// getting data from a table
		try
		{
			
			String tableToGet = "users";
			System.out.println("\nGetting database contents (\"" + tableToGet + "\" table):");
			ResultSet resultSet = stmt.executeQuery("SELECT * from " + tableToGet);
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			while (resultSet.next()) 
			{
				for (int i = 1; i <= columnsNumber; i++) 
				{
					if (i > 1) System.out.print(",  ");
					String columnValue = resultSet.getString(i);
					System.out.print(rsmd.getColumnName(i) + ": " + columnValue);
				}
				System.out.println("");
			}
		}
		catch (SQLException se)
		{
			//Handle errors for JDBC
			System.out.println("SQLException: " + se.getMessage());
		}
		catch (Exception e)
		{
			//Handle errors for Class.forName
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	/**
	 * Changes the specified user's password to the password
	 * @param username The user changing their password
	 * @param password The new password (hashed)
	 */
	public void changePassword(String username, String password)
	{
		try
		{
			stmt.executeUpdate("UPDATE users SET password ='"+password+"' WHERE username='"+username+"'");
		}
		catch(SQLException sql)
		{
			server.print("SQLException: " + sql.getMessage());
		}	
	}
	
	/**
	 * Updates a user's net worth to the amount specified
	 * @param username the user
	 * @param newAmount the amount specified
	 */
	public void changeNetWorth(String username, int newAmount)
	{
		try
		{
			stmt.executeUpdate("UPDATE users SET netWorth ='"+newAmount+"' WHERE username='"+username+"'");
		}
		catch(SQLException sql)
		{
			server.print("SQLException: " + sql.getMessage());
		}
	}
	
	public static void main (String [] args)
	{
		Database db = new Database();
		//db.changeNetWorth("noel", 9001);
		db.outputDatabase();
	}
}
