package uk.ac.soton.comp1206.component;

/**
 * This is a block that is displayed when the mouse hovers over a block on the grid
 *
 */
public class HoverBlock extends GameBlock {

	/**
	 * The X coordinate of this block
	 */
	private int xBlock;
	
	/**
	 * The Y coordinate of this block
	 */
	private int yBlock;
	
	/**
	 * Create the hoverblock in the given gameboard
	 * @param gameBoard The gameboard
	 * @param x the X of the block
	 * @param y the Y of the block
	 * @param width the width of the block
	 * @param height the height of the block
	 */
	public HoverBlock(GameBoard gameBoard, int x, int y, double width, double height) {
		super(gameBoard, x, y, width, height);
		
		xBlock = x;
		yBlock = y;
	}
	
	public void setXBlock(int x) { xBlock = x; }
	
	public void setYBlock (int y) { yBlock = y; }
	
	public int getXBlock() { return xBlock; }
	
	public int getYBlock() { return yBlock; }
}
