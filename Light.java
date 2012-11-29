
public class Light {

	public enum LightState {
		RED, YELLOW, GREEN;
	}
	
	private LightState verticalState; 
	private LightState horizontalState;
	
	private int counter;
	
	private final int verticalStreet;
	private final int horizontalStreet;
	
	private City city;
	
	public Light(City city, int verticalStreet, int horizontalStreet) {
		this.verticalState = LightState.GREEN;
		this.horizontalState = LightState.RED;
		this.counter = 0;
		this.city = city;
		
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
	
	public double getPosition(int street) {
		if (street == verticalStreet) {
			return City.VERTICAL_STREET_LENGTH * ((((float)horizontalStreet) / 2 + 1) / (this.city.HORIZONTAL_STREETS + 1));
		} else if (street == horizontalStreet) {
			return City.HORIZONTAL_STREET_LENGTH * ((((float)verticalStreet - 1) / 2 + 1) / (this.city.VERTICAL_STREETS + 1));
		} else {
			System.err.println("Error: bad light access (1)");
			return 0;
		}
	}
	
	public LightState getState(int street) {
		if (street == this.horizontalStreet)
			return this.horizontalState;
		else if (street == this.verticalStreet)
			return this.verticalState;
		else {
			System.err.println("Error: bad light access (2)");
			return LightState.RED;
		}
	}
	
	public void step() {
		
		counter++;
		
		if ((float)counter * City.TIME_STEP == 15) {
			if (this.verticalState == LightState.GREEN) {
				this.verticalState = LightState.YELLOW;
			} else if (this.horizontalState == LightState.GREEN) {
				this.horizontalState = LightState.YELLOW;
			}
		} else if ((float)counter * City.TIME_STEP == 20) {
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
