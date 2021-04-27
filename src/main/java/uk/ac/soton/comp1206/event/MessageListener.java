package uk.ac.soton.comp1206.event;

/**
 * Listens to new messages from the communicator
 *
 */
public interface MessageListener {

	/**
	 * Relay the message from the communicator
	 * @param message The message
	 */
	public void receiveMessage(String message);
}
