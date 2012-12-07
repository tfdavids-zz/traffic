import java.util.Collections;
import java.util.Vector;


public class Light {

	public enum LightState {
		RED, YELLOW, GREEN;
	}

	public enum LightMethod {
		CONSTANT, REGRESSION, RANDOM;
	}
	
	public static LightMethod method;
	
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

		if (weights.size() == 0) {
			for (int i = 0; i < NUM_FEATURES; i++) {
				weights.add(0.0);
			}
		}
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

	public void step() {

		if (method == LightMethod.CONSTANT) {
			fixedLengthStep();
		} else if (method == LightMethod.REGRESSION) {
			regressionStep();
		} else if (method == LightMethod.RANDOM) {
			randomLengthStep();
		}

		/* TODO: Improve this method.
		 * This method is the most important part of the project -- it is
		 * where the light decides whether or not it changes colors. For now,
		 * the light just uses a simple counter, and changes every 15 steps.
		 */

	}

	private void fixedLengthStep() {
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
	}

	private void randomLengthStep() {

	}

	//// code for linear regression \\\\

	private static Vector<Double> weights = new Vector<Double>();
	private Vector<Vector<Double>> inputs = new Vector<Vector<Double>>();

	private static final int LOOKAHEAD = 10;
	private static final int YELLOW_LIGHT = 5;
	private static final int MIN_GREEN_LIGHT = 5;
	private static final int NUM_FEATURES = 7;

	private static final double ALPHA = 0.01;

	private int yellowLightCounter = -1;
	private int greenLightCounter = -1;

	/**
	 * @return A vector with the following features (in order):
	 * - average vehicle speed in the green light direction
	 * - average squared vehicle speed in the green light direction
	 * - number of vehicles in the green light direction
	 * - number of vehicles in the red light direction
	 * - average squared vehicle speed in the red light direction
	 * - average vehicle speed in the red light direction
	 */
	private Vector<Double> getCurrentState() {
		Vector<Double> state = new Vector<Double>();

		state.add(city.getAverageSpeed(this, this.getVerticalStreet()));
		state.add(city.getAverageSquaredSpeed(this, this.getVerticalStreet()));
		state.add(city.getNumCars(this, this.getVerticalStreet()));
		state.add(city.getNumCars(this, this.getHorizontalStreet()));
		state.add(city.getAverageSquaredSpeed(this, this.getHorizontalStreet()));
		state.add(city.getAverageSpeed(this, this.getHorizontalStreet()));

		if (this.verticalState == LightState.GREEN) {
			// do nothing
		} else if (this.horizontalState == LightState.GREEN) {
			Collections.reverse(state);
		} else {
			System.err.println("Error: taking features from a light in transition");
		}

		return state;
	}

	private void regressionStep() {
		if (yellowLightCounter >= 0) {
			yellowLightCounter++;

			if ((float)yellowLightCounter * City.TIME_STEP >= YELLOW_LIGHT) {
				if (this.verticalState == LightState.YELLOW) {
					this.verticalState = LightState.RED;
					this.horizontalState = LightState.GREEN; // eventually, some delay between red lights?
					greenLightCounter = 0;
				} else if (this.horizontalState == LightState.YELLOW) {
					this.horizontalState = LightState.RED;
					this.verticalState = LightState.GREEN; // eventually, some delay between red lights?
					greenLightCounter = 0;
				}

				yellowLightCounter = -1; // this will freeze it until we decide to switch
			}
		} else if (greenLightCounter >= 0) {
			greenLightCounter++;
			if ((float)greenLightCounter * City.TIME_STEP >= MIN_GREEN_LIGHT) {
				greenLightCounter = -1;
			}
		} else {
			double didSwitch;

			if (inputs.size() < LOOKAHEAD) {
				didSwitch = 0.0;
				Vector<Double> input = getCurrentState();
				input.add(didSwitch);
				inputs.add(input);
			} else {
				Vector<Double> constantState = getCurrentState();
				constantState.add(0.0);
				double constantStateScore = 0.0;
				for (int i = 0; i < weights.size(); i++) {
					constantStateScore += weights.elementAt(i) * constantState.elementAt(i);
				}
				
				Vector<Double> switchedState = getCurrentState();
				Collections.reverse(switchedState);
				switchedState.add(1.0);
				double switchedStateScore = 0.0;
				for (int i = 0; i < weights.size(); i++) {
					switchedStateScore += weights.elementAt(i) * switchedState.elementAt(i);
				}

				// get the last input and score it
				Vector<Double> oldInputs = inputs.elementAt(0); inputs.removeElementAt(0);
				double currentStateCost = city.getAverageAverageSpeed();
				double predictedCost = 0.0;
				for (int i = 0; i < weights.size(); i++) {
					predictedCost += weights.elementAt(i) * oldInputs.elementAt(i);
				}
				for (int i = 0; i < weights.size(); i++) {
					double newValue = weights.elementAt(i) + ALPHA * (currentStateCost - predictedCost) * oldInputs.elementAt(i);
					if (Double.isInfinite(newValue)) {
						System.err.println("Error: infinite weights");
					}
					weights.setElementAt(newValue, i);
				}

				didSwitch = constantStateScore >= switchedStateScore ? 0.0 : 1.0;
				Vector<Double> currentState = getCurrentState();
				if (didSwitch == 1.0)
					Collections.reverse(currentState);
				currentState.add(didSwitch);
				inputs.add(currentState);			
			}

			if (didSwitch == 1.0) {
				yellowLightCounter = 0;

				if (this.verticalState == LightState.GREEN) {
					this.verticalState = LightState.YELLOW;
				} else if (this.horizontalState == LightState.GREEN) {
					this.horizontalState = LightState.YELLOW;
				}
			}
		}
	}

	//// END OF THOMAS' CODE \\\\


}
