package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.BlockMoveListener;
import uk.ac.soton.comp1206.event.PieceChangeListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    
    /**
     * The gameboard
     */
    GameBoard board;
    
    /**
     * The mainpane
     */
    protected BorderPane mainPane;
    
    /**
     * Essentials on the right
     */
    protected VBox essentials;
    
    /**
     * The text "Incoming"
     */
    protected Text incomingText;
    
    /**
     * The text "Level"
     */
    protected Text levelText;
    
    /**
     * The level
     */
    protected Text level;
    
    /**
     * The first side piece
     */
    protected PieceBoard firstPiece;
    
    /**
     * The second side piece
     */
    protected PieceBoard secondPiece;
    
    /**
     * The timeline for the timer animation
     */
    protected Timeline timeline;
    
    /**
     * The timeline to animate score going up
     */
    protected Timeline scoreTimeline;
    
    /**
     * The value of the score during animation
     */
    private SimpleIntegerProperty scoreAnim;
    
    /**
     * The UI element of the timer
     */
    private Rectangle timer;
    
    /**
     * The game instance
     */
    protected Game game;
    
    /**
     * The text "Highscore"
     */
    protected Text highscoreText;
    
    /**
     * The highscore value displayed
     */
    protected Text highscore;
    
    /**
     * The current highscore
     */
    protected Integer currentHS;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        //BG Pane
        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        //Setup board and layout
        setupGame();
        setupLayout(challengePane);
    }
    
    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game and initialise animators
        game = new Game(5, 5);
        timeline = new Timeline();
        scoreTimeline = new Timeline();

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
        
        game.addTimerListener(this::timerAnimation);
        
        game.addGameOverListener(() -> {
        	Platform.runLater(new Runnable() {

			public void run() {
				game.cleanUp();
				Multimedia.musicPlayer.stop();
				Multimedia.playAudio("explode.wav");
				gameWindow.startScores(game);
			}
        		
        	});
        });
        
        //The displayed highscore
        currentHS = getHighscore();
    }
    
    /**
     * Set the layout for the scene. BorderPane inside StackPane, different Panes inside
     * @param bg the StackPane of the scene
     */
    public void setupLayout(StackPane bg) {
    	 //BorderPane
        mainPane = new BorderPane();
        bg.getChildren().add(mainPane);
        
        //BorderPane to hold the score, title and lives
        var title = new BorderPane();
        mainPane.setTop(title);
        
        //VBox for score
        var scorePane = new VBox();
        title.setLeft(scorePane);
        
        //VBox for lives
        var livesPane = new VBox();
        title.setRight(livesPane);
        
        //VBox for highscore, level and upcoming pieces
        essentials = new VBox();
        mainPane.setRight(essentials);
        
        //Create the main GameBoard
        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);
        
        //Create the two PieceBoards
        firstPiece = new PieceBoard(new Grid(3, 3), gameWindow.getWidth()/6, gameWindow.getWidth()/6);
        secondPiece = new PieceBoard(new Grid(3, 3), gameWindow.getWidth()/9, gameWindow.getWidth()/9);
        
        //add here timer at the bottom
        timer = new Rectangle(gameWindow.getWidth(), 20);
        timer.minWidth(0);
        timer.setFill(Color.GREEN);
        mainPane.setBottom(timer);
        
        //Text
        var titleText = new Text("Challenge Mode");
        var scoreText = new Text("Score");
        var livesText = new Text("Lives");
        highscoreText = new Text("Highscore");
        incomingText = new Text("Incoming");
        levelText = new Text("Level");
        
        //Scores
        var scoreAnimTemp = new SimpleIntegerProperty();
        scoreAnim = new SimpleIntegerProperty();
        scoreAnim.bind(game.getScore());
        
        scoreAnim.addListener((listener) -> {
        	scoreTimeline.stop();
        	scoreTimeline.getKeyFrames().remove(0, scoreTimeline.getKeyFrames().size());
        	
        	scoreTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(500), 
        			new KeyValue(scoreAnimTemp, scoreAnim.get())));
        	scoreTimeline.play();
        });
        
        var score = new Text();
        score.textProperty().bind(scoreAnimTemp.asString());
        
        var lives = new Text();
        lives.textProperty().bind(game.getLives().asString());
        
        level = new Text();
        level.textProperty().bind(game.getLevel().asString());
        
        highscore = new Text(currentHS.toString());
        
        //Set the css style
        titleText.getStyleClass().add("title");
        scoreText.getStyleClass().add("heading");
        levelText.getStyleClass().add("heading");
        livesText.getStyleClass().add("heading");
        highscoreText.getStyleClass().add("heading");
        incomingText.getStyleClass().add("heading");
        score.getStyleClass().add("score");
        lives.getStyleClass().add("score");
        level.getStyleClass().add("level");
        highscore.getStyleClass().add("hiscore");

        //Add all nodes to the panes
        scorePane.getChildren().addAll(scoreText, score);
        livesPane.getChildren().addAll(livesText, lives);
        title.setCenter(titleText);
        essentials.getChildren().addAll(highscoreText, highscore, levelText, level, incomingText, firstPiece, secondPiece);
        
        //Set the correct placement of the nodes in the panes        
        BorderPane.setMargin(titleText, new Insets(25, 0, 10, 70));
        BorderPane.setMargin(scorePane, new Insets(20, 10, 20, 20));
        BorderPane.setMargin(livesPane, new Insets(20, 20, 20, 10));
        VBox.setMargin(score, new Insets(0, 0, 0, 0));
        VBox.setMargin(lives, new Insets(0, 0, 0, 20));
        VBox.setMargin(highscoreText, new Insets(0, 20, 10, 30));
        VBox.setMargin(highscore, new Insets(0, 0, 10, 50));
        VBox.setMargin(levelText, new Insets(0, 20, 10, 55));
        VBox.setMargin(level, new Insets(0, 20, 10, 83));
        VBox.setMargin(incomingText, new Insets(0, 20, 5, 40));
        VBox.setMargin(firstPiece, new Insets(5, 5, 40, 20));
        VBox.setMargin(secondPiece, new Insets(5, 5, 0, 40));

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        firstPiece.setOnBlockClick((block) -> { game.currentPiece.rotate(); firstPiece.displayPiece(game.currentPiece); });
        secondPiece.setOnBlockClick((block) -> { game.nextPiece.rotate(); secondPiece.displayPiece(game.nextPiece); });
    }

    /**
     * The animation of the UI timer
     * @param period How long the timer is
     */
    protected void timerAnimation(long period) {
    	timeline.stop();
    	timeline.getKeyFrames().remove(0, timeline.getKeyFrames().size());
    	
    	timer.setFill(Color.GREEN);
    	timer.setWidth(gameWindow.getWidth());
    	
    	timeline.getKeyFrames().add(new KeyFrame(Duration.millis(period),
    			new KeyValue(timer.widthProperty(), 0),
    			new KeyValue(timer.fillProperty(), Color.RED)));
    	
    	timeline.play();
    }
    
    /**
     * Get the highscore, update it if we have a higher
     * @return highscore
     */
    private Integer getHighscore() {
		int score = 0;

    	try {
			File file = new File("localScores.txt");
    		BufferedReader reader = new BufferedReader(new FileReader(file));
			String temp;
			
			if ((temp = reader.readLine()) != null) {
				score = Integer.parseInt(temp.split(":")[1]);
			}
			
			reader.close();
		} catch (Exception e) {
			logger.info("File not found");
		}
	
    	return score;
    }
    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clicked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        Multimedia.playMusic("game_start.wav");
        
        //Get pieces
        firstPiece.displayPiece(game.currentPiece);
        secondPiece.displayPiece(game.nextPiece);
        
        //Add listeners to the pieces
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
        
        //When we press a key
        scene.setOnKeyPressed((key) -> {
        	if (key.getCode().equals(KeyCode.ESCAPE)) {
        		game.cleanUp();
        		Multimedia.musicPlayer.stop();
        		Multimedia.playAudio("rotate.wav");
        		gameWindow.startMenu();
        	}
        	game.handleInput(key);
        });
    }

}
