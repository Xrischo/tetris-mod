package uk.ac.soton.comp1206.event;

/**
 * Listens to the timer being set
 */
public interface GameTimerListener {

	/**
	 * Updated the timer with the given period
	 * @param period The period
	 */
	public void updateTimer(long period);
}
