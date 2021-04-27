package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import uk.ac.soton.comp1206.event.MessageListener;
import uk.ac.soton.comp1206.event.PlayersScoreListener;
import uk.ac.soton.comp1206.network.Communicator;

/**
 * This class holds the logic for the multiplayer game
 *
 */
public class MultiplayerGame extends Game {

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    
    /**
     * The communicator
     */
    private Communicator communicator;

    /**
     * The score listeners
     */
    public ArrayList<PlayersScoreListener> scoreListeners = new ArrayList<PlayersScoreListener>();
    
    /**
     * The message listeners
     */
    public ArrayList<MessageListener> messageListeners = new ArrayList<MessageListener>();
    
    /**
     * Constructor for the game, initialise the communicator and the grid
     * @param cols
     * @param rows
     * @param communicator
     */
	public MultiplayerGame(int cols, int rows, Communicator communicator) {
		super(cols, rows);
		this.communicator = communicator;
	}
	
	/**
	 * Starts the game
	 */
	@Override
	public void start() {
        logger.info("Starting game");
        
        //Initialise the game as a single player game, then add the multiplayer components
        initialiseGame();
        initialiseMultiplayer();
        
        //Reset pieces from single player
        currentPiece = null;
        nextPiece = null;
        
        //Get players and their scores
        communicator.send("SCORES");
        
        //Get pieces from multiplayer
        communicator.send("PIECE");
        communicator.send("PIECE");
    }
	
	/**
	 * Initialise the multiplayer components for the game
	 */
	private void initialiseMultiplayer() {
		
		//Add listener to the communicator
		communicator.addListener((message) -> {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					if (message.startsWith("SCORES")) {
						for (var listener : scoreListeners) {
							listener.playerScore(message.replace("SCORES ", ""));
						}
					} else if (message.startsWith("PIECE")) {
						afterPieceTwo(Integer.parseInt(message.replace("PIECE ", "")));
					} else if (message.startsWith("BOARD")) {
						
					} else if (message.startsWith("MSG")) {
						for (var listener : messageListeners) {
							listener.receiveMessage(message.replace("MSG ", ""));
						}
					}
				}
				
			});
		});
	}
	
	/**
	 * What happens when the timer is out
	 */
	@Override
	public void gameLoop() {
    	grid.clearData();

    	lives.set(lives.get()-1);
    	if (lives.get() < 0) {
    		timer.cancel();
    		
    		communicator.send("DIE");
    		for (var listener: gameOverListeners) {
    			listener.gameOver();
    		}
    	} else { 
    		afterPiece();
    		communicator.send("LIVES " + lives.get());
    		communicator.send("SCORES");
    	}
    }
	
	//What happens when a piece is played
	@Override
	 public void afterPiece() {
		StringBuilder boardToSend = new StringBuilder();
		
		for (var x = 0; x < cols; x++) {
			for (var y = 0; y < rows; y++) {
				boardToSend.append(grid.get(x, y));
				boardToSend.append(" ");
			}
		}
		
		communicator.send("BOARD " + boardToSend.toString());
		communicator.send("PIECE");
    }
	
	/**
	 * What happens when a piece is played for multiplayer
	 * @param pieceValue The value of the next piece
	 */
	private void afterPieceTwo(int pieceValue) {
		
		//Get next piece
    	currentPiece = nextPiece;
    	nextPiece = GamePiece.createPiece(pieceValue);
    	
    	//Calculate score if both pieces have been initialised
		if (currentPiece != null) {
	    	int rowsCleared = grid.getRowsRemoved().size();
	    	int colsCleared = grid.getColsRemoved().size();
	    	int blocksCleared = rowsCleared*cols + colsCleared*rows - rowsCleared*colsCleared;
	    	int newScore = (rowsCleared + colsCleared) * blocksCleared * 10 * multiplier;
	    	
	    	score.set(score.get() + newScore);
	    	tempScore += newScore;
	    	
	    	multiplier = rowsCleared + colsCleared > 0 ? multiplier+1: 1;
	    	
	    	if(tempScore >= 1000) {
	    		tempScore = 0;
	    		level.set(level.get()+1);
	    	}
	    	    	
			communicator.send("SCORE " + score.get());
			communicator.send("SCORES");
			
	    	updatePieceListeners();
	    	setTimer();
		}
	}

	public void addScoreListener(PlayersScoreListener listener) { scoreListeners.add(listener); }
	
	public void addMessageListener(MessageListener listener) { messageListeners.add(listener); }
	
	/**
	 * Clear all the listeners and stop the timer
	 */
	@Override
	public void cleanUp() {
    	timer.cancel();
    	
    	currentBlockListeners.clear();
    	pieceListeners.clear();
    	timerListeners.clear();
    	modelListeners.clear();
    	gameOverListeners.clear();
    	scoreListeners.clear();
    	messageListeners.clear();
    }
}
