package uk.ac.soton.comp1206.utility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * This class handles the audio for the game
 *
 */
public class Multimedia {

    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    /**
     * The audio player
     */
	public static MediaPlayer audioPlayer;
	
	/**
	 * The music player
	 */
	public static MediaPlayer musicPlayer;
	
	/**
	 * Play audio from a file
	 */
	public static void playAudio(String file) {

        String toPlay = Multimedia.class.getResource("/sounds/" + file).toExternalForm();
        logger.info("Playing audio: " + toPlay);

        try {
            Media play = new Media(toPlay);
            audioPlayer = new MediaPlayer(play);
            audioPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to play audio file");
        }
	}
	
	/**
	 * Play music from a file
	 */
	public static void playMusic(String file) {
		try {
			Media play = new Media(Multimedia.class.getResource("/music/" + file).toExternalForm());
			musicPlayer = new MediaPlayer(play);
			musicPlayer.setAutoPlay(true);
			musicPlayer.play();
		} catch (Exception e) {
			logger.info("Couldn't play music");
			e.printStackTrace();
		}
	}
}
