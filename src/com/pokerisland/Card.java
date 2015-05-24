package com.pokerisland;

//the names of card .png images will be formatted as the int value
//of the Suit followed by the int value of the Rank. For example,
//the image of a king of hearts would be "213.png"

public class Card 
{
	private Suit suit;
	private Rank rank;	
	/**
	 * Instantiates a {@code Card} object
	 * @param  rank  The {@code Card} object's rank (ACE, ONE, TWO, etc) 
	 * @param  suit  The {@code Card} object's suit (CLUB, DIAMOND, etc)
	 */
	public Card(Rank rank, Suit suit)
	{
		this.rank = rank;
		this.suit = suit;
	}
	public Rank getRank()
	{
		return rank;
	}
	public Suit getSuit()
	{
		return suit;
	}
	
	public String toString() {
		String total = "";
		if (this.rank.value() == 1) {
			total += "A";
		} else if (this.rank.value() == 13) {
			total += "K";
		} else if (this.rank.value() == 12) {
			total += "Q";
		} else if (this.rank.value() == 11) {
			total += "J";
		} else if (this.rank.value() == 10) {
			total += "T";
		} else {
			total += ("" + this.rank.value());
		}
		return total + (this.suit.toString().charAt(0)+"").toLowerCase();
	}
}