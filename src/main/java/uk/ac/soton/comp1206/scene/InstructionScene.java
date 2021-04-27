package uk.ac.soton.comp1206.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * This scene shows the instructions to play the game
 *
 */
public class InstructionScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(InstructionScene.class);

    /**
     * Constructor
     * @param gameWindow The window that this scene is in
     */
	public InstructionScene(GameWindow gameWindow) {
		super(gameWindow);
	}

	/**
	 * Initialise what happens when we press ESC
	 */
	@Override
	public void initialise() {
        
        gameWindow.getScene().setOnKeyPressed((key) -> {
        	if (key.getCode().equals(KeyCode.ESCAPE)) {
        		logger.info("Closing instructions");
        		gameWindow.startMenu();
        	}
        });
	}

	/**
	 * Build the scene's layout
	 */
	@Override
	public void build() {
		logger.info("Building " + this.getClass().getName());

		//Set root
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        //Set panes
        var instructionPane = new StackPane();
        instructionPane.setMaxWidth(gameWindow.getWidth());
        instructionPane.setMaxHeight(gameWindow.getHeight());
        instructionPane.getStyleClass().add("instructions-background");
        root.getChildren().add(instructionPane);
        
        //Hold all elements in a VBox
        var vbox = new VBox();
        instructionPane.getChildren().add(vbox);
        
        //Image res 1368 x 846 (16 : 10)
        //Load an image from resources and scale it by the gamewindow, preserving the ratio
        Image image = new Image(getClass().getResource("/images/Instructions.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(gameWindow.getWidth()/1.4);
        imageView.setFitHeight(gameWindow.getWidth()/(1.4*1.6));
        
        var text = new Text("Game Pieces");
        text.getStyleClass().add("heading");
        
        vbox.getChildren().addAll(imageView, text);
        VBox.setMargin(imageView, new Insets(0, 0, 0, gameWindow.getWidth()/8));
        VBox.setMargin(text, new Insets(0, 0, 0, gameWindow.getWidth()/2.3));
        
        
        //Initialise every piece in a pieceboard and add them in 3 HBox components
        int index = 0;
        for (var i = 0; i < 3; i++) {
        	var pieceRow = new HBox();
        	
        	for (var j = 0; j < 5; j++) {
        		double res = (double) gameWindow.getWidth()/(double) gameWindow.getHeight();
        		
        		PieceBoard pieceBoard = new PieceBoard(new Grid(3,3), 
        				gameWindow.getWidth()/15, gameWindow.getHeight()/15*res);
        		GamePiece gamePiece = GamePiece.createPiece(index);
        		pieceBoard.displayPiece(gamePiece);
        		
        		pieceRow.getChildren().add(pieceBoard);
        		HBox.setMargin(pieceBoard, new Insets(0, 0, 0, 15));
        		
        		index++;
        	}
        	
        	vbox.getChildren().add(pieceRow);
        	VBox.setMargin(pieceRow, new Insets(10, 0, 10, gameWindow.getWidth()/3.4));
        }

	}
	
}
