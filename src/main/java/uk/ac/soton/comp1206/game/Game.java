package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.BlockMoveListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.GameTimerListener;
import uk.ac.soton.comp1206.event.PieceChangeListener;
import uk.ac.soton.comp1206.event.ShowModelListener;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);
    
    /**
     * Listeners to the current block we are on
     */
    protected ArrayList<BlockMoveListener> currentBlockListeners = new ArrayList<BlockMoveListener>();
    
    /**
     * Listeners to side pieces being changed
     */
    protected ArrayList<PieceChangeListener> pieceListeners = new ArrayList<PieceChangeListener>();
    
    /**
     * Listeners to the timer to update the UI timer
     */
    protected ArrayList<GameTimerListener> timerListeners = new ArrayList<GameTimerListener>();
    
    /**
     * Listeners to the piece model to update it in the UI
     */
    protected ArrayList<ShowModelListener> modelListeners = new ArrayList<ShowModelListener>();
    
    /**
     * Listeners to when the game is over
     */
    protected ArrayList<GameOverListener> gameOverListeners = new ArrayList<GameOverListener>();
    
    /**
     * The current piece
     */
    public GamePiece currentPiece;
    
    /**
     * The next piece
     */
    public GamePiece nextPiece;

    /**
     * The timer to play a move
     */
    protected Timer timer;
    
    /**
     * The task executed after the time is off
     */
    protected TimerTask task;
    
    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;
    
    /**
     * The current X position of the block we are on
     */
    private int xCurrent;
    
    /**
     * The current Y position of the block we are on
     */
    private int yCurrent;
    
    /**
     * The level
     */
    protected SimpleIntegerProperty level = new SimpleIntegerProperty();
    
    /**
     * The amount of lives
     */
    protected SimpleIntegerProperty lives = new SimpleIntegerProperty();
    
    /**
     * The score
     */
    protected SimpleIntegerProperty score = new SimpleIntegerProperty();
    
    /**
     * Temporary score to calculate levels
     */
    protected int tempScore;
    
    /**
     * The multiplier to calculate score
     */
    protected int multiplier;
    
    /**
     * The gamebot
     */
    private GameBot gameBot;
    
    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");

        //Set starting values
        score.set(0);
        level.set(1);
        lives.set(3);
        
        tempScore = 0;
        multiplier = 1;
        
        xCurrent = 0;
        yCurrent = 0;
        
        //Get pieces
        currentPiece = spawnPiece();
        nextPiece = spawnPiece();
                
        //Start timer
        timer = new Timer();
        task = new TimerTask() {

			public void run() {
				gameLoop();
			}
        };
        setTimer();
    }
    
    /**
     * Sets the timer for the next piece
     */
    public void setTimer() {
    	timer.cancel();
    	timer = new Timer();
    	task = new TimerTask() {

			public void run() {
				gameLoop();
			}
    		
    	};
    	timer.schedule(task, getTimePeriod());
    	
    	for (var timerListener : timerListeners) {
    		timerListener.updateTimer(getTimePeriod());
    	}
    }
    
    /**
     * Get the time left to play a piece
     * @return The time left
     */
    public long getTimePeriod() { return 12000 - 500*level.get() > 2500 ? 12000 - 500*level.get() : 2500; }
    
    /**
     * Handles what happens when the time is off
     */
    public void gameLoop() {
    	grid.clearData();

    	//Lose a life
    	lives.set(lives.get()-1);
    	Multimedia.playAudio("lifelose.wav");
    	
    	//Game over if no lives
    	if (lives.get() < 0) {
    		timer.cancel();
    		
    		for (var listener : gameOverListeners) {
    			listener.gameOver();
    		}
    	} else afterPiece();
    }
    
    /**
     * Handles the input on the scene 
     * @param key The key pressed
     */
    public void handleInput(KeyEvent key) {
    	switch(key.getCode()) {
			case D: xCurrent++; calculateCurrent(); break;
 			case W: yCurrent--; calculateCurrent(); break;
	   		case A: xCurrent--; calculateCurrent(); break;
	   		case S: yCurrent++; calculateCurrent(); break;
	   		case X: if (grid.canPlayPiece(currentPiece, xCurrent, yCurrent)) {
	   					grid.playPiece(currentPiece, xCurrent, yCurrent); 
	   					afterPiece();
	   				} else { Multimedia.playAudio("fail.wav"); }
	   				break;
	   		case R: currentPiece.rotate();
	   				Multimedia.playAudio("rotate.wav");
	   				for (var listener : pieceListeners) {
	   					listener.pieceChange(currentPiece, 0);
	   				}; break;
	   		case T: swapPiece(); break;
	   		case P: for (var listener: modelListeners) listener.showModel(); break;
	   		case L: playBot(); break;
	   		case M: for (var listener : gameOverListeners) listener.gameOver(); break;
		}

    }
    
    /**
     * Get the bot to play the next piece for you (loser)
     */
    private void playBot() {
    	if (gameBot == null) gameBot = new GameBot(this);
    	if (gameBot.think()) Multimedia.playAudio("place.wav");
    	else Multimedia.playAudio("fail.wav");
    }
    
    /**
     * Calculates where the current block is
     */
    private void calculateCurrent() {
    	if (xCurrent >= grid.getRows()) xCurrent = 0;
    	else if (xCurrent < 0) xCurrent = grid.getRows() - 1;
    	else if (yCurrent >= grid.getCols()) yCurrent = 0;
    	else if (yCurrent < 0) yCurrent = grid.getCols() - 1;
    	
    	for (var listener : currentBlockListeners) {
    		listener.blockMoved(xCurrent, yCurrent);
    	}
    }
    
    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        if (grid.canPlayPiece(currentPiece, x, y)) {grid.playPiece(currentPiece, x, y); afterPiece();}
    }

    /**
     * Spawn the next piece
     * @return The next piece that is spawned
     */
    public GamePiece spawnPiece() {
    	return GamePiece.createPiece(new Random().nextInt(15));
    }
    
    /**
     * Swap current and next piece
     */
    public void swapPiece() {
    	GamePiece temp = currentPiece;
    	currentPiece = nextPiece;
    	nextPiece = temp;
    	
    	Multimedia.playAudio("rotate.wav");
    	updatePieceListeners();
    }
    
    /**
     * Handle what happens after the piece is places
     */
    public void afterPiece() {
    	currentPiece = nextPiece;
    	nextPiece = spawnPiece();
    	
    	//Get number of lines cleared, calculate number of blocks and get the score out of it
    	int rowsCleared = grid.getRowsRemoved().size();
    	int colsCleared = grid.getColsRemoved().size();
    	int blocksCleared = rowsCleared*cols + colsCleared*rows - rowsCleared*colsCleared;
    	int newScore = (rowsCleared + colsCleared) * blocksCleared * 10 * multiplier;
    	
    	score.set(score.get() + newScore);
    	tempScore += newScore;
    	
    	multiplier = rowsCleared + colsCleared > 0 ? multiplier+1: 1;
    	
    	//Update level
    	while (tempScore >= 1000) {
    		tempScore -= 1000;
    		level.set(level.get()+1);
    	}
    	    	
    	Multimedia.playAudio("place.wav");
    	updatePieceListeners();
    	setTimer();
    }
    
    /**
     * Update side piece listeners. Code: 0 for current piece, 1 for next piece
     */
    protected void updatePieceListeners() {
    	for (var listener : pieceListeners) {
    		listener.pieceChange(currentPiece, 0);
    		listener.pieceChange(nextPiece, 1);
    	}
    }
    
    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }
    
    public SimpleIntegerProperty getScore() { return score; }
    
    public SimpleIntegerProperty getLives() { return lives; }
    
    public SimpleIntegerProperty getLevel() { return level; }

    public void addBlockMoveListener(BlockMoveListener listener) { this.currentBlockListeners.add(listener); }
    
    public void addPieceChangeListener(PieceChangeListener listener) { this.pieceListeners.add(listener); }

    public void addTimerListener(GameTimerListener listener) { this.timerListeners.add(listener); }
    
    public void addModelListener(ShowModelListener listener) { this.modelListeners.add(listener); }
    
    public void addGameOverListener(GameOverListener listener) { this.gameOverListeners.add(listener); }
    
    /**
     * Cancel the timer and clear all of the listeners
     */
    public void cleanUp() {
    	timer.cancel();
    	
    	currentBlockListeners.clear();
    	pieceListeners.clear();
    	timerListeners.clear();
    	modelListeners.clear();
    	gameOverListeners.clear();
    }
    
}
