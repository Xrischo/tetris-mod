package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

public interface PieceChangeListener {

	/**
	 * Called when pieces are changed in the upcoming pieces (rotated / new pieces etc.)
	 * @param piece the gamepiece that is changed
	 * @param code the code for the piece: 0 - current piece, 1 - next piece
	 */
	public void pieceChange(GamePiece piece, int code);
}
