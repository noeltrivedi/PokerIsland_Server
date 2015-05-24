package com.pokerisland;

import java.util.ArrayList;

import javafx.util.Pair;

import com.pokerisland.Card;

public class SeatedPlayer {

	private Pair<Card, Card> holeCards;
	int stackSize;
	int maxWin = 0;
	boolean hasBB = false;
	boolean hasSB = false;
	boolean hasBTN = false;
	int seatPosition;
	String handHistory = "";
	ArrayList<String> actions;
	NetworkedPlayer user;
	int unreadActionMarker = 0;
	int betInFront = 0;

	/**
	 * Constructs a {@code SeatedPlayer}
	 * <pre><ul>{@code SeatedPlayer(NetworkedPlayer user, int stackSize, int seatPosition)}</ul></pre>
	 * @param user the NetworkedPlayer who is sitting at a table
	 * @param stackSize the amount of chips that this SeatedPlayer has
	 * @param seatPosition the seat position to place this SeatedPlayer at
	 */
	SeatedPlayer(NetworkedPlayer user, int stackSize, int seatPosition) {
		this.setStackSize(stackSize);
		this.actions = new ArrayList<String>();
		this.user = user;
		this.seatPosition = seatPosition;
	}

	void addEntitled(int amount) {
		this.maxWin += amount;
	}

	void resetForNextHand() {
		this.maxWin = 0;
		this.holeCards = null;
		this.actions.clear();
		this.unreadActionMarker = 0;
	}

	int doAction(String action, ArrayList<SeatedPlayer> players) {
		// add it to HH for this and other players
		String[] brokenMessage = action.split(";");
		this.unreadActionMarker++;
		if (brokenMessage[0].equals("calls") || brokenMessage[0].equals("bets") || brokenMessage[0].equals("raises")) {
			int chipsPutIn = Integer.parseInt(brokenMessage[1]) - (brokenMessage[0].equals("raises") ? this.betInFront : 0);
			for (SeatedPlayer player : players) {
				player.maxWin += Gameplay.min(chipsPutIn, player.getStackSize());
				//System.err.println(chipsPutIn);
				//System.out.println(player + " added " + Gameplay.min(chipsPutIn, player.getStackSize()));
			}
			this.stackSize -= chipsPutIn;
			if (Gameplay.debugMode) {
				System.out.println(this.getUsername() + " " + brokenMessage[0] + (brokenMessage[0].equals("raises") ? " to" : "") + " " + Integer.parseInt(brokenMessage[1]) + " and now has " + this.stackSize);
			}
			for (SeatedPlayer sp : players) {
				sp.sendMessage("handHistory;" + (this.getUsername() + " " + brokenMessage[0] + (brokenMessage[0].equals("raises") ? " to" : "") + " " + Integer.parseInt(brokenMessage[1]) + " and now has " + this.stackSize));
			}
			if (brokenMessage[0].equals("raises")) {
				this.betInFront = Integer.parseInt(brokenMessage[1]);
			} else {
				this.betInFront += Integer.parseInt(brokenMessage[1]);
			}
			return chipsPutIn;
		} else if (brokenMessage[0].equals("checks") || brokenMessage[0].equals("folds")) {
			if (Gameplay.debugMode) {
				System.out.println(this.getUsername() + " " + brokenMessage[0]);
			}
			for (SeatedPlayer sp : players) {
				sp.sendMessage("handHistory;" + (this.getUsername() + " " + brokenMessage[0]));
			}
			return 0;
		} else {
			System.err.println("Unrecognized action by player " + this + ": " + action);
			return 0;
		}
	}

	public boolean sendMessage(String m) {
		return this.user.sendMessage(m);
	}

	public void addToHandHistory(String handHistory) {
		this.handHistory += handHistory;
	}

	public void addAction(String s) {
		this.actions.add(s);
	}

	public int postBlind(int oneBB, ArrayList<SeatedPlayer> players) {
		int postAmt = Gameplay.min(this.getStackSize(), this.hasBB ? oneBB : (this.hasSB ? oneBB/2 : 0));
		this.stackSize -= postAmt;
		if (Gameplay.debugMode) {
			System.out.println(this.getUsername() + (postAmt == 0 ? "" : " has the " + (this.hasBB ? "BB" : "SB") + ",") + " posts " + (postAmt == 0 ? "no" : postAmt) + " chips and now has " + this.stackSize + " chips");
		}
		for (SeatedPlayer sp : players) {
			sp.sendMessage("handHistory;" + (this.getUsername() + (postAmt == 0 ? "" : " has the " + (this.hasBB ? "BB" : "SB") + ",") + " posts " + (postAmt == 0 ? "no" : postAmt) + " chips and now has " + this.stackSize + " chips"));
		}
		this.betInFront = postAmt;
		for (SeatedPlayer player : players) {
			player.maxWin += Gameplay.min(postAmt, player.getStackSize());
		}
		return postAmt;
	}

	public String getNextAction() {
		while (this.actions.isEmpty() || this.actions.size() <= this.unreadActionMarker) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		return this.actions.get(this.unreadActionMarker);
	}

	public Pair<Card, Card> getHoleCards() {
		return this.holeCards;
	}

	public int getStackSize() {
		return this.stackSize;
	}

	public void setHoleCards(Pair<Card, Card> holeCards, ArrayList<SeatedPlayer> players) {
		this.holeCards = holeCards;
		if (Gameplay.debugMode) {
			System.out.println(this.getUsername() + " was dealt " + this.getHoleCards().getKey() + ", " + this.getHoleCards().getValue());
		}
		for (SeatedPlayer sp : players) {
			sp.sendMessage("handHistory;" + (this.getUsername() + " was dealt " + this.getHoleCards().getKey() + ", " + this.getHoleCards().getValue()));
		}
	}

	public void setHoleCards(Card card1, Card card2, ArrayList<SeatedPlayer> players) {
		this.setHoleCards(new Pair<Card, Card>(card1, card2), players);
	}

	public void setStackSize(int stackSize) {
		this.stackSize = stackSize;
	}

	public String toString() {
		return this.getUsername() + ", who has " + (this.holeCards != null ? this.holeCards.toString().replace("=", " ") : "") + " and " + this.stackSize + " chips";
	}

	public String getUsername() {
		return this.user != null ? this.user.getUsername() : "SP" + this.seatPosition;
	}
	
	public NetworkedPlayer getNetworkedPlayer()
	{
		return user;
	}

}