package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The GameBot calculates the most valuable piece to be played next given the two upcoming pieces.
 * 
 * It checks for number of lines cleared and number of single unfilled blocks left for each possible play.
 * Based on that, it calculates a fitness value, and the highest one represents the best move.
 * 
 * Data is storred in parallel ArrayLists - that is, a value is added in each list such that the indeces
 * represent a given gamepiece, its rotation and coordinates.
 */
public class GameBot {

    private static final Logger logger = LogManager.getLogger(GameBot.class);

    /**
     * The first game piece
     */
	private GamePiece pieceOne;
	
	/**
	 * The second game piece
	 */
	private GamePiece pieceTwo;
	
	/**
	 * The game logic on which the bot operates
	 */
	private Game game;
	
	/**
	 * The grid of the game
	 */
	private Grid grid;
	
	/**
	 * The number of rotations for the first piece
	 */
	private ArrayList<Integer> rotationsOne;
	
	/**
	 * The number of rotations for the second piece
	 */
	private ArrayList<Integer> rotationsTwo;
	
	/**
	 * The X coordinates for the first piece
	 */
	private ArrayList<Integer> gridXOne;
	
	/**
	 * The X coordinates for the second piece
	 */
	private ArrayList<Integer> gridXTwo;	
	
	/**
	 * The Y coordinates for the first piece
	 */
	private ArrayList<Integer> gridYOne;
	
	/**
	 * The Y coordinates for the second piece
	 */
	private ArrayList<Integer> gridYTwo;
	
	/**
	 * The fitness values for the first piece
	 */
	private ArrayList<Double> fitnessOne;
	
	/**
	 * The fitness values for the second piece
	 */
	private ArrayList<Double> fitnessTwo;
	
	private int[][] gridBlocks;
	/**
	 * Constructor of our bot, takes the game logic as parameter.
	 * @param game the game
	 */
	public GameBot(Game game) {
		logger.info("Starting GameBot");
		
		this.game = game;
		this.grid = game.getGrid();
		
		//Innitialise the lists
		rotationsOne = new ArrayList<Integer>();
		rotationsTwo = new ArrayList<Integer>();
		gridXOne = new ArrayList<Integer>();
		gridXTwo = new ArrayList<Integer>();
		gridYOne = new ArrayList<Integer>();
		gridYTwo = new ArrayList<Integer>();
		fitnessOne = new ArrayList<Double>();
		fitnessTwo = new ArrayList<Double>();
		
		gridBlocks = new int[grid.getRows()][grid.getCols()];
	}
	
	/**
	 * Handles each piece's coordinates and rotations, adds data to lists, 
	 * calls fitness methods, makes the best move
	 */
	public boolean think() {
		//Innitialise the two upcoming pieces
		pieceOne = game.currentPiece;
		pieceTwo = game.nextPiece;
		
		int pieceSizeOne = 0;
		int pieceSizeTwo = 0;
		
		//Get number of blocks in a piece, important for fitness function
		for (var i = 0; i < 3; i++) {
			for (var j = 0; j < 3; j++) {
				if (pieceOne.getBlocks()[i][j] != 0) pieceSizeOne++;
				if (pieceTwo.getBlocks()[i][j] != 0) pieceSizeTwo++;
			}
		}
		
		logger.info("Bot started thinking");
		
		//Handle each piece by rotating and moving it along the grid and calculating its fitness value
		for (var x = 0; x < grid.getRows(); x++) {
			for (var y = 0; y < grid.getCols(); y++) {
				for (var rot = 1; rot <= 4; rot++) {
					pieceOne.rotate();
					pieceTwo.rotate();
					
					if (grid.canPlayPiece(pieceOne, x, y)) {
						rotationsOne.add(rot);
						gridXOne.add(x);
						gridYOne.add(y);
						fitnessOne.add(getFitness(pieceOne, pieceSizeOne, x, y));
					}
					
					if (grid.canPlayPiece(pieceTwo, x, y)) {
						rotationsTwo.add(rot);
						gridXTwo.add(x);
						gridYTwo.add(y);
						fitnessTwo.add(getFitness(pieceTwo, pieceSizeTwo, x, y));
					}
				}
			}
		}
		
		double prev = 0;
		int indexOne = -1;
		int indexTwo = -1;
		
		//Get the index of the highest fitness value for the first piece
		for (var i = 0; i < fitnessOne.size(); i++) {
			if (fitnessOne.get(i) >= prev) {
				prev = fitnessOne.get(i); indexOne = i; 
			}
		}
		
		prev = 0;
		
		//Get the index of the highest fitness value for the second piece
		for (var i = 0; i < fitnessTwo.size(); i++) {
			if (fitnessTwo.get(i) >= prev) {
				prev = fitnessTwo.get(i); indexTwo = i;
			}
		}
		
		//Play the best move and get the next piece
		if (indexOne == -1 && indexTwo == -1)  { logger.info("No piece to place"); return false;}
		else if (indexOne == -1) {
			pieceTwo.rotate(rotationsTwo.get(indexTwo));
			
			game.swapPiece();
			grid.playPiece(pieceTwo, gridXTwo.get(indexTwo), gridYTwo.get(indexTwo));
			
			game.afterPiece();
		} else if (indexTwo == -1) {
			pieceOne.rotate(rotationsOne.get(indexOne));
			grid.playPiece(pieceOne, gridXOne.get(indexOne), gridYOne.get(indexOne));

			game.afterPiece();
		} else {
			if (fitnessOne.get(indexOne) > fitnessTwo.get(indexTwo)) {
				pieceOne.rotate(rotationsOne.get(indexOne));
				grid.playPiece(pieceOne, gridXOne.get(indexOne), gridYOne.get(indexOne));
			} else {
				pieceTwo.rotate(rotationsTwo.get(indexTwo));
				
				game.swapPiece();
				grid.playPiece(pieceTwo, gridXTwo.get(indexTwo), gridYTwo.get(indexTwo));
			}
			
			game.afterPiece();
		}
		
		//Clear lists
		clearData();
		return true;
	}
	
	/**
	 * Get a given number that represents the quality of the piece to be played in the current position
	 * We want to play the piece with more blocks in a position that clears most lines, and in a way that
	 * leaves as few single blocks as possible. In case of cleared lines, no blocks are counted as single.
	 * 
	 * @return
	 * @throws InterruptedException 
	 */
	private double getFitness(GamePiece piece, int pieceSize, int x, int y) {
		logger.info("Bot calculating fitness");
		
		double linesCleared = 0;
		double singleBlocks = 0;
		
		//Get lines cleared or single blocks left
		double linesOrSingles = fullLines(piece, x, y);
		
		//If there are cleared lines, the single blocks are counted as 0.
		//If not, then the returned number is of number of single blocks.
		if (linesOrSingles < 10) linesCleared = linesOrSingles;
		else if (linesOrSingles == 10) singleBlocks = 0;
		else singleBlocks = linesOrSingles % 10;
		
		return (pieceSize*3*(linesCleared+1)) / ((singleBlocks+1)*4);
	}
	
	/**
	 * Calculates the number of lines filled with the given piece, and the number of single blocks left by adding the given piece.
	 * @param piece The piece we calculate on
	 * @param x Its X coordinate
	 * @param y Its Y coordinate
	 * @return the number of lines filled or the number of single blocks. Number of singles are marked from 10+ in order to distinguish them
	 * @throws InterruptedException 
	 */
	private double fullLines(GamePiece piece, int x, int y) {
		logger.info("Bot calculating lines");
		
		int[][] pieceBlocks = piece.getBlocks();

		int fitness = 1;
		double vicinityFitness = 1;
		
		int numberOfSingles = 10;

		//Copy the grid's values into an array
		for (var i = 0; i < gridBlocks.length; i++) {
			for (var j = 0; j < gridBlocks[x].length; j++) {
				gridBlocks[i][j] = grid.get(i, j);
			}
		}

    	Set<Integer> valX = new HashSet<Integer>();
    	Set<Integer> valY = new HashSet<Integer>();
    	
		//Fill the grid with the given piece and add the coordinates into sets
		for (var i = -1; i < 2; i++) {
    		for (var j = -1; j < 2; j++) {
    			if (pieceBlocks[i+1][j+1] != 0) {
    				gridBlocks[x+i][y+j] = pieceBlocks[i+1][j+1];
    				valX.add(x+i);
    				valY.add(y+j);
    				
    				vicinityFitness += getVicinityFitness(x+i, y+j);
    			}
    		}
    	}
		
		boolean toRemove = false;
		
		//Calculate number of lines filled and number of single blocks for each X coordinate on which we placed a block
		for (Integer value : valX) {
			int emptyBlocks = 0;
			
			//The Y coordinate of the empty block
			int yEmpty = 0;
			
			toRemove = true;
			
			for (var yb = 0; yb < gridBlocks[0].length; yb++) {
				if (gridBlocks[value][yb] == 0) {
					toRemove = false;

					if (getVicinityFitness(value, yb) == 0 || getCornerFitness(value, yb) == 1) numberOfSingles++;
				}
			}
			
			if (toRemove) fitness++;

		}
		
		//Calculate for each Y coordinate of a placed block
		for (Integer value : valY) {
			int emptyBlocks = 0;
			
			//The X coordinate of the empty block
			int xEmpty = 0;
			toRemove = true;
			
			for (var xb = 0; xb < gridBlocks.length; xb++) {
				if (gridBlocks[xb][value] == 0) {
					toRemove = false;

					if (getVicinityFitness(xb, value) == 0 || getCornerFitness(xb, value) == 1) numberOfSingles++;
				}
			}
			
			if (toRemove) fitness++;
		}
		
		//If no lines were removed, return the number of single blocks
		return (fitness/2 + vicinityFitness*10) / numberOfSingles*5;
	}
	
	/**
	 * Clear all the arrays
	 */
	private void clearData() {
		rotationsOne.clear();
		rotationsTwo.clear();
		gridXOne.clear();
		gridXTwo.clear();	
		gridYOne.clear();
		gridYTwo.clear();
		fitnessOne.clear();
		fitnessTwo.clear();
	}
	
	/**
	 * Get the fitness value of a block given that there are other blocks next to it
	 * @param x The current block's X coordinate
	 * @param y The current block's Y coordinate
	 * @return the collected fitness value of the block
	 */
	private double getVicinityFitness(int x, int y) {
		int fitnessV = 0;
		
		if (x-1 >= 0 && gridBlocks[x-1][y] != 0) fitnessV += 0.3;
		if (x+1 < grid.getCols() && gridBlocks[x+1][y] != 0) fitnessV += 0.3;
		if (y-1 >= 0 && gridBlocks[x][y-1] != 0) fitnessV += 0.3;
		if (y+1 < grid.getRows() && gridBlocks[x][y+1] != 0) fitnessV += 0.3;
		
		return fitnessV;
	}
	
	/**
	 * Get the fitness value of a block that's in a corner
	 * @param x The block's X coordinate
	 * @param y The block's Y coordinate
	 * @return The fitness value of a cornered block
	 */
	private double getCornerFitness(int x, int y) {
		int fitnessC = 0;
		
		if (x-1 < 0 && gridBlocks[x+1][y] != 0) fitnessC += 0.5;
		else if (x+1 == grid.getCols() && gridBlocks[x-1][y] != 0) fitnessC += 0.5;
		
		if (y-1 < 0 && gridBlocks[x][y+1] != 0) fitnessC += 0.5;
		else if (y+1 > 0 && gridBlocks[x][y-1] !=0) fitnessC += 0.5;
		
		return fitnessC;
	}
	
	public void setPieceOne(GamePiece piece) { this.pieceOne = piece; }
	public void setPieceTwo(GamePiece piece) { this.pieceTwo = piece; }
}
