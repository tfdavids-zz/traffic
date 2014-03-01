import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

abstract class Light {

	public enum LightState {
		RED, YELLOW, GREEN;
	}

	public enum LightMethod {
		CONSTANT, REGRESSION, RANDOM, FVI, WEIGHTED; // FVI = fitted value iteration, WEIGHTED uses results of linear regression
	}

	public static LightMethod method;

	protected LightState verticalState; 
	protected LightState horizontalState;

	protected int counter;

	protected final int verticalStreet;
	protected final int horizontalStreet;

	protected City city;

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
	protected int getVerticalStreet() {
		return verticalStreet;
	}

	protected int getHorizontalStreet() {
		return horizontalStreet;
	}

	public double getPosition(int street) {
		if (street == verticalStreet) {
			return City.VERTICAL_STREET_LENGTH * ((((float)horizontalStreet) / 2 + 1) / (City.HORIZONTAL_STREETS + 1));
		} else if (street == horizontalStreet) {
			return City.HORIZONTAL_STREET_LENGTH * ((((float)verticalStreet - 1) / 2 + 1) / (City.VERTICAL_STREETS + 1));
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

	abstract protected void step();
}
