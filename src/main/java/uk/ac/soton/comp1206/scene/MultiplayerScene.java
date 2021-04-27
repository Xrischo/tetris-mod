package uk.ac.soton.comp1206.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.event.BlockMoveListener;
import uk.ac.soton.comp1206.event.GameTimerListener;
import uk.ac.soton.comp1206.event.MessageListener;
import uk.ac.soton.comp1206.event.PieceChangeListener;
import uk.ac.soton.comp1206.event.PlayersScoreListener;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * Set the scene for the multiplayer game
 *
 */
public class MultiplayerScene extends ChallengeScene {
	
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);
    
    /**
     * The game logic
     */
    private MultiplayerGame game;
    
    /**
     * The numbre of players
     */
    private VBox players;
    
    /**
     * The text for players
     */
    private Text playerText;
    
    /**
     * The textfield to send messages
     */
    private TextField textField;
    
    /**
     * Basic constructor to initialise the current window
     * @param gameWindow
     */
	public MultiplayerScene(GameWindow gameWindow) {
		super(gameWindow);
		logger.info("Creating Multiplayer Scene");
	}
	
	/**
	 * Initialise the scene
	 */
	@Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        Multimedia.playMusic("game_start.wav");
        
        game.addPieceChangeListener(new PieceChangeListener() {

			@Override
			public void pieceChange(GamePiece piece, int code) {
				if (code == 0) { 
					firstPiece.displayPiece(piece);
					board.calculateModel(firstPiece);
				}
				else secondPiece.displayPiece(piece);
				
				if (game.getScore().get() > currentHS) 
					highscore.setText(((Integer) game.getScore().get()).toString());
			}
				
        });
        
        scene.setOnKeyPressed((key) ->  {
        	if (key.getCode().equals(KeyCode.Y)) textField.setVisible(true);
        	game.handleInput(key);
        });
        
    }
	
	/**
	 * Setup the game 
	 */
	@Override
	public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game and initialise animators
        game = new MultiplayerGame(5, 5, gameWindow.getCommunicator());
        timeline = new Timeline();
        scoreTimeline = new Timeline();
        
        //Initialise players list and textfield
        players = new VBox();
        playerText = new Text();
        textField = new TextField();

        //Add listeners
        game.addBlockMoveListener(new BlockMoveListener() {

			@Override
			public void blockMoved(int x, int y) {
				board.changeCurrentBlock(x, y);
				if (board.getShowModel()) board.calculateModel(firstPiece);
			}
        	
        });
        
        game.addModelListener(() -> { 
        	board.switchModel(); 
        	board.calculateModel(firstPiece);
        });
        
        game.addTimerListener(new GameTimerListener() {

			public void updateTimer(long period) {
				timerAnimation(period);
			}
		});
        
        game.addGameOverListener(() -> {
        	Platform.runLater(new Runnable() {

			public void run() {
				game.cleanUp();
				Multimedia.musicPlayer.stop();
				Multimedia.playAudio("explode.wav");
				gameWindow.startLeaderboard(game);
			}
        		
        	});
        });
        
        //Update the score on each player
        game.addScoreListener(new PlayersScoreListener() {

			@Override
			public void playerScore(String score) {
				players.getChildren().clear();
				double offset = 1;
				
				for (var player : score.split("\n")) {
					offset += 2;
					
					var nextPlayer = new Text(player.split(":")[0] + ": " + player.split(":")[1]);
					nextPlayer.getStyleClass().add("scorelist");
					nextPlayer.setFill(new Color(1 - 1/offset, offset/(offset+6), 0.5, 1));
					
					if (player.split(":")[2].equals("DEAD")) nextPlayer.setStrikethrough(true);
					
					players.getChildren().add(nextPlayer);
					
					VBox.setMargin(nextPlayer, new Insets(0, 10, 0, 20));
				}
			}
        	
        });
        
        game.addMessageListener(new MessageListener() {

			@Override
			public void receiveMessage(String message) {
				playerText.setText("<" + message.split(":")[0] + "> " 
						+ message.replace(message.split(":")[0] + ":", ""));
			}
        	
        });
        
        //Need to initialise this useless variable otherwise Exception is thrown smh
        currentHS = 0;
    }

	/**
	 * Build the layout of the scene
	 */
	@Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        //Set root
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        //BG Pane
        var backgroundPane = new StackPane();
        backgroundPane.setMaxWidth(gameWindow.getWidth());
        backgroundPane.setMaxHeight(gameWindow.getHeight());
        backgroundPane.getStyleClass().add("menu-background");
        root.getChildren().add(backgroundPane);

        //Setup board and layout
        setupGame();
        super.game = game;
        setupLayout(backgroundPane);

        //Put gameboard and texts received with the textfield in a VBox
        var boardAndText = new VBox();
        playerText.getStyleClass().add("playerBox");
        playerText.setFill(Color.WHITE);
        textField.getStyleClass().add("textField");
        
        boardAndText.getChildren().addAll(board, playerText, textField);
        mainPane.setCenter(boardAndText);
        
        BorderPane.setMargin(boardAndText, new Insets(0, 30, 0, gameWindow.getWidth()/4));
        
        textField.setVisible(false);
        textField.setOnKeyPressed((key) -> {
        	if (key.getCode().equals(KeyCode.ENTER)) {
        		gameWindow.getCommunicator().send("MSG " + textField.getText());
        		textField.setVisible(false);
        	} else if (key.getCode().equals(KeyCode.ESCAPE)) textField.setVisible(false);
        });
        
        //Remove the right layout to put players' info on top
        essentials.getChildren().removeAll(highscoreText, highscore, levelText, level, incomingText, firstPiece, secondPiece);
        essentials.getChildren().addAll(players, levelText, level, incomingText, firstPiece, secondPiece);
        
    }
}
