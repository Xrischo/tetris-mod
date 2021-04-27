package uk.ac.soton.comp1206.scene;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * This scene is responsible for the lobby - making a lobby, joining one, chat and start a game
 *
 */
public class LobbyScene extends BaseScene {
	
    private static final Logger logger = LogManager.getLogger(LobbyScene.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * The timer to ask the communicator for list of lobbies
     */
    private final Timer timer = new Timer();
    
    /**
     * List of lobbies
     */
    private VBox lobbiesList;
    
    /**
     * The chat of a lobby
     */
    private BorderPane lobbyChat;
    
    /**
     * The messages in a lobby
     */
    private VBox messages;
    
    /**
     * The player list in a lobby
     */
    private HBox playersList;
    
    /**
     * The button to host a game
     */
    private Button lobbyHostButton;
    
    /**
     * The communicator
     */
    private Communicator communicator;
	
    
    /**
     * Constructor to initialise the timer
     * @param gameWindow The window that this scene is in
     */
	public LobbyScene(GameWindow gameWindow) {
		super(gameWindow);
		this.communicator = gameWindow.getCommunicator();
		
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				communicator.send("LIST");
			}
			
		};
		timer.schedule(task, 0, 5000);
	}

	/**
	 * Initialise what happens on each communicator's received message, and closing the window
	 */
	@Override
	public void initialise() {
		
		//Add listener to the communicator
		communicator.addListener((message) -> {
			if (!(message.equals("Ping? Pong!"))) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						if (message.startsWith("CHANNELS")) {
							updateChannels(message.replace("CHANNELS ", ""));
						} else if (message.startsWith("JOIN")) {
							joinChannel(message.replace("JOIN ", ""));
						} else if (message.startsWith("HOST")) {
							lobbyHostButton.setVisible(true);
						} else if (message.startsWith("PARTED")) { 
							lobbyChat.setVisible(false);
						} else if (message.startsWith("ERROR")) {
							Alert error = new Alert(Alert.AlertType.ERROR, message.replace("ERROR", ""));
				            error.showAndWait();
						} else if (message.startsWith("NICK")) {
							communicator.send("USERS");
						} else if (message.startsWith("START")) {
							Multimedia.musicPlayer.stop();
							timer.cancel();
							gameWindow.startMultiplayer();
						} else if (message.startsWith("USERS")) {
							showUsers(message.replace("USERS ", ""));
						} else if (message.startsWith("MSG")) {
							String text = message.replace(message.split(":")[0] + ":", "");
							
							if (text.startsWith("/nick ")) {
								communicator.send("NICK " + text.replace("/nick ", ""));
							} else {
							LocalDateTime date = LocalDateTime.now();
							messages.getChildren().add(
									new Text("[" + date.format(formatter) + "] <" 
											+ message.replace("MSG ", "").split(":")[0] + "> " + text));
							}
						}
					}
					
				});
			}
		});
		
		gameWindow.getScene().setOnKeyPressed((key) -> {
			if (key.getCode().equals(KeyCode.ESCAPE)) {
				logger.info("Closing lobby");
				gameWindow.startMenu();
			}
		});
		
		//Ask for list of lobbies
		communicator.send("LIST");
	}

	/**
	 * Build the layout of the scene
	 */
	@Override
	public void build() {
		logger.info("Building " + this.getClass().getName());

		//Set root
	    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

	    //Background pane
	    var backgroundPane = new StackPane();
	    backgroundPane.setMaxWidth(gameWindow.getWidth());
	    backgroundPane.setMaxHeight(gameWindow.getHeight());
	    backgroundPane.getStyleClass().add("menu-background");
	    root.getChildren().add(backgroundPane);

	    //Hold everything in a borderpane
	    var borderPane = new BorderPane();
	    backgroundPane.getChildren().add(borderPane);
	    
	    //Left layout nodes
	    var leftLayout = new VBox();
	    var currentGamesText = new Text("Current Games");
	    var hostButton = new Text("Host Game");
	    var createTextField = new TextField();
	    lobbiesList = new VBox();
	    
	    //Sort left layout
	    borderPane.setLeft(leftLayout);
	    leftLayout.getChildren().addAll(currentGamesText, hostButton, createTextField, lobbiesList);
	    currentGamesText.getStyleClass().add("heading");
	    hostButton.getStyleClass().add("heading");
	    createTextField.getStyleClass().add("TextField");
	    
	    createTextField.setVisible(false);
	    createTextField.setOnKeyPressed((event) -> {
	    	if (event.getCode().equals(KeyCode.ENTER)) {
	    		communicator.send("CREATE " + createTextField.getText());
	    		createTextField.setText(null);
	    		createTextField.setVisible(false);
	    		
	    		Multimedia.playAudio("rotate.wav");
	    	}
	    });
	    hostButton.setOnMouseClicked((e) -> { createTextField.setVisible(!createTextField.isVisible()); Multimedia.playAudio("rotate.wav"); });
        hostButton.setOnMouseEntered((e) -> { hostButton.setFill(Color.YELLOW); });
        hostButton.setOnMouseExited((e) -> { hostButton.setFill(Color.WHITE); });
	    
	    //Title
	    var title = new Text("Multiplayer");
	    title.getStyleClass().add("title");
	    borderPane.setTop(title);
	    
	    //Lobby chat nodes (on the right)
	    lobbyChat = new BorderPane();
	    var scrollChat = new ScrollPane();
	    messages = new VBox();
	    playersList = new HBox();
	    var interactPane = new BorderPane();
	    var lobbyTextField = new TextField();
	    lobbyHostButton = new Button("Start");
	    var leaveButton = new Button("Leave");
	    
	    //Sort lobby chat nodes
	    lobbyChat.setCenter(scrollChat);
	    lobbyChat.setTop(playersList);
	    lobbyChat.setBottom(interactPane);
	    scrollChat.setContent(messages);
	    interactPane.setTop(lobbyTextField);
	    interactPane.setLeft(lobbyHostButton);
	    interactPane.setRight(leaveButton);
	    
	    scrollChat.setFitToWidth(true);
	    
		lobbyTextField.setPromptText("Send Message");
	    leaveButton.setOnAction((e) -> { communicator.send("PART"); Multimedia.playAudio("rotate.wav"); });
	    lobbyHostButton.setOnAction((e) -> { communicator.send("START"); });
	    lobbyHostButton.setVisible(false);
	    
	    lobbyTextField.setOnKeyPressed((event) -> {
	    	if (event.getCode().equals(KeyCode.ENTER)) {
	    		communicator.send("MSG " + lobbyTextField.getText());
	    		lobbyTextField.setText(null);
	    		
	    		Multimedia.playAudio("message.wav");
	    	}
	    });
	    
	    lobbyChat.setPrefWidth(gameWindow.getWidth()/2);
	    lobbyChat.setMaxHeight(gameWindow.getHeight()/2);
	    lobbyChat.getStyleClass().add("gameBox");
	    scrollChat.getStyleClass().add("scroller");
	    messages.getStyleClass().add("messages");
	    lobbyTextField.getStyleClass().add("TextField");
	    playersList.getStyleClass().add("playerBox");
	 
	    borderPane.setRight(lobbyChat);
	    
	    //Set the chat invisible until we join a lobby
	    lobbyChat.setVisible(false);
	}

	/**
	 * Update the list of channels
	 * @param lobbies The lobbies that exist
	 */
	private void updateChannels(String lobbies) {
		logger.info("Updating channels");
		
		//Split to get each lobby, remove the previous list to render it again
		String[] lobbyList = lobbies.split("\\n");
		lobbiesList.getChildren().remove(0, lobbiesList.getChildren().size());
		
		//Add each lobby and handle join requests and styling
		for (var lobby : lobbyList) {
			var channel = new Text(lobby);
			lobbiesList.getChildren().add(channel);
			
			channel.getStyleClass().add("channelItem");
			channel.setOnMouseClicked((e) -> { communicator.send("JOIN " + lobby); Multimedia.playAudio("rotate.wav"); });
			channel.setOnMouseEntered((e) -> { channel.setFill(Color.YELLOW); });
			channel.setOnMouseExited((e) -> { channel.setFill(Color.WHITE); });
			
			VBox.setMargin(channel, new Insets(10, 0, 10, 0));
		}
	}
	
	
	/**
	 * Handle joining a channel
	 * @param channel The channel that we join
	 */
	private void joinChannel(String channel) {
		logger.info("Joining channel " + channel);
		
		//Remove the previous messages and players list
		messages.getChildren().clear();
		messages.getChildren().add(new Text("Welcome to the Lobby!\nType /nick to change your nickname. "));
		playersList.getChildren().clear();
		
		//We are not hosts yet
		lobbyHostButton.setVisible(false);
		
		//Get the players list
		communicator.send("USERS");
		
		//Set the lobby chat visible
		lobbyChat.setVisible(true);
		
		Multimedia.playAudio("lifegain.wav");
	}
	
	/**
	 * Show users in a channel
	 * @param users The users to show
	 */
	private void showUsers(String users) {
		playersList.getChildren().clear();
		String[] lobbyUsers = users.split(":");
		
		for (var user : lobbyUsers) {
			var nextUser = new Text(user);
			playersList.getChildren().add(nextUser);
			
			nextUser.getStyleClass().add("playerBox");
			HBox.setMargin(nextUser, new Insets(0, 0, 0, 0));
		}
	}
}
