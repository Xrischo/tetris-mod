package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * The Board that holds the next pieces
 *
 */
public class PieceBoard extends GridPane {
	
    private static final Logger logger = LogManager.getLogger(PieceBoard.class);
    
    /**
     * Number of columns
     */
    private final int cols;
    
    /**
     * Number of rows
     */
    private final int rows;
    
    /**
     * The width of the board
     */
    private final double width;
    
    /**
     * The height of the board
     */
    private final double height;
    
    /**
     * The grid that represents the board
     */
    final Grid grid;
    
    /**
     * The gameblocks of the board
     */
    GameBlock[][] blocks;
    
    /**
     * Listener for each click on a block
     */
    private BlockClickedListener blockClickedListener;
    
    /**
     * Constructor for the board
     * @param grid The grid of the board
     * @param width The width
     * @param height The height
     */
	public PieceBoard(Grid grid, double width, double height) {
		this.grid = grid;
		this.width = width;
		this.height = height;
		this.cols = grid.getCols();
		this.rows = grid.getRows();
		
		build();
	}
	
	/**
	 * Build the board's layout
	 */
	private void build() {
        logger.info("Building Piece Board: {} x {}",cols,rows);

        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];

        for(var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x,y);
            }
        }
        
        //Add a circle in the middle
        addCircleBlock();
    }
	
	/**
	 * The Game Block to be created
	 * @param x The X coordinate of the new block
	 * @param y The Y coordinate of the new block
	 * @return the new gameblock
	 */
	private GameBlock createBlock(int x, int y) {
        var blockWidth = width / cols;
        var blockHeight = height / rows;

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        //Add to the GridPane
        add(block,x,y);

        //Add to our block directory
        blocks[x][y] = block;

        //Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x,y));

        //Add a mouse click handler to the block to trigger GameBoard blockClicked method
        block.setOnMouseClicked((e) -> blockClicked(e, block));

        return block;
    } 
	
	/**
	 * Render a piece when it has changed (rotated/swapped etc.)
	 * @param piece The piece to be rendered
	 */
	public void displayPiece(GamePiece piece) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				grid.getGridProperty(i, j).set(piece.getBlocks()[i][j]);
			}
		}
	}
	
	/**
	 * Add a circle in the middle
	 */
	private void addCircleBlock() {
		//Make a new block with the dimensions of this board's blocks
		double widthBlock = width / this.getColumnCount();
		double heightBlock = height / this.getRowCount();

		GameBlock circleBlock = new GameBlock(this, 1, 1, widthBlock, heightBlock);
		
		//Add the new block in the middle
		add(circleBlock, 1, 1);
		
		//Draw the circle
		var gc = circleBlock.getGraphicsContext2D();
		
		gc.clearRect(0, 0, widthBlock, heightBlock);
		
		gc.setFill(Color.color(1, 1, 1, 0.6));
		gc.fillOval(widthBlock/4, heightBlock/4, widthBlock/2, heightBlock/2);
		
	}
	
	/**
     * Set the listener to handle an event when a block is clicked
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     * @param event mouse event
     * @param block block clicked on
     */
    protected void blockClicked(MouseEvent event, GameBlock block) {
        logger.info("Block clicked: {}", block);

        if(blockClickedListener != null) {
            blockClickedListener.blockClicked(block);
        }
    }
}
