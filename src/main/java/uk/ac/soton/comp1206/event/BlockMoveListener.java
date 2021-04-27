package uk.ac.soton.comp1206.event;

import javafx.scene.input.KeyEvent;

/**
 *	Handle what happens when the current highlighted block on the grid is changed
 */
public interface BlockMoveListener {

	/**
	 * Update the moved block
	 * @param x The new X position
	 * @param y The new Y position
	 */
	public void blockMoved(int x, int y);
}
