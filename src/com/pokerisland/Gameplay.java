package com.pokerisland;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javafx.util.Pair;


public class Gameplay {

	// NOTE: do not change debugMode to false -- I haven't actually ensured all debugMode messages are not necessary yet
	public static boolean debugMode = true;

	Card[] board = new Card[5];
	Deck deck;
	int capacity;
	int BB = 2;
	int SB = 1;
	int buttonPosition;

	HashMap<Integer, SeatedPlayer> players;
	boolean handInProgress;
	boolean isSettingUpNextHand;
	int pot = 0;

	int tableID;

	ArrayList<SeatedPlayer> playersToAdd;
	ArrayList<Integer> playersToRemove;

	private static final boolean callOption = true, checkOption = false;

	/**
	 * Creates a new game with specified blind level, capacity, and ID.
	 * @param blindLevel The big blind for this table. The small blind for this table is floor(blindLevel/2)
	 * @param capacity The maximum number of SeatedPlayers this game will allow to play in a given hand
	 * @param tableID The ID for this table
	 */
	public Gameplay(int blindLevel, int capacity, int tableID) {
		this.capacity = capacity;
		this.BB = blindLevel;
		this.SB = blindLevel/2;
		this.buttonPosition = 0;
		this.players = new HashMap<Integer, SeatedPlayer>();
		this.deck = new Deck(); // or this.createDeck();
		this.playersToAdd = new ArrayList<SeatedPlayer>();
		this.playersToRemove = new ArrayList<Integer>();
		this.tableID = tableID;
	}

	/**
	 * Sends a message to all {@code SeatedPlayers} in this instance of {@code Gameplay}. The message will be added to all of their hand histories.
	 * <pre><ul>{@code void sendToAllHH(Object... m)}</ul></pre>
	 * @param m The message to be sent regarding hand history
	 */
	void sendToAllHH(Object... m) {
		String toSend = "";
		for (Object part : m) {
			toSend+= part;
		}
		for (SeatedPlayer sp : this.players.values()) {
			sp.sendMessage(toSend);
		}
	}

	/**
	 * Sends a message to all {@code SeatedPlayers} in this instance of {@code Gameplay}.
	 * <pre><ul>{@code void sendToAll(Object... m)}</ul></pre>
	 * @param m The message to be sent
	 */
	void sendToAll(Object... m) {
		String toSend = "tableGUI;"+this.tableID;
		for (Object part : m) {
			toSend += ";"+part;
		}
		for (int key : this.players.keySet()) {
			this.players.get(key).sendMessage(toSend);
		}
	}

	/**
	 * Sends a message to one {@code SeatedPlayers} in this instance of {@code Gameplay}.
	 * <pre><ul>{@code void sendToOne(SeatedPlayer sp, Object... m)}</ul></pre>
	 * @param sp The {@code SeatedPlayer} to send the message to
	 * @param m The message to be sent
	 */
	void sendToOne(SeatedPlayer sp, Object... m) {
		String toSend = "tableGUI;"+this.tableID;
		for (Object part : m) {
			toSend += ";"+part;
		}
		sp.sendMessage(toSend);
	}

	/**
	 * Sends a message to all but one {@code SeatedPlayers} in this instance of {@code Gameplay}.
	 * <pre><ul>{@code void sendToAllMinusOne(SeatedPlayer sp, Object... m)}</ul></pre>
	 * @param sp The {@code SeatedPlayer} to NOT send the message to
	 * @param m The message to be sent
	 */
	void sendToAllMinusOne(SeatedPlayer sp, Object... m) {
		String toSend = "tableGUI;"+this.tableID;
		for (Object part : m) {
			toSend += ";"+part;
		}
		for (int key : this.players.keySet()) {
			if (sp.equals(this.players.get(key))) {

			} else {
				this.players.get(key).sendMessage(toSend);
			}
		}
	}

	/**
	 * Prepares the {@code Gameplay} for the next hand: Removes players from the table who are 
	 * leaving, adds players to the table who are trying to join the table, moves blinds to the
	 * next appropriate position, clears the board, and shuffles the deck.
	 * <pre><ul>{@code void nextHand()}</ul></pre>
	 */
	void nextHand() {
		this.isSettingUpNextHand = true;

		boolean reset = this.playersToAdd.size() > 0 || this.playersToRemove.size() > 0;
		for (int i = 0; i < this.playersToRemove.size(); i++) {
			//let the client know the player's networth upon exit
			SeatedPlayer sp = this.players.get(this.playersToRemove.get(i));
			if (sp != null) {
				sp.sendMessage("netWorth;"+sp.getStackSize());
				this.players.remove(sp.seatPosition);
				this.sendToAll("removePlayer", this.playersToRemove.get(i));
			}
		}
		this.playersToRemove.clear();

		ArrayList<SeatedPlayer> playersAdded = new ArrayList<SeatedPlayer>();
		for (int i = 0; i < this.playersToAdd.size(); i++) {
			if (this.capacity > this.players.size()) {
				this.players.put(this.playersToAdd.get(i).seatPosition, this.playersToAdd.get(i));
				for (int key : this.players.keySet()) {
					if (this.playersToAdd.get(i).equals(this.players.get(key))) {

					} else {
						this.sendToOne(this.playersToAdd.get(i), "addPlayer", key, this.players.get(key).getUsername(), this.players.get(key).getStackSize());
					}
				}
				this.sendToAll("addPlayer", this.playersToAdd.get(i).seatPosition, this.playersToAdd.get(i).getUsername(), this.playersToAdd.get(i).getStackSize());
				playersAdded.add(this.playersToAdd.get(i));
			} else {
				for (int j = i; j < this.playersToAdd.size(); j++) {
					//this.sendToOne(this.playersToAdd(j), "no ur not added dood");
				}
				break;
			}
		}

		for (SeatedPlayer addedPlayer : playersAdded) {
			this.playersToAdd.remove(addedPlayer);
		}

		if (this.players.size() < 2) {
			this.isSettingUpNextHand = false;
			return;
		}

		// Reset the board
		for (int b = 0; b < this.board.length; b++) {
			this.board[b] = null;
		}
		this.sendToAll("clearBoard");

		// shuffle the deck
		this.deck.shuffle();

		// MOVE BB, SB, BTN TO APPROPRIATE SEATS
		if (reset) {
			for (int key : this.players.keySet()) {
				this.players.get(key).hasBB = false;
				this.players.get(key).hasSB = false;
				this.players.get(key).hasBTN = false;
			}
			ArrayList<Integer> seatsList = new ArrayList<Integer>(this.players.keySet());
			if (this.players.size() > 2) { // 3+ PLAYERS
				this.players.get(seatsList.get(0)).hasBTN = true;
				this.players.get(seatsList.get(1)).hasSB = true;
				this.players.get(seatsList.get(2)).hasBB = true;
				this.sendToAll("setDealerButtonPosition", seatsList.get(0));
				this.sendToAll("setSmallBlindButtonPosition", seatsList.get(1));
				this.sendToAll("setBigBlindButtonPosition", seatsList.get(2));
				this.buttonPosition = seatsList.get(0);
			} else { // HU
				this.players.get(seatsList.get(0)).hasBTN = true;
				this.players.get(seatsList.get(0)).hasSB = true;
				this.players.get(seatsList.get(1)).hasBB = true;
				this.sendToAll("setDealerButtonPosition", seatsList.get(0));
				this.sendToAll("setSmallBlindButtonPosition", seatsList.get(0));
				this.sendToAll("setBigBlindButtonPosition", seatsList.get(1));
				this.buttonPosition = seatsList.get(0);
			}
		} else {
			ArrayList<Integer> seatsList = new ArrayList<Integer>(this.players.keySet());
			if (this.players.size() == 2) { // HU
				SeatedPlayer playerWithBB = 
						(this.players.get(seatsList.get(0)).hasBB ?	this.players.get(seatsList.get(0)) : this.players.get(seatsList.get(1)));
				SeatedPlayer playerWithSBAndBTN = 
						(this.players.get(seatsList.get(0)).equals(playerWithBB) ? this.players.get(seatsList.get(1)) :	this.players.get(seatsList.get(0)));
				playerWithBB		.hasBB	= false;
				playerWithBB		.hasSB	= true;
				playerWithBB		.hasBTN	= true;
				playerWithSBAndBTN	.hasSB	= false;
				playerWithSBAndBTN	.hasBTN	= false;
				playerWithSBAndBTN	.hasBB	= true;

				this.sendToAll("setDealerButtonPosition", playerWithBB.seatPosition);
				this.sendToAll("setSmallBlindButtonPosition", playerWithBB.seatPosition);
				this.sendToAll("setBigBlindButtonPosition", playerWithSBAndBTN.seatPosition);

				this.buttonPosition = playerWithBB.seatPosition;
			} else { // 3+ players
				for (int i = 0; i < seatsList.size(); i++) {
					if (this.players.get(seatsList.get(i)).hasBB) {
						SeatedPlayer playerUTG, playerWithBB, playerWithSB, playerWithBTN;
						// will be:		BB			 SB			  BTN			  CO/UTG
						playerUTG 		= this.players.get(seatsList.get((i+1)%seatsList.size()));
						playerWithBB 	= this.players.get(seatsList.get(i));
						playerWithSB 	= this.players.get(seatsList.get((i-1+seatsList.size())%seatsList.size()));
						playerWithBTN 	= this.players.get(seatsList.get((i-2+seatsList.size())%seatsList.size()));

						playerUTG		.hasBB 	= true;
						playerWithBB	.hasBB 	= false;
						playerWithBB	.hasSB	= true;
						playerWithSB	.hasSB	= false;
						playerWithSB	.hasBTN = true;
						playerWithBTN	.hasBTN = false;

						this.sendToAll("setDealerButtonPosition", playerWithSB.seatPosition);
						this.sendToAll("setSmallBlindButtonPosition", playerWithBB.seatPosition);
						this.sendToAll("setBigBlindButtonPosition", playerUTG.seatPosition);

						this.buttonPosition = playerWithSB.seatPosition;
						break;
					}
				}
			}	
		}
		this.isSettingUpNextHand = false;
	}

	/**
	 * Plays a hand. {@code void nextHAnd()} should be called before and after this method.
	 * <pre><ul>{@code public void playHand()}</ul></pre>
	 */
	public void playHand() {
		this.handInProgress = true;

		if (debugMode) {
			System.out.println("New Hand-----------------------------------------------------------");
		}
		this.sendToAllHH("handHistory;", "New Hand-----------------------------------------------------------");

		ArrayList<SeatedPlayer> playersInHand = new ArrayList<SeatedPlayer>();
		ArrayList<Integer> seats = new ArrayList<Integer>(this.players.keySet());
		Collections.sort(seats);
		int oopIndex = seats.indexOf(this.buttonPosition)+1;
		for (int i = 0; i < seats.size(); i++) {
			playersInHand.add(this.players.get(seats.get((i+oopIndex)%seats.size())));
			playersInHand.get(i).resetForNextHand();
			this.sendToAll("setPlayerAction", playersInHand.get(i).seatPosition, " ");
			this.sendToAll("hidePlayerCards", playersInHand.get(i).seatPosition);
		}

		// DEAL CARDS

		for (SeatedPlayer player : playersInHand) {
			player.setHoleCards(this.deck.dealCard(), this.deck.dealCard(), playersInHand);
		}

		for (SeatedPlayer player : playersInHand) {
			this.sendToAllMinusOne(player, "setPlayerCards", player.seatPosition, 0, 0, 0, 0);
			this.sendToOne(player, "setPlayerCards", player.seatPosition, player.getHoleCards().getKey().getRank().value(), player.getHoleCards().getKey().getSuit().value(),
					player.getHoleCards().getValue().getRank().value(), player.getHoleCards().getValue().getSuit().value());
		}

		// PREFLOP-------------------------------------------------------------------------------
		if (debugMode) {
			System.out.println("\nPreflop:");
			this.sendToAllHH("handHistory;", "Preflop:");
			for (SeatedPlayer player : playersInHand) {
				System.out.println(player);
			}
		}

		for (SeatedPlayer player : playersInHand) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			this.addToPot(player.postBlind(this.BB, playersInHand));
			if (player.hasSB) {
				this.sendToAll("putInChips", player.seatPosition, Gameplay.min(player.getStackSize(), this.SB));
				this.sendToAll("setPlayerAction", player.seatPosition, "Posts " + Gameplay.min(player.getStackSize(), this.SB));
				player.betInFront = this.SB;
				this.sendToAll("setPlayerStackSize", player.seatPosition, player.getStackSize());
			} else if (player.hasBB) {
				this.sendToAll("putInChips", player.seatPosition, Gameplay.min(player.getStackSize(), this.BB));
				this.sendToAll("setPlayerAction", player.seatPosition, "Posts " + Gameplay.min(player.getStackSize(), this.BB));
				player.betInFront = this.BB;
				this.sendToAll("setPlayerStackSize", player.seatPosition, player.getStackSize());
			}
		}

		if (this.preflopAction(playersInHand)) {

			try {
				Thread.sleep(500);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			// /PREFLOP-------------------------------------------------------------------------------

			// FLOP-----------------------------------------------------------------------------------
			this.board[0] = this.deck.dealCard();
			this.board[1] = this.deck.dealCard();
			this.board[2] = this.deck.dealCard();

			this.sendToAll("showFlopCards",
					this.board[0].getRank().value(), this.board[0].getSuit().value(),
					this.board[1].getRank().value(), this.board[1].getSuit().value(),
					this.board[2].getRank().value(), this.board[2].getSuit().value()
					);

			if (debugMode) {
				System.out.print("\nDealing flop:");
				this.sendToAllHH("handHistory;", "Dealing flop:");
				for (Card card : this.board) {
					if (card != null) {
						System.out.print(" " + card);
						this.sendToAllHH("handHistory;", card.toString());
					}
				}
				System.out.println();
			}

			if (this.postflopAction(playersInHand)) {

				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}

				// /FLOP----------------------------------------------------------------------------------

				// TURN-----------------------------------------------------------------------------------
				this.board[3] = this.deck.dealCard();

				this.sendToAll("showTurnCard",
						this.board[3].getRank().value(), this.board[3].getSuit().value()
						);

				if (debugMode) {
					System.out.print("\nDealing turn:");
					this.sendToAllHH("handHistory;", "Dealing turn:");
					for (Card card : this.board) {
						if (card != null) {
							System.out.print(" " + card);
							this.sendToAllHH("handHistory;", card.toString());
						}
					}
					System.out.println();
				}

				if (this.postflopAction(playersInHand)) {

					try {
						Thread.sleep(500);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}

					// /TURN----------------------------------------------------------------------------------

					// RIVER----------------------------------------------------------------------------------
					this.board[4] = this.deck.dealCard();

					this.sendToAll("showRiverCard",
							this.board[4].getRank().value(), this.board[4].getSuit().value()
							);

					if (debugMode) {
						System.out.print("\nDealing river:");
						this.sendToAllHH("handHistory;", "Dealing river:");
						for (Card card : this.board) {
							if (card != null) {
								System.out.print(" " + card);
								this.sendToAllHH("handHistory;", card.toString());
							}
						}
						System.out.println();
					}
					if (this.postflopAction(playersInHand)) {

						try {
							Thread.sleep(500);
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}

						// /RIVER---------------------------------------------------------------------------------

						// SHOWDOWN-------------------------------------------------------------------------------

						if (debugMode) {
							System.out.println("\nShowdown:");
						}
						this.sendToAllHH("handHistory;", "Showdown:");

						for (SeatedPlayer sp : playersInHand) {
							this.sendToAll("setPlayerCards", sp.seatPosition, 
									sp.getHoleCards().getKey().getRank().value(), 
									sp.getHoleCards().getKey().getSuit().value(),
									sp.getHoleCards().getValue().getRank().value(),
									sp.getHoleCards().getValue().getSuit().value()
									);
							this.sendToAll("setPlayerAction", sp.seatPosition, "Shows " + sp.getHoleCards().getKey().toString() + " " + sp.getHoleCards().getValue().toString());
							this.sendToAllHH("handHistory;", sp.getUsername() + " shows " + sp.getHoleCards().getKey().toString() + " " + sp.getHoleCards().getValue().toString());
						}
						
						Set<SeatedPlayer> winners = this.getBestHands(playersInHand);
						for (SeatedPlayer winner : winners) {
							if (winners.isEmpty()) {
								break;
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException ie) {
								ie.printStackTrace();
							}
							int winAmt = this.pot/winners.size();
							winner.setStackSize(winner.getStackSize() + winAmt);
							if (debugMode) {
								System.out.println(winner + ", wins " + winAmt + " chip" + (winAmt > 1 ? "s" : ""));
							}
							this.sendToAll("setPlayerAction", winner.seatPosition, "Wins " + winAmt);
							this.sendToAllHH("handHistory;", (winner.getUsername() + ", wins " + winAmt + " chip" + (winAmt > 1 ? "s" : "")));
							if (winAmt <= 0) {
								System.err.println("uh oh");
							}
							this.pot -= winAmt;
							this.sendToAll("takeOutChips", winner.seatPosition, winAmt);
							this.sendToAll("setPotAmount", this.pot);
							this.sendToAll("setPlayerStackSize", winner.seatPosition, winner.getStackSize());
						}

						/*while (this.pot > 1) {
							Set<SeatedPlayer> winners = this.getBestHands(playersInHand);
							if (playersInHand.isEmpty() || winners.isEmpty()) {
								break;
							}
							for (SeatedPlayer winner : winners) {
								try {
									Thread.sleep(2000);
								} catch (InterruptedException ie) {
									ie.printStackTrace();
								}
								int winAmt = Gameplay.min(winner.maxWin/winners.size(), this.pot);
								winner.setStackSize(winner.getStackSize() + winAmt);
								if (debugMode) {
									System.out.println(winner + ", wins " + winAmt + " chip" + (winAmt > 1 ? "s" : ""));
								}
								this.sendToAll("setPlayerAction", winner.seatPosition, "Wins " + winAmt);
								this.sendToAllHH("handHistory;", (winner.getUsername() + ", wins " + winAmt + " chip" + (winAmt > 1 ? "s" : "")));
								if (winAmt <= 0) {
									System.err.println("uh oh");
								}
								this.pot -= winAmt;
								this.sendToAll("takeOutChips", winner.seatPosition, winAmt);
								this.sendToAll("setPotAmount", this.pot);
								this.sendToAll("setPlayerStackSize", winner.seatPosition, winner.getStackSize());
							}
							playersInHand.removeAll(winners);
						}*/
						this.sendToAll("setPotAmount", 0);
						this.pot = 0;

						for (SeatedPlayer player : playersInHand) {
							if (player.getStackSize() <= 0) {
								this.playersToRemove.add(player.seatPosition);
							}
						}

						// /SHOWDOWN------------------------------------------------------------------------------

						try {
							Thread.sleep(3000);
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}

						this.handInProgress = false;
					} else {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}

						playersInHand.get(0).setStackSize(playersInHand.get(0).getStackSize() + this.pot);
						if (debugMode) {
							System.out.println(playersInHand.get(0) + ", wins " + this.pot + " chips");
						}
						this.sendToAll("setPlayerAction", playersInHand.get(0).seatPosition, playersInHand.get(0).getUsername()+" wins " + this.pot + " chips");
						this.sendToAllHH("handHistory;", playersInHand.get(0)+" wins " + this.pot + " chips");
						this.sendToAll("takeOutChips", playersInHand.get(0).seatPosition, this.pot);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}
						this.sendToAll("setPlayerStackSize", playersInHand.get(0).seatPosition, playersInHand.get(0).getStackSize());
						this.pot = 0;
						this.handInProgress = false;
					}
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}

					playersInHand.get(0).setStackSize(playersInHand.get(0).getStackSize() + this.pot);
					if (debugMode) {
						System.out.println(playersInHand.get(0) + ", wins " + this.pot + " chips");
					}
					this.sendToAll("setPlayerAction", playersInHand.get(0).seatPosition, playersInHand.get(0).getUsername()+" wins " + this.pot + " chips");
					this.sendToAllHH("handHistory;", playersInHand.get(0)+" wins " + this.pot + " chips");
					this.sendToAll("takeOutChips", playersInHand.get(0).seatPosition, this.pot);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
					this.sendToAll("setPlayerStackSize", playersInHand.get(0).seatPosition, playersInHand.get(0).getStackSize());
					this.pot = 0;
					this.handInProgress = false;
				}
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}

				playersInHand.get(0).setStackSize(playersInHand.get(0).getStackSize() + this.pot);
				if (debugMode) {
					System.out.println(playersInHand.get(0) + ", wins " + this.pot + " chips");
				}
				this.sendToAll("setPlayerAction", playersInHand.get(0).seatPosition, playersInHand.get(0).getUsername()+" wins " + this.pot + " chips");
				this.sendToAllHH("handHistory;", playersInHand.get(0)+" wins " + this.pot + " chips");
				this.sendToAll("takeOutChips", playersInHand.get(0).seatPosition, this.pot);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				this.sendToAll("setPlayerStackSize", playersInHand.get(0).seatPosition, playersInHand.get(0).getStackSize());
				this.pot = 0;
				this.handInProgress = false;
			}
		} else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}

			playersInHand.get(0).setStackSize(playersInHand.get(0).getStackSize() + this.pot);
			if (debugMode) {
				System.out.println(playersInHand.get(0) + ", wins " + this.pot + " chips");
			}
			this.sendToAll("setPlayerAction", playersInHand.get(0).seatPosition, playersInHand.get(0).getUsername()+" wins " + this.pot + " chips");
			this.sendToAllHH("handHistory;", playersInHand.get(0)+" wins " + this.pot + " chips");
			this.sendToAll("takeOutChips", playersInHand.get(0).seatPosition, this.pot);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			this.sendToAll("setPlayerStackSize", playersInHand.get(0).seatPosition, playersInHand.get(0).getStackSize());
			this.pot = 0;
			this.handInProgress = false;
		}
	}

	private void addToPot(int chips) {
		this.pot += chips;
	}

	private boolean postflopAction(ArrayList<SeatedPlayer> playersInHand) {

		// edge cases: players are all in, legal raises
		// side pots
		// set minimum and maximum buy in for buyInGUI

		boolean cont = true;
		int previousBet = 0;
		int currentBet = 0;
		boolean waitingOnAction = true;
		HashMap<SeatedPlayer, Boolean> players = new HashMap<SeatedPlayer, Boolean>();
		int numOfNonZeroStackedPlayers = 0;
		for (SeatedPlayer player : playersInHand) {
			players.put(player, true);
			if (player.getStackSize() > 0) numOfNonZeroStackedPlayers++;
		}
		if (numOfNonZeroStackedPlayers > 1) {
			// cool, we do hand action
		} else {
			return true;
		}
		int actionOnIndex = 0;
		SeatedPlayer playa = playersInHand.get(actionOnIndex%playersInHand.size());

		while (waitingOnAction) {
			playa = playersInHand.get(actionOnIndex%playersInHand.size());
			if (playersInHand.size() == 1) {
				waitingOnAction = false;
				cont = false;
				break;
			} else if (playa.getStackSize() > 0) {
				if (playa.betInFront < currentBet) { // fold, call, raise
					int minBet = Gameplay.max(2*currentBet-previousBet, this.BB);
					int maxBet = playa.getStackSize()+playa.betInFront;
					if (maxBet < minBet) {
						minBet = playa.getStackSize();
						maxBet = minBet;
					}
					this.sendToOne(playa, "setCallAmount", Gameplay.min(currentBet-playa.betInFront, playa.getStackSize()));
					this.sendToOne(playa, "setMinimumBetAmount", minBet);
					this.sendToOne(playa, "setMaximumBetAmount", maxBet);
					this.sendToAll("setPlayerAction", playa.seatPosition, "Thinking...");
					this.sendToOne(playa, "setButtonsVisible", true, Gameplay.callOption);
					if (playa.getNextAction().contains("folds")) {
						players.remove(playa);
						actionOnIndex = actionOnIndex%playersInHand.size();
						playersInHand.remove(playa);
						this.sendToAll("hidePlayerCards", playa.seatPosition);
						this.sendToAll("setPlayerAction", playa.seatPosition, "folds");
						if (playersInHand.size() == 1) {
							cont = false;
							waitingOnAction = false;
							break;
						}
					} else if (playa.getNextAction().contains("calls")) {
						players.put(playa, false);
						this.sendToAll("putInChips", playa.seatPosition, Integer.parseInt(playa.getNextAction().split(";")[1]));
						this.sendToAll("setPlayerAction", playa.seatPosition, playa.getNextAction().replace(";", " "));
						// this.sendToAll("setPlayerStackSize", playa.seatPosition, playa.getStackSize()-Integer.parseInt(playa.getNextAction().split(";")[1]));
						actionOnIndex++;
					} else if (playa.getNextAction().contains("raises")) {
						for (SeatedPlayer player : playersInHand) {
							if (player.getStackSize() > 0) {
								players.put(player, true);
							}
						}
						players.put(playa, false);
						this.sendToAll("putInChips", playa.seatPosition, Integer.parseInt(playa.getNextAction().split(";")[1])-playa.betInFront);
						this.sendToAll("setPlayerAction", playa.seatPosition, playa.getNextAction().replace(";", " to "));
						// this.sendToAll("setPlayerStackSize", playa.seatPosition, playa.getStackSize()+playa.betInFront-Integer.parseInt(playa.getNextAction().split(";")[1]));
						previousBet = currentBet;
						currentBet = Integer.parseInt(playa.getNextAction().split(";")[1]);
						actionOnIndex++;
					}
					this.addToPot(playa.doAction(playa.getNextAction(), playersInHand));
					this.sendToAll("setPlayerStackSize", playa.seatPosition, playa.getStackSize());
				} else if (playa.betInFront > currentBet) {
					if (debugMode) {
						System.err.println("whaaat");
						System.err.println(playa.betInFront);
						System.err.println(currentBet);
						System.err.println(playa);
						System.err.println(actionOnIndex);
					}
					actionOnIndex++;
				} else if (currentBet == playa.betInFront || currentBet == 0) { // fold, check, bet
					int minBet = Gameplay.min(playa.getStackSize(), this.BB);
					int maxBet = playa.getStackSize()+playa.betInFront;
					if (minBet > maxBet) {
						minBet = maxBet;
					}
					this.sendToOne(playa, "setMinimumBetAmount", minBet);
					this.sendToOne(playa, "setMaximumBetAmount", maxBet);
					this.sendToAll("setPlayerAction", playa.seatPosition, "Thinking...");
					this.sendToOne(playa, "setButtonsVisible", true, Gameplay.checkOption);
					if (playa.getNextAction().contains("folds")) {
						// because we remove them from playersInHand, we don't need to add to actionOnIndex
						players.remove(playa);
						actionOnIndex = actionOnIndex%playersInHand.size();
						playersInHand.remove(playa);
						this.sendToAll("hidePlayerCards", playa.seatPosition);
						this.sendToAll("setPlayerAction", playa.seatPosition, "folds");
						if (playersInHand.size() == 1) {
							cont = false;
							waitingOnAction = false;
							break;
						}
					} else if (playa.getNextAction().contains("checks")) {
						players.put(playa, false);
						this.sendToAll("setPlayerAction", playa.seatPosition, "checks");
						actionOnIndex++;
					} else if (playa.getNextAction().contains("bets")) {
						for (SeatedPlayer player : playersInHand) {
							if (player.getStackSize() > 0) {
								players.put(player, true);
							}
						}
						players.put(playa, false);
						this.sendToAll("putInChips", playa.seatPosition, Integer.parseInt(playa.getNextAction().split(";")[1]));
						this.sendToAll("setPlayerAction", playa.seatPosition, playa.getNextAction().replace(";", " "));
						this.sendToAll("setPlayerStackSize", playa.seatPosition, playa.getStackSize());
						previousBet = currentBet;
						currentBet = Integer.parseInt(playa.getNextAction().split(";")[1]);
						actionOnIndex++;
					}
					this.addToPot(playa.doAction(playa.getNextAction(), playersInHand));
					this.sendToAll("setPlayerStackSize", playa.seatPosition, playa.getStackSize());
				}
				waitingOnAction = false;
				for (Boolean bool : players.values()) {
					if (bool) {
						waitingOnAction = true;
						//break;
					}
				}
				if (!waitingOnAction) {
					for (SeatedPlayer player : playersInHand) {
						player.betInFront = 0;
					}
				}
			} else {
				int playables = 0;
				for (SeatedPlayer player : playersInHand) {
					if (player.getStackSize() > 0 && players.get(player)) {
						playables++;
					}
				}
				waitingOnAction = playables > 1;
				actionOnIndex++;
			}
		}

		for (SeatedPlayer player : playersInHand) {
			player.betInFront = 0;
		}
		return cont;
	}

	static int min(int a, int b) {
		return a > b ? b : a;
	}

	static int max(int a, int b) {
		return a > b ? a : b;
	}

	private boolean preflopAction(ArrayList<SeatedPlayer> playersInHand) {
		boolean cont = true;
		int previousBet = 0;
		int currentBet = this.BB;
		boolean waitingOnAction = true;
		HashMap<SeatedPlayer, Boolean> players = new HashMap<SeatedPlayer, Boolean>();
		int actionOnIndex = 0;
		for (SeatedPlayer player : playersInHand) {
			players.put(player, true);
			if (player.hasBB) {
				actionOnIndex = playersInHand.indexOf(player)+1;
			}
		}
		SeatedPlayer playa = playersInHand.get(actionOnIndex%playersInHand.size());

		while (waitingOnAction) {
			playa = playersInHand.get(actionOnIndex%playersInHand.size());
			if (playersInHand.size() == 1) {
				waitingOnAction = false;
				cont = false;
				break;
			} else if (playa.getStackSize() > 0) {
				if (playa.betInFront < currentBet) { // fold, call, raise
					int minBet = Gameplay.max(2*currentBet-previousBet, this.BB);
					int maxBet = playa.getStackSize()+playa.betInFront;
					if (maxBet < minBet) {
						minBet = playa.getStackSize();
						maxBet = minBet;
					}
					this.sendToOne(playa, "setCallAmount", Gameplay.min(currentBet-playa.betInFront, playa.getStackSize()));
					this.sendToOne(playa, "setMinimumBetAmount", minBet);
					this.sendToOne(playa, "setMaximumBetAmount", maxBet);
					this.sendToAll("setPlayerAction", playa.seatPosition, "Thinking...");
					this.sendToOne(playa, "setButtonsVisible", true, Gameplay.callOption);
					if (playa.getNextAction().contains("folds")) {
						players.remove(playa);
						actionOnIndex = actionOnIndex%playersInHand.size();
						playersInHand.remove(playa);
						this.sendToAll("hidePlayerCards", playa.seatPosition);
						this.sendToAll("setPlayerAction", playa.seatPosition, "folds");
						if (playersInHand.size() == 1) {
							cont = false;
							waitingOnAction = false;
							break;
						}
					} else if (playa.getNextAction().contains("calls")) {
						players.put(playa, false);
						this.sendToAll("putInChips", playa.seatPosition, Integer.parseInt(playa.getNextAction().split(";")[1]));
						this.sendToAll("setPlayerAction", playa.seatPosition, playa.getNextAction().replace(";", " "));
						// this.sendToAll("setPlayerStackSize", playa.seatPosition, playa.getStackSize()-Integer.parseInt(playa.getNextAction().split(";")[1]));
						actionOnIndex++;
					} else if (playa.getNextAction().contains("raises")) {
						for (SeatedPlayer player : playersInHand) {
							if (player.getStackSize() > 0) {
								players.put(player, true);
							}
						}
						players.put(playa, false);
						this.sendToAll("putInChips", playa.seatPosition, Integer.parseInt(playa.getNextAction().split(";")[1])-playa.betInFront);
						this.sendToAll("setPlayerAction", playa.seatPosition, playa.getNextAction().replace(";", " to "));
						// this.sendToAll("setPlayerStackSize", playa.seatPosition, playa.getStackSize()+playa.betInFront-Integer.parseInt(playa.getNextAction().split(";")[1]));
						previousBet = currentBet;
						currentBet = Integer.parseInt(playa.getNextAction().split(";")[1]);
						actionOnIndex++;
					}
					this.addToPot(playa.doAction(playa.getNextAction(), playersInHand));
					this.sendToAll("setPlayerStackSize", playa.seatPosition, playa.getStackSize());
				} else if (playa.betInFront > currentBet) {
					if (debugMode) {
						System.err.println("whaaat");
						System.err.println(playa.betInFront);
						System.err.println(currentBet);
						System.err.println(playa);
						System.err.println(actionOnIndex);
					}
					actionOnIndex++;
				} else if (currentBet == playa.betInFront || currentBet == 0) { // fold, check, bet
					int minBet = Gameplay.min(playa.getStackSize(), this.BB);
					int maxBet = playa.getStackSize()+playa.betInFront;
					if (minBet > maxBet) {
						minBet = maxBet;
					}
					this.sendToOne(playa, "setMinimumBetAmount", minBet);
					this.sendToOne(playa, "setMaximumBetAmount", maxBet);
					this.sendToAll("setPlayerAction", playa.seatPosition, "Thinking...");
					this.sendToOne(playa, "setButtonsVisible", true, Gameplay.checkOption);
					if (playa.getNextAction().contains("folds")) {
						// because we remove them from playersInHand, we don't need to add to actionOnIndex
						players.remove(playa);
						actionOnIndex = actionOnIndex%playersInHand.size();
						playersInHand.remove(playa);
						this.sendToAll("hidePlayerCards", playa.seatPosition);
						this.sendToAll("setPlayerAction", playa.seatPosition, "folds");
						if (playersInHand.size() == 1) {
							cont = false;
							waitingOnAction = false;
							break;
						}
					} else if (playa.getNextAction().contains("checks")) {
						players.put(playa, false);
						this.sendToAll("setPlayerAction", playa.seatPosition, "checks");
						actionOnIndex++;
					} else if (playa.getNextAction().contains("bets")) {
						for (SeatedPlayer player : playersInHand) {
							if (player.getStackSize() > 0) {
								players.put(player, true);
							}
						}
						players.put(playa, false);
						this.sendToAll("putInChips", playa.seatPosition, Integer.parseInt(playa.getNextAction().split(";")[1]));
						this.sendToAll("setPlayerAction", playa.seatPosition, playa.getNextAction().replace(";", " "));
						this.sendToAll("setPlayerStackSize", playa.seatPosition, playa.getStackSize());
						previousBet = currentBet;
						currentBet += Integer.parseInt(playa.getNextAction().split(";")[1]);
						actionOnIndex++;
					}
					this.addToPot(playa.doAction(playa.getNextAction(), playersInHand));
					this.sendToAll("setPlayerStackSize", playa.seatPosition, playa.getStackSize());
				}
				waitingOnAction = false;
				for (Boolean bool : players.values()) {
					if (bool) {
						waitingOnAction = true;
						//break;
					}
				}
				if (!waitingOnAction) {
					for (SeatedPlayer player : playersInHand) {
						player.betInFront = 0;
					}
				}
			} else {
				int playables = 0;
				for (SeatedPlayer player : playersInHand) {
					if (player.getStackSize() > 0 && players.get(player)) {
						playables++;
					}
				}
				waitingOnAction = playables > 1;
				actionOnIndex++;
			}
		}

		for (SeatedPlayer player : playersInHand) {
			player.betInFront = 0;
		}
		return cont;
	}

	/**
	 * Creates a new {@code Deck} object, shuffles it, and saves this into the {@code deck} member variable
	 * <pre><ul>{@code public Deck createDeck()}</ul></pre>
	 * @return	{@code Deck} A new shuffled Deck object
	 */
	public Deck createDeck() {
		this.deck = new Deck();
		this.deck.shuffle();
		return this.deck;
	}

	/**
	 * 
	 * @param player		
	 * @param newStackSize	
	 * @return
	 */
	public void setStack(SeatedPlayer player, int newStackSize) {
		// this.playersAndStacks.replace(player, newStackSize);
		player.setStackSize(newStackSize);
	}

	/**
	 * Given an {@code ArrayList<SeatedPlayer>}, gets all winning players as a {@code Set<SeatedPlayer>}
	 * <pre><ul>{@code public Set<SeatedPlayer> findBestHands(ArrayList<SeatedPlayer> players)}</ul></pre>
	 * @pre		{@code board} member variable (a {@code Card[]}) has a valid {@code card} in each of its 5 spaces
	 * @param	players	An {@code ArrayList<SeatedPlayer>} containing the {@code SeatedPlayer}s at this table
	 * @return	{@code Set<SeatedPlayer>} containing all winning players for this hand
	 */
	public Set<SeatedPlayer> getBestHands(ArrayList<SeatedPlayer> players) {
		if (players.size() == 1) {
			HashSet<SeatedPlayer> returnMe = new HashSet<SeatedPlayer>();
			returnMe.add(players.get(0));
			return returnMe;
		} else {
			int compare;
			Set<SeatedPlayer> winners = new HashSet<SeatedPlayer>();
			ArrayList<Set<Card>> bestHands = new ArrayList<Set<Card>>();
			for (SeatedPlayer player : players) {
				if (winners.isEmpty()) winners.add(player);
				for (Set<Card> fiveCardHand : getHandCombinations(player.getHoleCards())) {
					if (bestHands.isEmpty()) {
						bestHands.add(fiveCardHand);
					} else {
						compare = compareHandStrengths(fiveCardHand, bestHands.get(0));
						if (compare == 1) {
							bestHands.clear();
							winners.clear();
							bestHands.add(fiveCardHand);
							winners.add(player);
							if (debugMode) System.out.println("New best hand " + player.getHoleCards().toString().replace("=", " "));
						} else if (compare == 0) {
							bestHands.add(fiveCardHand);
							winners.add(player);
						}
					}
				}
			}
			return winners;
		}
	}

	public int compareHandStrengths(Set<Card> hand1, Set<Card> hand2) {
		int hand1Strength = getHandCategory(hand1);
		int hand2Strength = getHandCategory(hand2);
		if (hand1Strength > hand2Strength) {
			if (debugMode) System.out.println(sortHandWithStrength(hand1, hand1Strength).toString() + " beats " + sortHandWithStrength(hand2, hand2Strength).toString());
			return 1;
		} else if (hand1Strength < hand2Strength) {
			if (debugMode) System.out.println(sortHandWithStrength(hand2, hand2Strength).toString() + " beats " + sortHandWithStrength(hand1, hand1Strength).toString());
			return -1;
		} else {
			return compareKickers(sortHandWithStrength(hand1, hand1Strength), sortHandWithStrength(hand2, hand2Strength)); // EVERYONE LOVES A CHOP POT (and a salad)
		}
	}

	private static int compareKickers(ArrayList<Card> hand1, ArrayList<Card> hand2) {
		for (int i = 0; i < 5; i++) {
			int card1Rank = getCardRank(hand1.get(i));
			int card2Rank = getCardRank(hand2.get(i));
			if (card1Rank > card2Rank) {
				if (debugMode) System.out.println(sortHandWithStrength(new HashSet<Card>(hand1), getHandCategory(new HashSet<Card>(hand1))).toString() + " kicker beats " + sortHandWithStrength(new HashSet<Card>(hand2), getHandCategory(new HashSet<Card>(hand2))).toString());
				return 1;
			} else if (card1Rank < card2Rank) {
				if (debugMode) System.out.println(sortHandWithStrength(new HashSet<Card>(hand2), getHandCategory(new HashSet<Card>(hand2))).toString() + " kicker beats " + sortHandWithStrength(new HashSet<Card>(hand1), getHandCategory(new HashSet<Card>(hand1))).toString());
				return -1;
			}
		}
		return 0;
	}

	private static ArrayList<Card> sortHandWithStrength(Set<Card> hand, int handStrength) {
		ArrayList<Card> orderedHand = sortHand(hand);
		switch(handStrength) {
		case 9: // straight flush
		case 6: // flush
		case 5: // straight
		case 1: // high card
			return orderedHand;
		case 8: // quads
			if (orderedHand.get(0).getRank().value() != orderedHand.get(1).getRank().value()) {
				orderedHand.add(orderedHand.remove(0));
				return orderedHand;
			} else return orderedHand;
		case 7: // full house
			if (orderedHand.get(1).getRank().value() != orderedHand.get(2).getRank().value()) {
				orderedHand.add(orderedHand.remove(0));
				orderedHand.add(orderedHand.remove(0));
				return orderedHand;
			} else return orderedHand;
		case 4: // trips
			for (int sequenceStart = 0; sequenceStart < orderedHand.size()-1; sequenceStart++) {
				if (orderedHand.get(sequenceStart).getRank().value() == orderedHand.get(sequenceStart+1).getRank().value()) {
					Card tripsCardOne = orderedHand.remove(sequenceStart);
					Card tripsCardTwo = orderedHand.remove(sequenceStart);
					Card tripsCardThree = orderedHand.remove(sequenceStart);
					ArrayList<Card> twoSorted = new ArrayList<Card>();
					twoSorted.add(tripsCardOne);
					twoSorted.add(tripsCardTwo);
					twoSorted.add(tripsCardThree);
					twoSorted.addAll(sortHand(new HashSet<Card>(orderedHand)));
					return twoSorted;
				}
			}
			System.err.println("case: 4");
		case 2: // pair
			for (int sequenceStart = 0; sequenceStart < orderedHand.size()-1; sequenceStart++) {
				if (orderedHand.get(sequenceStart).getRank().value() == orderedHand.get(sequenceStart+1).getRank().value()) {
					Card pairCardOne = orderedHand.remove(sequenceStart);
					Card pairCardTwo = orderedHand.remove(sequenceStart);
					ArrayList<Card> threeSorted = new ArrayList<Card>();
					threeSorted.add(pairCardOne);
					threeSorted.add(pairCardTwo);
					threeSorted.addAll(sortHand(new HashSet<Card>(orderedHand)));
					return threeSorted;
				}
			}
			System.err.println("case: 2");
		case 3: // two pair
			@SuppressWarnings("unused")
			int kickerIndex = 0;
			if (orderedHand.get(0).getRank().value() != orderedHand.get(1).getRank().value()) {
				orderedHand.add(orderedHand.remove(0));
			} else if (orderedHand.get(2).getRank().value() != orderedHand.get(3).getRank().value()) {
				orderedHand.add(orderedHand.remove(2));
			}
			return orderedHand;
		}
		System.err.println("uh oh");
		return new ArrayList<Card>();
	}

	private static int getHandCategory(Set<Card> fiveCards) {
		if (isStraightFlush(fiveCards)) {
			return 9;
		} else if (isQuads(fiveCards)) {
			return 8;
		} else if (isFullHouse(fiveCards)) {
			return 7;
		} else if (isFlush(fiveCards)) {
			return 6;
		} else if (isStraight(fiveCards)) {
			return 5;
		} else if (isTrips(fiveCards)) {
			return 4;
		} else if (isTwoPair(fiveCards)) {
			return 3;
		} else if (isPair(fiveCards)) {
			return 2;
		} else if (isHighCard(fiveCards)) {
			return 1;
		} else {
			return 0;
		}
	}

	private static boolean isStraightFlush(Set<Card> fiveCards) {	// STRAIGHT FLUSH
		return isStraight(fiveCards) && isFlush(fiveCards);
	}
	private static boolean isQuads(Set<Card> fiveCards) {			// FOUR OF A KIND
		int[] rankChangesAndSpot = getRankChangesAndSpot(sortHand(fiveCards));
		// Four of a kind: A-A-A-A-B or A-B-B-B-B
		return (rankChangesAndSpot[0] == 1 &&
				(rankChangesAndSpot[1] == 0 || rankChangesAndSpot[1] == 3));
	}
	private static boolean isFullHouse(Set<Card> fiveCards) {		// FULL HOUSE
		int[] rankChangesAndSpot = getRankChangesAndSpot(sortHand(fiveCards));
		// Full house: A-A-A-B-B or A-A-B-B-B
		return (rankChangesAndSpot[0] == 1) &&
				(rankChangesAndSpot[1] == 1 || rankChangesAndSpot[1] == 2);
	}
	private static boolean isFlush(Set<Card> fiveCards) {			// FLUSH
		ArrayList<Card> sortedHand = sortHand(fiveCards);
		int suit = sortedHand.get(0).getSuit().value();
		return (suit == sortedHand.get(1).getSuit().value()) &&
				(suit == sortedHand.get(2).getSuit().value()) &&
				(suit == sortedHand.get(3).getSuit().value()) &&
				(suit == sortedHand.get(4).getSuit().value());
	}
	private static boolean isStraight(Set<Card> fiveCards) {		// STRAIGHT
		ArrayList<Card> sortedHand = sortHand(fiveCards);
		if (getCardRank(sortedHand.get(0)) == 14) { // ace case
			// either 5432A or KQJTA
			return ((getCardRank(sortedHand.get(1)) == 5 &&
					getCardRank(sortedHand.get(2)) == 4 &&
					getCardRank(sortedHand.get(3)) == 3 &&
					getCardRank(sortedHand.get(4)) == 2) ||
					(getCardRank(sortedHand.get(1)) == 13 &&
					getCardRank(sortedHand.get(2)) == 12 &&
					getCardRank(sortedHand.get(3)) == 11 &&
					getCardRank(sortedHand.get(4)) == 10
							));
		} else { // everything else
			int counter = getCardRank(sortedHand.get(0));
			for (Card card : sortedHand) {
				if (getCardRank(card) != ((--counter) + 1)) {
					return false;
				}
			}
		}
		return true;
	}
	private static boolean isTrips(Set<Card> fiveCards) {			// THREE OF A KIND
		// note that we do not use getRankChangesAndSpot() here
		// because we can't differentiate from trips and two pair
		// when given rankChanges = 2 and rankChangeSpot = 3
		ArrayList<Card> sortedHand = sortHand(fiveCards);
		for (int i = 0; i < 2; i++) {
			if ((getCardRank(sortedHand.get(i)) == getCardRank(sortedHand.get(i+1))) &&
					(getCardRank(sortedHand.get(i)) == getCardRank(sortedHand.get(i+2)))) {
				return true;
			}
		}
		return false;
	}
	private static boolean isTwoPair(Set<Card> fiveCards) {		// TWO-PAIR
		int[] rankChangesAndSpot = getRankChangesAndSpot(sortHand(fiveCards));
		// Two-pair: A-A-B-B-C or A-A-B-C-C or A-B-B-C-C
		return (rankChangesAndSpot[0] == 2) &&
				(rankChangesAndSpot[1] == 2 || rankChangesAndSpot[1] == 3);
	}
	private static boolean isPair(Set<Card> fiveCards) {			// ONE-PAIR
		int[] rankChangesAndSpot = getRankChangesAndSpot(sortHand(fiveCards));
		// One-pair: A-A-B-C-D or A-B-B-C-D or A-B-C-C-D or A-B-C-D-D
		return (rankChangesAndSpot[0] == 3);
	}
	private static boolean isHighCard(Set<Card> fiveCards) { 		// HIGH CARD
		return !(isPair(fiveCards) || isTwoPair(fiveCards) || isTrips(fiveCards) ||
				isStraight(fiveCards) || isFlush(fiveCards) || isFullHouse(fiveCards) ||
				isQuads(fiveCards) || isStraightFlush(fiveCards));
	}

	private static int getCardRank(Card c) {
		return c.getRank().value() != 1 ? c.getRank().value() : 14;
	}

	private static int[] getRankChangesAndSpot(ArrayList<Card> sortedHand) {
		int[] changes = new int[2];
		changes[0] = 0; // rank change
		changes[1] = 0; // rank change start location
		for (int i = 0; i < 4; i++) {
			if (getCardRank(sortedHand.get(i)) != getCardRank(sortedHand.get(i+1))) {
				changes[0]++;
				changes[1] = i;
			}
		}
		return changes;
	}
	
	private static ArrayList<Card> sortHand(Set<Card> fiveCards) {
		ArrayList<Card> sorted = new ArrayList<Card>();
		for (Card card : fiveCards) {
			sorted.add(card);
		}
		Collections.sort(sorted, new Comparator<Card>() {
			public int compare(Card card1, Card card2) {
				int card1Rank = getCardRank(card1);
				int card2Rank = getCardRank(card2);
				if (card1Rank == card2Rank) {
					return 0;
				} else if (card1Rank > card2Rank) {
					return -1; // we do this because we want biggest to smallest
				} else /*if (card1Rank < card2Rank)*/ {
					return 1;
				}
			}
		});
		return sorted;
	}

	/**
	 * Given two {@code Card} objects in a {@code Pair}, returns an {@code ArrayList<Set<Card>>} containing all (7 choose 5) hand combinations
	 * <pre><ul>{@code private ArrayList<Set<Card>> getHandCombinations(Pair<Card, Card> holeCards)}</ul></pre>
	 * @param	holeCards	The hole cards to be used with the board to get seven cards to choose from
	 * @return	{@code ArrayList<Set<Card>>} containing all 21 combinations, or {@code null} if board member variable is not filled with Card objects
	 */
	private ArrayList<Set<Card>> getHandCombinations(Pair<Card, Card> holeCards) {
		if (this.board.length != 5) return null;
		for (Card boardCard : this.board) {
			if (boardCard == null) return null;
		}
		ArrayList<Card> sevenCards = new ArrayList<Card>();
		for (Card card : this.board) {
			sevenCards.add(card);
		}
		sevenCards.add(holeCards.getKey());
		sevenCards.add(holeCards.getValue());
		ArrayList<Card> sevenCardsCopy = new ArrayList<Card>();
		for (Card card : sevenCards) {
			sevenCardsCopy.add(card);
		}
		// sevenCards now contains the seven cards from which five are chosen to designate our "best hand"
		// sevenCardsCopy is a copy of sevenCards -- it's an independent object

		ArrayList<Set<Card>> handCombinations = new ArrayList<Set<Card>>(); // 7 choose 5 = 21
		Set<Card> fiveCardHand;

		for (int i = 0; i < 7; i++) {
			for (int j = i; j < 6; j++) {
				sevenCardsCopy.remove(i);
				sevenCardsCopy.remove(j);
				fiveCardHand = new HashSet<Card>();
				for (Card card : sevenCardsCopy) {
					fiveCardHand.add(card);
				}
				handCombinations.add(fiveCardHand);
				sevenCardsCopy.clear();
				for (Card card : sevenCards) {
					sevenCardsCopy.add(card);
				}
			}
		}
		return handCombinations;
	}
	public HashMap<Integer, SeatedPlayer> getPlayers() {
		return this.players;
	}
}