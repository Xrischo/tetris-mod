package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Pair;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for its display.
 */
public class Grid {

    private static final Logger logger = LogManager.getLogger(Grid.class);

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The rows to be removed
     */
	ArrayList<Integer> rowsToRemove;
	
	/**
	 * The cols to be removed
	 */
	ArrayList<Integer> colsToRemove;
	
    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
        
        rowsToRemove = new ArrayList<Integer>();
        colsToRemove = new ArrayList<Integer>();
    }

    /**
     * Checks if the given piece can be played at the current location
     * @param piece The piece to be checked
     * @param x The row of the current block
     * @param y The column of the current block
     */
    public boolean canPlayPiece(GamePiece piece, int x, int y) {
    	int[][] pieceBlocks = piece.getBlocks();

    	/**
    	 * Check the 3x3 grid
    	 * @param k takes values -1 0 1 which correspond to the blocks above the center, the center and below it
    	 * @param j takes values -1 0 1 which correspond to the blocks left to the center, the center and to the right
    	 */
    	for (var k = -1; k < 2; k++) {
    		for (var j = -1; j < 2; j++) {
    			if (pieceBlocks[k+1][j+1] != 0) {
    				if (x + k < 0 || x + k >= this.cols ||
    						y + j < 0 || y + j >= this.rows ||
    						grid[x+k][y+j].get() != 0)
    					return false;
    			}
    		}
    	}
    	
    	return true;
    }
    
    /**
     * Updates the grid's values according to the played piece
     * @param piece The piece played
     * @param x The row of the current block
     * @param y The column of the current block
     */
    public void playPiece(GamePiece piece, int x, int y) {
    	logger.info("Playing piece " + piece.getName());
    	
    	int[][] pieceBlocks = piece.getBlocks();
    	
    	Set<Integer> valX = new HashSet<Integer>();
    	Set<Integer> valY = new HashSet<Integer>();
    	
    	for (var k = -1; k < 2; k++) {
    		for (var j = -1; j < 2; j++) {
    			if (pieceBlocks[k+1][j+1] != 0) {
    				grid[x+k][y+j].set(pieceBlocks[k+1][j+1]);
    				valX.add(x+k);
    				valY.add(y+j);
    			}
    		}
    	}
    	
    	updateGrid(new ArrayList<Integer>(valX), new ArrayList<Integer>(valY));
    }
 
    /**
     * Check and update the grid by removing the rows and columns that are full from the last played piece.
     * In order not to check the whole grid, we only check the rows and columns where blocks were placed.
     * @param valX List of X coordinates of placed blocks
     * @param valY List of Y coordinates of placed blocks
     */
    public void updateGrid(ArrayList<Integer> valX, ArrayList<Integer> valY) {
    	boolean toRemove = false;

    	clearData();
    	
    	for (int x : valX) {
    		toRemove = true;
    		for (var y = 0; y < cols; y++) {
    			if (grid[x][y].get() == 0) {
    				toRemove = false;
    				break;
    			}
    		}
    		if (toRemove) rowsToRemove.add(x);
    	}
    	
    	for (int y : valY) {
    		toRemove = true;
    		for (var x = 0; x < rows; x++) {
    			if (grid[x][y].get() == 0) {
    				toRemove = false;
    				break;
    			}
    		}
    		if (toRemove) colsToRemove.add(y);
    	}
    	
    	for (int row : rowsToRemove) { for (var col = 0; col < cols; col++) { grid[row][col].set(0); Multimedia.playAudio("clear.wav"); }}
    	for (int col : colsToRemove) { for (var row = 0; row < rows; row++) { grid[row][col].set(0); Multimedia.playAudio("clear.wav"); }}
    	
    }
    
    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
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
    
    /**
     * Clear which rows and cols were removed
     */
    public void clearData() {
    	rowsToRemove.clear();
    	colsToRemove.clear();
    }

    public ArrayList<Integer> getRowsRemoved() {
    	return rowsToRemove;
    }
    
    public ArrayList<Integer> getColsRemoved() {
    	return colsToRemove;
    }
}
