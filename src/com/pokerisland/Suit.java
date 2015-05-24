package com.pokerisland;

public enum Suit 
{
	CLUB(0), DIAMOND(1), HEART(2), SPADE(3); //these values are in alphabetical order
	private final int value;
	Suit(int value)
	{
		this.value = value;
	}
	public int value()
	{
		return value;
	}
	public static Suit valueOf(int value)
	{
		if(value < 0 || value > 3)
		{
			System.out.println("Error: Out-of-bounds access of Suit");
		}
		switch (value)
		{
			case 0:
				return CLUB;
			case 1:
				return DIAMOND;
			case 2:
				return HEART;
			default:
				return SPADE;
		}
	}
	public static int size()
	{
		return 4;
	}
};

