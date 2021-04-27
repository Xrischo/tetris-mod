package uk.ac.soton.comp1206.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
    	Multimedia.playMusic("menu.mp3");

        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        //Set panes
        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);
        
        //Image res: 4552 x 912 (5:1)
        Image image = new Image(getClass().getResource("/images/TetrECS.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(gameWindow.getWidth()/1.3);
        imageView.setFitHeight(gameWindow.getWidth()/(1.3*5));
        imageView.setRotate(-15);
        mainPane.setTop(imageView);

        var buttonPane = new VBox();
        mainPane.setCenter(buttonPane);

        //Buttons
        var singlePlayerButton = new Text("Single Player");
        var multiPlayerButton = new Text("Multiplayer");
        var guideButton = new Text("How To Play");
        var quitButton = new Text("Quit");
        
        singlePlayerButton.getStyleClass().add("title");
        multiPlayerButton.getStyleClass().add("title");
        guideButton.getStyleClass().add("title");
        quitButton.getStyleClass().add("title");
        buttonPane.getChildren().addAll(singlePlayerButton, multiPlayerButton, guideButton, quitButton);
        
        BorderPane.setMargin(buttonPane, new Insets(80, 0, gameWindow.getHeight()/8, gameWindow.getWidth()/3));
        BorderPane.setMargin(imageView, new Insets(gameWindow.getHeight()/6, 
        		0, gameWindow.getHeight()/10, gameWindow.getWidth()/8));
        VBox.setMargin(multiPlayerButton, new Insets(0, 0, 0, 20));
        VBox.setMargin(quitButton, new Insets(0, 0, 0, 80));
        
        singlePlayerButton.setOnMouseClicked((e) -> { this.startGame(e); Multimedia.playAudio("rotate.wav"); });
        singlePlayerButton.setOnMouseEntered((e) -> { singlePlayerButton.setFill(Color.YELLOW); });
        singlePlayerButton.setOnMouseExited((e) -> { singlePlayerButton.setFill(Color.WHITE); });
        
        multiPlayerButton.setOnMouseClicked((e) -> { this.openLobby(e); Multimedia.playAudio("rotate.wav"); });
        multiPlayerButton.setOnMouseEntered((e) -> { multiPlayerButton.setFill(Color.YELLOW); });
        multiPlayerButton.setOnMouseExited((e) -> { multiPlayerButton.setFill(Color.WHITE); });
        
        guideButton.setOnMouseClicked((e) -> { this.openGuide(e); Multimedia.playAudio("rotate.wav"); });
        guideButton.setOnMouseEntered((e) -> { guideButton.setFill(Color.YELLOW); });
        guideButton.setOnMouseExited((e) -> { guideButton.setFill(Color.WHITE); });
        
        quitButton.setOnMouseClicked((e) -> { System.exit(0); Multimedia.playAudio("rotate.wav"); });
        quitButton.setOnMouseEntered((e) -> { quitButton.setFill(Color.YELLOW); });
        quitButton.setOnMouseExited((e) -> { quitButton.setFill(Color.WHITE); });
        
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(5000),
        		new KeyValue(imageView.rotateProperty(), 15, Interpolator.EASE_BOTH)));
        timeline.play();
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {}

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(MouseEvent event) {
    	Multimedia.musicPlayer.stop();
        gameWindow.startChallenge();
    }

    /**
     * Open the lobby
     * @param event event
     */
    private void openLobby(MouseEvent event) {
    	gameWindow.startLobby();
    }
    
    /**
     * Open the guide
     * @param event event
     */
    private void openGuide(MouseEvent event) {
    	gameWindow.startGuide();
    }
}
