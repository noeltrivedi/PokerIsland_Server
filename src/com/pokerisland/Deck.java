package com.pokerisland;

import java.util.Collections;
import java.util.ArrayList;

public class Deck 
{
	private ArrayList<Card> cards;
	int topCard;
	/**
	 * Creates a {@code Deck} object containing 52 {@code Card} objects in order (a standard deck)
	 * <pre><ul>{@code public Deck()}</ul></pre>
	 */
	public Deck()
	{
		topCard = 0;
		cards = new ArrayList<Card>();
		for(int a = 0; a < Suit.size(); a++)
		{
			for(int b = 1; b < 14; b++)
			{
				cards.add(new Card(Rank.valueOf(b), Suit.valueOf(a)));
			}
		}
	}
	/**
	 * Randomizes the order of the {@code Card} objectss in the {@code Deck}
	 * <pre><ul>{@code public void shuffle()}</ul></pre>
	 */
	public void shuffle()
	{
		topCard = 0;
		Collections.shuffle(cards);
	}
	/**
	 * Takes and returns the {@code Card} from the top of the {@code Deck}
	 * <pre><ul>{@code public Card dealCard()}</ul></pre>
	 * @return the top Card to be dealt
	 */
	public Card dealCard()
	{
		return cards.get(topCard++);
	}
	
	public Card getCard(String desiredCard) { // "Ah," "Tc," "2s" etc
		for (Card card : this.cards) {
			if (card.toString().equals(desiredCard)) {
				return card;
			}
		}
		return null;
	}
	/*public static void main(String[] args)
	{
		Deck deck = new Deck();
		deck.shuffle();
		for(int i = 0; i < 4; i++)
		{
			Card c1 = deck.dealCard();
			System.out.println("Card dealt is a "+c1.getRank()+" of "+c1.getSuit());
		}
		for(int i = 0; i < 52; i++)
		{
			System.out.println("Card at "+i+" is a "+deck.cards.get(i).getRank()+" of "+deck.cards.get(i).getSuit());
		}
	}*/
}