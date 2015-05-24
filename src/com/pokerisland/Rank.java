package com.pokerisland;

public enum Rank 
{
	ACE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), JACK(11), QUEEN(12), KING(13);
	private final int value;
	Rank(int value)
	{
		this.value = value;
	}
	public int value()
	{
		return value;
	}
	public static Rank valueOf(int value)
	{
		if(value < 1 || value > 13)
		{
			System.err.println("Error: Out-of-bounds access of Rank");
		}
		switch (value)
		{
			case 1:
				return ACE;
			case 2:
				return TWO;
			case 3:
				return THREE;
			case 4:
				return FOUR;
			case 5:
				return FIVE;
			case 6:
				return SIX;
			case 7:
				return SEVEN;
			case 8:
				return EIGHT;
			case 9:
				return NINE;
			case 10:
				return TEN;
			case 11:
				return JACK;
			case 12:
				return QUEEN;
			default:
				return KING;
		}
	}
}
