
public class Light {

	public enum LightState {
		RED, YELLOW, GREEN;
	}
	
	private LightState verticalState; 
	private LightState horizontalState;
	
	private int counter;
	
	private final int verticalStreet;
	private final int horizontalStreet;
	
	public Light(int verticalStreet, int horizontalStreet) {
		this.verticalState = LightState.GREEN;
		this.horizontalState = LightState.RED;
		this.counter = 0;
		
		this.verticalStreet = verticalStreet;
		this.horizontalStreet = horizontalStreet;
	}
	
	// Use these methods so we can keep track of when the light is using this information --
	// in some cases, we won't want it to know its street
	private int getVerticalStreet() {
		return verticalStreet;
	}
	
	private int getHorizontalStreet() {
		return horizontalStreet;
	}
	
	public LightState getState(int street) {
		if (street % 2 == 0) return this.horizontalState;
		else return this.verticalState;
	}
	
	public void step() {
		
		counter++;
		
		if (counter == 12) {
			if (this.verticalState == LightState.GREEN) {
				this.verticalState = LightState.YELLOW;
			} else if (this.horizontalState == LightState.GREEN) {
				this.horizontalState = LightState.YELLOW;
			}
		} else if (counter == 15) {
			if (this.verticalState == LightState.YELLOW) {
				this.verticalState = LightState.RED;
				this.horizontalState = LightState.GREEN; // eventually, some delay between red lights?
			} else if (this.horizontalState == LightState.YELLOW) {
				this.horizontalState = LightState.RED;
				this.verticalState = LightState.GREEN; // eventually, some delay between red lights?
			}
			
			counter = 0;
		}
		
		/* TODO: Improve this method.
		 * This method is the most important part of the project -- it is
		 * where the light decides whether or not it changes colors. For now,
		 * the light just uses a simple counter, and changes every 15 steps.
		 */
		 
	}
}
