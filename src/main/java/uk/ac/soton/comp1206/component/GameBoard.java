package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 *
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 *
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    /**
     * Number of columns in the board
     */
    private final int cols;

    /**
     * Number of rows in the board
     */
    private final int rows;

    /**
     * The visual width of the board - has to be specified due to being a Canvas
     */
    private final double width;

    /**
     * The visual height of the board - has to be specified due to being a Canvas
     */
    private final double height;

    /**
     * The grid this GameBoard represents
     */
    final Grid grid;

    /**
     * The blocks inside the grid
     */
    GameBlock[][] blocks;
    HoverBlock[][] model;
    
    /**
     * The current block on the grid
     */
    HoverBlock currentBlock;
    
    /**
     * The block that we place on top of the hovered block
     */
    HoverBlock hoverBlock;

    /**
     * 
     */
    private int hoverBlockX;
    private int hoverBlockY;
    
    /**
     * The row of the current highlighted block on the grid
     */
    public int xCurrent;
    public int xCurrentPrev;
    public int xCurrentNext;
    
    /**
     *  The column of the current highlighted block on the grid
     */
    public int yCurrent;
    public int yCurrentPrev;
    public int yCurrentNext;
    
    /**
     * The listener to call when a specific block is clicked
     */
    private BlockClickedListener blockClickedListener;

    /**
     * 
     */
    private boolean showModel = false;

    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;

        //Build the GameBoard
        build();
    }

    /**
     * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols,rows);

        //Build the GameBoard
        build();
    }

    /**
     * Get a specific block from the GameBoard, specified by it's row and column
     * @param x row
     * @param y column
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
    	return blocks[x][y];
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    private void build() {
        logger.info("Building grid: {} x {}",cols,rows);

        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];
        model = new HoverBlock[3][3];

        for(var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x,y);
            }
        }
        
        //Initialise the current block and the hover block only if this is an instance of gameboard
       	hoverBlock = new HoverBlock(this, 0, 0, width / cols, height / rows);
        hoverBlock.getGraphicsContext2D().clearRect(0, 0, width / cols, height / rows);
        hoverBlock.getGraphicsContext2D().setFill(new Color(1, 1, 1, 0.2));
        hoverBlock.getGraphicsContext2D().fillRect(0, 0, width / cols, height / rows);
             
        //When we click it, register it as a click on the block below
        hoverBlock.setOnMouseClicked((e) -> blockClicked(e, getBlock(hoverBlockX, hoverBlockY)));
        hoverBlock.setVisible(false);
            
        getChildren().add(hoverBlock);
            
       	currentBlock = createCurrentBlock(); 
      	currentBlock.setOnMouseEntered((e) -> { setConstraints(hoverBlock, xCurrent, yCurrent); });
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     * @param x column
     * @param y row
     */
    protected GameBlock createBlock(int x, int y) {
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
        
        //Set on hover settings
        block.setOnMouseEntered((e) -> { hoverSettings(block); });  
        this.setOnMouseExited((e) -> { hoverBlock.setVisible(false); });
        
        return block;
    }
    
    public HoverBlock createCurrentBlock() {
    	
    	double blockWidth = width / cols;
    	double blockHeight = height / rows;
    	
    	//Initialise blocks for the model
    	for (var x = -1 ; x < 2; x++) {
    		for (var y = -1; y < 2; y++) {
    			getChildren().add(model[x+1][y+1] = new HoverBlock(this, 0, 0, blockWidth, blockHeight));
    			
    			if (x == -1 && y == -1) { 
    				model[x+1][y+1].setOnMouseClicked((e) -> blockClicked(e, getBlock(xCurrentPrev, yCurrentPrev)));
    				model[x+1][y+1].setOnMouseEntered((e) -> setConstraints(hoverBlock, xCurrentPrev, yCurrentPrev));
    			} else if (x == -1 && y == 0) {
    				model[x+1][y+1].setOnMouseClicked((e) -> blockClicked(e, getBlock(xCurrentPrev, yCurrent)));
    				model[x+1][y+1].setOnMouseEntered((e) -> setConstraints(hoverBlock, xCurrentPrev, yCurrent));
    			} else if (x == -1) {
    				model[x+1][y+1].setOnMouseClicked((e) -> blockClicked(e, getBlock(xCurrentPrev, yCurrentNext)));
    				model[x+1][y+1].setOnMouseEntered((e) -> setConstraints(hoverBlock, xCurrentPrev, yCurrentNext));
    			} else if (x == 0 && y == -1) {
    				model[x+1][y+1].setOnMouseClicked((e) -> blockClicked(e, getBlock(xCurrent, yCurrentPrev)));
    				model[x+1][y+1].setOnMouseEntered((e) -> setConstraints(hoverBlock, xCurrent, yCurrentPrev));
    			} else if (x == 0 && y == 0) {
    				model[x+1][y+1].setOnMouseClicked((e) -> blockClicked(e, getBlock(xCurrent, yCurrent)));
    				model[x+1][y+1].setOnMouseEntered((e) -> setConstraints(hoverBlock, xCurrent, yCurrent));
    			} else if (x == 0) {
    				model[x+1][y+1].setOnMouseClicked((e) -> blockClicked(e, getBlock(xCurrent, yCurrentNext)));
    				model[x+1][y+1].setOnMouseEntered((e) -> setConstraints(hoverBlock, xCurrent, yCurrentNext));
    			} else if (x == 1 && y == -1) {
    				model[x+1][y+1].setOnMouseClicked((e) -> blockClicked(e, getBlock(xCurrentNext, yCurrentPrev)));
    				model[x+1][y+1].setOnMouseEntered((e) -> setConstraints(hoverBlock, xCurrentNext, yCurrentPrev));
    			} else if (x == 1 && y == 0) {
    				model[x+1][y+1].setOnMouseClicked((e) -> blockClicked(e, getBlock(xCurrentNext, yCurrent)));
    				model[x+1][y+1].setOnMouseEntered((e) -> setConstraints(hoverBlock, xCurrentNext, yCurrent));
    			} else {
    				model[x+1][y+1].setOnMouseClicked((e) -> blockClicked(e, getBlock(xCurrentNext, yCurrentNext)));
    				model[x+1][y+1].setOnMouseEntered((e) -> setConstraints(hoverBlock, xCurrentNext, yCurrentNext));
    			}
    			
    			model[x+1][y+1].setVisible(false);
    			
    		}
    	}
    	
    	//middle block of the model is the currentBlock
        model[1][1].setVisible(true);
        model[1][1].getGraphicsContext2D().setFill(new Color(1, 1, 1, 0.2));
        model[1][1].getGraphicsContext2D().fillRect(0, 0, width / cols, height / rows);
        
        model[1][1].setOnMouseClicked((e) -> blockClicked(e, getBlock(xCurrent, yCurrent)));
        
        return model[1][1];
    }
    
    /**
     * Calculate the way the piece would currently look like on the grid
     * or render it invisible if we turn it off
     * @param piece the piece that is to be placed
     */
    public void calculateModel(PieceBoard piece) {
    	
    	if (showModel) {
    		int xModel;
    		int yModel;
    		
    		for (var x = -1; x < 2; x++) {
    			
    			if (x == -1) xModel = xCurrentPrev;
    			else if (x == 0) xModel = xCurrent;
    			else xModel = xCurrentNext;
    			
        		for (var y = -1; y < 2; y++) {
        			
        			if (y == -1) yModel = yCurrentPrev;
        			else if (y == 0) yModel = yCurrent;
        			else yModel = yCurrentNext;
        			
        			setConstraints(model[x+1][y+1], xModel, yModel);

        			var gc = model[x+1][y+1].getGraphicsContext2D();
        			Color pieceBlockColour = (Color) piece.blocks[x+1][y+1].getGraphicsContext2D().getFill();
        			gc.clearRect(0, 0, width / cols, height / rows);
       				gc.setFill(Color.color(
      					pieceBlockColour.getRed()/2, pieceBlockColour.getGreen()/2, 
      					pieceBlockColour.getBlue()/2, pieceBlockColour.getOpacity()/1.5));

        			
       				gc.fillRect(0, 0, width / cols, height / rows);
        			
       				model[x+1][y+1].setVisible(true);
        		
        		}
        	}
    	} else {
    		for (var x = 0; x < 3; x++) {
    			for (var y = 0; y < 3; y++) {
    				model[x][y].setVisible(false);
    			}
    		}
    		currentBlock.setVisible(true);
            currentBlock.getGraphicsContext2D().setFill(new Color(1, 1, 1, 0.2));
    		currentBlock.getGraphicsContext2D().clearRect(0, 0, width / cols, height / rows);
            currentBlock.getGraphicsContext2D().fillRect(0, 0, width / cols, height / rows);
            
            currentBlock.setOnMouseClicked((e) -> blockClicked(e, getBlock(xCurrent, yCurrent)));
    	}
    }

    /**
     * Connected to a listener, turn the model on and off 
     */
    public void switchModel() {	showModel = !showModel; }
    
    /**
     * Set what happens when the mouse hovers over the given block
     * @param block the block we hover over
     */
    protected void hoverSettings(GameBlock block) {
    	hoverBlockX = block.getX();
    	hoverBlockY = block.getY();
    	
    	setConstraints(hoverBlock, hoverBlockX, hoverBlockY);
   		hoverBlock.setVisible(true);
    }
    
    protected void hoverSettings(int x, int y) {
    	if (x < 0 && y < 0) {
    		setConstraints(hoverBlock, rows - 1, cols - 1);
    	} else if (x >= rows && y >= cols) {
    		setConstraints(hoverBlock, 0, 0);
    	} else if (x < 0) {
    		setConstraints(hoverBlock, rows - 1, y);
    	} else if (x >= rows) {
    		setConstraints(hoverBlock, 0, y);
    	} else if (y < 0) {
    		setConstraints(hoverBlock, x, cols - 1);
    	} else if (y >= cols) {
    		setConstraints(hoverBlock, x, 0);
    	} else setConstraints(hoverBlock, x, y);
    }
    /**
     * Change the current gameblock
     * @param xOffset the offset by cols
     * @param yOffset the offset by rows
     */
    public void changeCurrentBlock(int x, int y) { 
    	setConstraints(currentBlock, x, y); 
    	xCurrent = x;
    	yCurrent = y;
    	
    	xCurrentPrev = x - 1 < 0 ? cols - 1 : x - 1;
    	xCurrentNext = x + 1 == cols ? 0 : x + 1;
    	
    	yCurrentPrev = y - 1 < 0 ? rows - 1 : y - 1;
    	yCurrentNext = y + 1 == rows ? 0 : y + 1;
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
    
    public boolean getShowModel() { return showModel; }
    
}
