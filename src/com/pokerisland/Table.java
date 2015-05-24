package com.pokerisland;

import java.util.HashMap;

public class Table extends Thread {

	private Gameplay game;
	int tableID;
	int blindLevel;
	int capacity;

	public Table(int tableID, int blindLevel, int capacity) {
		this.tableID = tableID; 
		this.blindLevel = blindLevel;
		this.capacity = capacity;
		this.game = new Gameplay(blindLevel, capacity, tableID);
	}

	public void run() {
		while (true) {
			if (this.game.players.size() >= 2) {
				this.game.nextHand();
				if (this.game.players.size() >= 2) {
					this.game.playHand();
				}
            } else if (this.game.playersToAdd.size() > 0 || this.game.playersToRemove.size() > 0) {
				this.game.nextHand();
			} else {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Returns a random available seat at the table.
	 * <pre><ul>{@code public int getAvailableSeat()}</pre></ul>
	 * @return A random available seat number from 0-8
	 */
	public int getAvailableSeat() {
		int seat = 0;
		while (this.game.players.keySet().contains(seat)) {
			seat = (int) (Math.random()*this.capacity);
		}
		return seat;
	}

	/**
	 * Tries to add a player to the table. 
	 * <pre><ul>{@code public void addPlayer(SeatedPlayer player)}</ul></pre>
	 * @param player The {@code SeatedPlayer} to be added to the game
	 */
	public void addPlayer(SeatedPlayer player) {
		boolean alreadyJoined = false;
		for (SeatedPlayer tablePlayers: this.game.players.values()) {
			if (tablePlayers.getUsername().equals(player.getUsername())) {
				alreadyJoined = true;
			}
		}
		if (this.game.capacity == this.game.players.size()) {
			if (Gameplay.debugMode) {
				System.err.println("ILLEGAL, TableGUI allowed someone to join a table without any available seats");
				// TODO come back to this and see if this message is necessary
				System.err.println("this.capacity = " + this.capacity);
				System.err.println("this.players.size() = " + this.game.players.size());
			}
		} else if (alreadyJoined) {
			if (Gameplay.debugMode) {
				System.err.println("ILLEGAL, same playing joining table multiple times");
			}
		} else if (this.game.players.get(player.seatPosition) != null) {
			// TODO send message that the seat was taken while you were trying to buy-in
		} else {
			while (this.game.isSettingUpNextHand) {
				
			}
			this.game.playersToAdd.add(player);
		}
	}

	/**
	 * Queues a {@code SeatedPlayer} to be removed from the table.
	 * <pre><ul>{@code public void removePlayer(int seatPosition)}</ul></pre>
	 * @param seatPosition The seat position of the {@code SeatedPlayer} to be removed
	 */
	public void removePlayer(int seatPosition) {
		while (this.game.isSettingUpNextHand) {
			
		}
		this.game.playersToRemove.add(seatPosition);
	}

	public int getTableID() {
		return this.tableID; 
	}

	/**
	 * Sends a message to the {@code Gameplay} object of this {@code Table}
	 * @param isGameplay {@code true} to invoke {@code Gameplay} methods; {@code false} to add or remove player
	 * @param message The String representing to action to perform
	 * @param np The {@code NetworkedPlayer} to invoke the message on
	 */
	public void message(boolean isGameplay, String message, NetworkedPlayer np) {
		if (isGameplay) {
			for (int key : this.game.players.keySet()) {
				if (this.game.players.get(key).getUsername().equals(np.getUsername())) {
					this.game.players.get(key).addAction(message.split(";")[2]+";"+message.split(";")[3]);
					return;
				}
			}
			if (Gameplay.debugMode) System.out.println("Unable to parse the message OR player left table: " + np.getUsername() + " " + message);
		} else {
			if (message.split(";")[2].equals("leaves")) {
				for (SeatedPlayer sp : this.game.players.values()) {
					if (sp.getUsername().equals(np.getUsername())) {
						this.removePlayer(sp.seatPosition);
						return;
					}
				}
				if (Gameplay.debugMode) System.out.println("Unable to find player " + np + " at the table to remove them.");
			} else if (message.split(";")[2].equals("joins")) {
				this.addPlayer(new SeatedPlayer(np, Integer.parseInt(message.split(";")[3]), this.getAvailableSeat()));
			} else {
				if (Gameplay.debugMode) System.err.println("Unable to parse the message: " + message);
			}
		}
	}

	public int getCapacity() {
		return this.capacity;
	}

	public int getBlindLevel() {
		return this.blindLevel;
	}

	public int getNumOfPlayers() {
		try {
			return this.game.getPlayers().size();
		} catch (Exception e) {
			System.err.println("No game instantiated, getNumPlayers called");
			return this.getCapacity()-1;
		}
	}
	
	public void removeNetworkedPlayer(NetworkedPlayer np)
	{
		HashMap<Integer, SeatedPlayer> seatedPlayerArray = game.getPlayers();
		for(Integer i : seatedPlayerArray.keySet())
		{
			SeatedPlayer sp = seatedPlayerArray.get(i);
			if(np.equals(sp.getNetworkedPlayer()))
			{
				//Our NetworkedPlayer is their NetworkedPlayer 
				message(true, "game;"+tableID+";folds;gucci", np);
				message(false, "table;"+tableID+";leaves;gucci", np);
				
			}
		}
	}

}
