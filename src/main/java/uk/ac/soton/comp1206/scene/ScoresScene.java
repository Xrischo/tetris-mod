package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoresUI;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * This scene holds the scores at the end of a singe player or a multiplayer game.
 */
public class ScoresScene extends BaseScene {

	/**
	 * Logger of the class
	 */
    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    /**
     * The multiplayer game
     */
    private MultiplayerGame mpGame;
    
    /**
     * The single player game
     */
	private Game game;
	
	/**
	 * The communicator of the game
	 */
	private Communicator communicator;
	
	/**
	 * The scores on the left side of the layout - either local scores or multiplayer leaderboard
	 */
	private ScoresUI localScoresUI;
	
	/**
	 * The scores on the right side of the layout - online highscores
	 */
	private ScoresUI onlineScoresUI;
	
	/**
	 * The background pane
	 */
	private StackPane scorePane;
	
	/**
	 * The layout pane
	 */
	private BorderPane borderPane;
	
	/**
	 * The list of pairs name:score for local scores
	 */
	SimpleListProperty<Pair> localScores;
	
	/**
	 * The list of pairs name:score for online scores
	 */
	SimpleListProperty<Pair> onlineScores;
	
	/**
	 * The list of strings name:score:<lives|DEAD> for multiplayer games
	 */
	SimpleListProperty<String> boardScores;
	
	/**
	 * Value that indicates whether we have been prompted to add a new highscore
	 */
	private boolean scoreAdded = false;
	
	
	/**
	 * Constructor for single player
	 * @param gameWindow The game window
	 * @param game The game logic
	 */
	public ScoresScene(GameWindow gameWindow, Game game) {
		super(gameWindow);
		this.game = game;
		this.communicator = gameWindow.getCommunicator();
		Multimedia.playMusic("end.wav");
		
		//Initialise scores
		localScores = new SimpleListProperty<Pair>(FXCollections.observableArrayList(
				new ArrayList<Pair<String, Integer>>()));
		onlineScores = new SimpleListProperty<Pair>(FXCollections.observableArrayList(
				new ArrayList<Pair<String, Integer>>()));
		
		//Initialise the corresponding UI elements
        localScoresUI = new ScoresUI(localScores, "Your Scores");
        onlineScoresUI = new ScoresUI(onlineScores, "Online Scores");
		
        //Update UI on each score change
		localScores.addListener(new ListChangeListener<Pair>() {

			@Override
			public void onChanged(Change<? extends Pair> c) {
				localScoresUI.loadScores();
			}
			
		});
		
		onlineScores.addListener(new ListChangeListener<Pair>() {

			@Override
			public void onChanged(Change<? extends Pair> c) {
				onlineScoresUI.loadScores();
			}
			
		});

	}
	
	/**
	 * Constructor for multiplayer game
	 * @param gameWindow The game window
	 * @param mpGame The multiplayer game logic
	 */
	public ScoresScene(GameWindow gameWindow, MultiplayerGame mpGame) {
		super(gameWindow);
		this.mpGame = mpGame;
		this.communicator = gameWindow.getCommunicator();
		Multimedia.playMusic("end.wav");
		
		//Initialise leaderboard and online scores
		boardScores = new SimpleListProperty<String>(FXCollections.observableArrayList(
				new ArrayList<String>()));
		onlineScores = new SimpleListProperty<Pair>(FXCollections.observableArrayList(
				new ArrayList<Pair<String, Integer>>()));
		
		//Initialise the corresponding UI elements
        localScoresUI = new ScoresUI(boardScores, "Leaderboard");
        onlineScoresUI = new ScoresUI(onlineScores, "Online Scores");
		
        //Update UI on score change
		boardScores.addListener(new ListChangeListener<String>() {

			@Override
			public void onChanged(Change<? extends String> c) {
				localScoresUI.loadLeaderboard();
			}
			
		});
		
		onlineScores.addListener(new ListChangeListener<Pair>() {

			@Override
			public void onChanged(Change<? extends Pair> c) {
				onlineScoresUI.loadScores();
			}
			
		});

	}
	
	/**
	 * Method to initialise communicator listener and send protocols
	 */
	@Override
	public void initialise() {

		//Add listener
		communicator.addListener((message) -> {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					if (message.startsWith("HISCORES")) {
						loadOnlineScores(message);
					} else if (message.startsWith("SCORES ")) {
						loadMultiplayerScores(message);
					}
				}
			});
		});
		 
		//Go back to main menu when pressing ESC
		gameWindow.getScene().setOnKeyPressed((key) -> {
			if (key.getCode().equals(KeyCode.ESCAPE)) { gameWindow.startMenu(); }
		});
		
		//Send HISCORES for both single and multiplayer, send SCORES only on multiplayer
		communicator.send("HISCORES");
		if (game instanceof MultiplayerGame) communicator.send("SCORES");
		else loadLocalScores();
	}

	/**
	 * Build the layout
	 */
	@Override
	public void build() {
		
		//The root of the scene graph
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        
        //Background pane
        scorePane = new StackPane();
        scorePane.setMaxWidth(gameWindow.getWidth());
        scorePane.setMaxHeight(gameWindow.getHeight());
        scorePane.getStyleClass().add("scores-background");
        root.getChildren().add(scorePane);
        
        //Borderpane that holds the layout
        borderPane = new BorderPane();
        scorePane.getChildren().add(borderPane);
        
        //Set scores invisible in case we get prompted to add a new score first
        localScoresUI.setVisible(false);
        onlineScoresUI.setVisible(false);
        
        //Set layout of scores
        borderPane.setLeft(localScoresUI);
        borderPane.setRight(onlineScoresUI);
        
        BorderPane.setMargin(localScoresUI, new Insets(10, 0, 0, 100));
        BorderPane.setMargin(onlineScoresUI, new Insets(10, 100, 0, 0));
	}
	
	/**
	 * Load scores on the local machine
	 */
	private void loadLocalScores() {
		File file = new File("localScores.txt");
		
		//If file doesn't exist, get default high scores
		if (!file.isFile()) writeDefaultScores();
		
		//Read from file and add the scores to the scores list
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String temp;
			
			while ((temp = reader.readLine()) != null) {
				String name = temp.split(":")[0];
				Integer score = Integer.parseInt(temp.split(":")[1]);
				localScores.add(new Pair<String, Integer>(name, score));
			}
			
			reader.close();
		} catch (Exception e) {
			logger.info("File not found");
		}
		
		//Sort the array highest - lowest and check whether we need to add a new score
		localScores.sort(new SortArray());
		addScore();
	}
	
	/**
	 * Load scores from the communicator
	 * @param scores The scores that we receive from the communicator
	 */
	private void loadOnlineScores(String scores) {
		//HISCORES <Name>:<Score>\n...
		logger.info("Reading online scores");
		
		//Remove the protocol from the string and split it
		scores = scores.replaceAll("HISCORES ", "");
		String[] scoresList = scores.split("\\n");
		
		//Add each score to the list
		for (var score : scoresList) {
			String name = score.split(":")[0];
			Integer value = Integer.parseInt(score.split(":")[1]);

			onlineScores.add(new Pair<String, Integer>(name, value));
		}
		
		//Sort the list and check whether we need to add a new score
		onlineScores.sort(new SortArray());
		addScore();
	}
	
	/**
	 * Load scores from the multiplayer game
	 * @param scores The scores from the multiplayer game
	 */
	private void loadMultiplayerScores(String scores) {
		//SCORES <Name>:<Score>:<Lives|DEAD>\n...
		logger.info("Reading leaderboard");
				
		//Remove protocol and split
		scores = scores.replaceAll("SCORES ", "");
		String[] scoresList = scores.split("\\n");
				
		//Add each score to the list
		for (var score : scoresList) {
			boardScores.add(score);
		}
		
		//Sort the list
		boardScores.sort(new SortLeaderboard());
	}
	
	/**
	 * Write scores on the local machine
	 */
	private void writeLocalScores() {
		File file = new File("localScores.txt");
				
		//Write the scores from the score list to the file, removing the previous data
		try {
			Writer out = new FileWriter(file, false);
					
			for (int i = 0; i < localScores.size() && i < 10; i++) {
				out.write(localScores.get(i).getKey() + ":" + localScores.get(i).getValue() + "\n");
			}
					
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write default scores to the local machine
	 */
	private void writeDefaultScores() {
		File file = new File("localScores.txt");
		
		try {
			Writer out = new FileWriter(file, true);
			
			for (int i = 1000; i <= 10000; i+=1000) {
				out.write("test:" + i + "\n");
			}
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Show textfield and a button to add a new highscore
	 */
	private void addScore() {
		var button = new Button("Submit");
		button.setVisible(false);
		
		//If either of the scores are not initialised, we are in a multiplayer game and we don't need to add
		if (localScores == null || onlineScores == null) {
			localScoresUI.setVisible(true);
			onlineScoresUI.setVisible(true);
			return;
		}
		
		//Check if the index is valid and the score is higher than the lowest recorded
		if (!scoreAdded && ((localScores.size()-1 >= 0 && 
				game.getScore().get() > (int) localScores.get(localScores.size()-1).getValue()) || 
				(onlineScores.size()-1 >= 0 && 
				game.getScore().get() > (int) onlineScores.get(onlineScores.size()-1).getValue())))
		{
			//We have been prompted now
			scoreAdded = true;
			
			//Set the scores invisible in case they weren't already
			localScoresUI.setVisible(false);
			onlineScoresUI.setVisible(false);
				
			//Set the simple layout
			var inputPane = new VBox();
			var textfield = new TextField();
			button.setVisible(true);
			inputPane.getChildren().addAll(textfield, button);
			scorePane.getChildren().add(inputPane);
				
			//When we press submit, hide everything, add the score and rerender the UI elements
			button.setOnAction((e) -> {

				
				Pair input = new Pair<String, Integer>(textfield.getText(), game.getScore().get());
				localScores.add(input);
				onlineScores.add(input);
					
				//Sort the arrays once we've entered the new score
				localScores.sort(new SortArray());
				onlineScores.sort(new SortArray());
					
				//Write scores on the local machine and send a new highscore to the server
				writeLocalScores();
				communicator.send("HISCORE " + textfield.getText() + ":" + game.getScore().get());
					
				//Set the score submission layout invisible
				inputPane.setVisible(false);
				textfield.setVisible(false);
				button.setVisible(false);
					
				//Set the UI elements visible
				localScoresUI.setVisible(true);
				onlineScoresUI.setVisible(true);
					
				Multimedia.playAudio("pling.wav");
				
			});
		} else {
			if (!button.isVisible()) {
				localScoresUI.setVisible(true);
				onlineScoresUI.setVisible(true);
			}
		}	
		
	}
		
	/**
	 * Sort local scores and online scores
	 */
	class SortArray implements Comparator<Pair> {

		@Override
		public int compare(Pair o1, Pair o2) {
			return (int) o2.getValue() - (int) o1.getValue();
		}
		
	}
	
	/**
	 * Sort leaderboard scores
	 */
	class SortLeaderboard implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			return Integer.parseInt(o2.split(":")[1]) - Integer.parseInt(o1.split(":")[1]);
		}
		
	}
}
