package com.pokerisland;


public class TestSuite
{
 
	public static void main(String[] args)
	{
		createClientServer();
		//createSeveralClients();
	}
	
	public static void outputMaxInt()
	{
		System.out.println("Integer Max Value: " + Integer.MAX_VALUE);
	}
	
	public static void createClientServer()
	{
		new Server();
	}

}
