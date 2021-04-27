package uk.ac.soton.comp1206.ui;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

/**
 * This class wraps the scores in a VBox UI element and animates them
 */
public class ScoresUI extends VBox {
	
    private static final Logger logger = LogManager.getLogger(ScoresUI.class);
    
    /**
     * List of scores as Text variables
     */
    private ArrayList<Text> scoresList = new ArrayList<Text>();
    
    /**
     * The current index of the score we are animating
     */
    private int currentIndex;
    
    /**
     * The timer for the animation
     */
    private Timer timer = new Timer();
    
    /**
     * The task that needs to be done after the timer
     */
	private TimerTask timertask;
	
	/**
	 * The name of the scores we are displaying - Leaderboard, online scores etc.
	 */
	private String nameOfScores;
	
	/**
	 * The list property that binds to the scene's scores
	 */
	SimpleListProperty scores;

	/**
	 * Constructor for the UI to initialise the scores list and their name
	 * @param scores The list from the scene that we are binding to
	 * @param nameOfScores The name of the scores list
	 */
	public ScoresUI(SimpleListProperty scores, String nameOfScores) {
		this.scores = new SimpleListProperty();
		this.scores.bind(scores);
		
		this.nameOfScores = nameOfScores;
	}

	/**
	 * Load scores by getting them out of the list property and adding them to the arraylist as texts
	 */
	public void loadScores() {
		//Remove all previous children to rerender them
		getChildren().remove(0, getChildren().size());
		scoresList.clear();
		
		var scoreName = new Text(nameOfScores);
		scoreName.getStyleClass().add("heading");
		
		//Add the name of the scores first
		getChildren().add(scoreName);
		VBox.setMargin(scoreName, new Insets(10, 0, 5, 20));
		
		//Make a text from each score's name and value with style and colour
		for (var i = 0; i < scores.getSize() && i < 10; i++) {
			
			//Take the next score out of the list property
			Pair temp = (Pair) scores.get(i);
			
			//Get the score as a text and add css
			var name = new Text((String) temp.getKey() + ": " + ((Integer) temp.getValue()).toString());
			name.getStyleClass().add("scorelist");
			
			//Change the colour of the node on each loop
			double offset = (double) i / 10;
			Color colour = new Color(1 - offset, offset, offset/2, 1);
			name.setFill(colour);
			
			getChildren().add(name);
			
			//Set opacity to 0 to animate it later
			name.setOpacity(0);
			VBox.setMargin(name, new Insets(0, 50, 10, 20));
			
			scoresList.add(name);
		}
		
		//Animate
		currentIndex = 0;
		reveal();
	}
	
	/**
	 * Load the leaderboard when we are in a multiplayer game
	 */
	public void loadLeaderboard() {
		logger.info("Loading leaderboard");
		
		//Remove children to rerender them
		getChildren().remove(0, getChildren().size());
		scoresList.clear();
		
		var scoreName = new Text(nameOfScores);
		scoreName.getStyleClass().add("heading");
		
		getChildren().add(scoreName);
		VBox.setMargin(scoreName, new Insets(10, 0, 5, 20));
		
		//Get each score out of the list and add to the arraylist 
		for (var i = 0; i < scores.getSize(); i++) {
			String temp = (String) scores.get(i);
			
			//The score name: value, strike it through if the player is dead
			var score = new Text(temp.split(":")[0] + ": " + temp.split(":")[1]);
			if (temp.split(":")[2].equals("DEAD")) score.setStrikethrough(true);
			score.getStyleClass().add("scorelist");
			
			//Change the colour on every loop
			double offset = (double) i / 10;
			Color colour = new Color(1 - offset, offset, offset/2, 1);
			score.setFill(colour);
			
			getChildren().add(score);
			
			//Set opacity to 0 to animate it
			score.setOpacity(0);
			VBox.setMargin(score, new Insets(0, 50, 10, 20));
			
			scoresList.add(score);
		}
		
		//Animate
		currentIndex = 0;
		reveal();
	}
	
	/**
	 * Get the scores list
	 * @return scores
	 */
	public SimpleListProperty getScores() { return scores; }
	
	/**
	 * Animating the scores
	 */
	public void reveal() {
		Timeline timeline = new Timeline();
		
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(200), 
				new KeyValue(scoresList.get(currentIndex).opacityProperty(), 1)));
		timeline.play();
		
		//After every animation, wait a bit to reveal the next score
		currentIndex++;
		setTimer();
	}
	
	/**
	 * Set the timer to reveal the next element
	 */
	private void setTimer() {
		if (currentIndex < scoresList.size()) {
			timer.cancel();
			timer = new Timer();
			
			timertask = new TimerTask() {

				@Override
				public void run() {
					reveal();
				}
				
			};
			
			timer.schedule(timertask, 200);
		}
	}

}
