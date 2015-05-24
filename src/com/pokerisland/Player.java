package com.pokerisland;

public class Player
{
	protected int netWorth;
	protected String username;
	protected boolean isGuest;
	
	public Player()
	{
		username = null;
	}
	
	public Player(String username, int netWorth)
	{
		this.username = username;
		this.netWorth = netWorth;
	}
	public void setNetWorth(int netWorth)
	{
		this.netWorth = netWorth;
	}
	public void setUsername(String username)
	{
		this.username = username;
	}
	public void setIsGuest(boolean isGuest)
	{
		this.isGuest = isGuest;
	}
	public int getNetWorth()
	{
		return netWorth;
	}
	public String getUsername()
	{
		return username;
	}
	public boolean getIsGuest()
	{
		return isGuest;
	}
	
	public void logout()
	{
		isGuest = false;
		username = null;
		netWorth = 0;
	}
}
