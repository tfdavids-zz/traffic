import java.util.Vector;


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

		if (true) {
			fixedLengthStep();
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

	//// THOMAS' CODE \\\\

	private static Vector<Double> weights;
	private Vector<Vector<Double>> inputs;

	private static final int LOOKAHEAD = 50;
	private static final int YELLOW_LIGHT = 5;

	
	/**
	 * @return A vector with the following features (in order):
	 * - average vehicle speed in the green light direction
	 * - average vehicle speed in the red light direction
	 * - average squared vehicle speed in the green light direction
	 * - average squared vehicle speed in the red light direction
	 * - number of vehicles in the green light direction
	 * - number of vehicles in the red light direction
	 */
	private Vector<Double> getCurrentState() {
		Vector<Double> state = new Vector<Double>();
		
		if (this.verticalState == LightState.GREEN) {
			state.add(city.getAverageSpeed(this, this.getVerticalStreet()));
			state.add(city.getAverageSpeed(this, this.getHorizontalStreet()));
			state.add(city.getAverageSquaredSpeed(this, this.getVerticalStreet()));
			state.add(city.getAverageSquaredSpeed(this, this.getHorizontalStreet()));
			state.add(city.getNumCars(this, this.getVerticalStreet()));
			state.add(city.getNumCars(this, this.getHorizontalStreet()));
		} else if (this.horizontalState == LightState.GREEN) {
			state.add(city.getAverageSpeed(this, this.getHorizontalStreet()));
			state.add(city.getAverageSpeed(this, this.getVerticalStreet()));
			state.add(city.getAverageSquaredSpeed(this, this.getHorizontalStreet()));
			state.add(city.getAverageSquaredSpeed(this, this.getVerticalStreet()));
			state.add(city.getNumCars(this, this.getHorizontalStreet()));
			state.add(city.getNumCars(this, this.getVerticalStreet()));
		} else {
			System.err.println("Error: taking features from a light in transition");
		}
		
		return state;
	}

	private void logisticRegressionStep() {

		if (counter >= 0) {
			counter++;

			if (counter >= YELLOW_LIGHT) {
				if (this.verticalState == LightState.YELLOW) {
					this.verticalState = LightState.RED;
					this.horizontalState = LightState.GREEN; // eventually, some delay between red lights?
				} else if (this.horizontalState == LightState.YELLOW) {
					this.horizontalState = LightState.RED;
					this.verticalState = LightState.GREEN; // eventually, some delay between red lights?
				}

				counter = -1; // this will freeze it until we decide to switch
			}
		} else {
			double didSwitch;

			if (inputs.size() < LOOKAHEAD) {
				didSwitch = -1.0; // TODO: use some heuristic to decide whether to switch here
				Vector<Double> input = getCurrentState();
				input.add(didSwitch);
				inputs.add(input);
			} else {
				Vector<Double> currentState = getCurrentState();
				double currentStateScore = 0.0;
				for (int i = 0; i < weights.size(); i++) {
					currentStateScore += weights.elementAt(i) * currentState.elementAt(i);
				}

				// get the last input and score it
				Vector<Double> oldInputs = inputs.elementAt(0); inputs.removeElementAt(0);
				double currentStateCost = city.getAverageAverageSpeed();
				// TODO: update weights based on oldInputs and currentStateCost

				didSwitch = currentStateScore > 0 ? 1.0 : -1.0;
				currentState.add(didSwitch);
				inputs.add(currentState);			
			}

			if (didSwitch == 1.0) {
				counter = 0;

				if (this.verticalState == LightState.GREEN) {
					this.verticalState = LightState.YELLOW;
				} else if (this.horizontalState == LightState.GREEN) {
					this.horizontalState = LightState.YELLOW;
				}
			}
		}

		// features: average vert & horiz vehicle speed, average vert & horiz squared vehicle speed, vert & horiz num vehicles
		// current state: a vector of these features
		// weights: a vector with this length where each component corresponds to these features
		// current state score: average average speed of every vehicle on the grid
		// 

		// if vector of priors is totally full

		// currentState = this.getCurrentState()
		// currentStateScore = this.getCurrentStateScore()
		// input = vectorOfInputs.removeFirstElement()
		// this.updateWeights(input, currentStateScore)


		// decision = makeDecision(weights, currentState)
		// vectorOfInputs.append(currentState, decision)

		// if vector of priors is not full:

		// just add the current state to the end of it
	}

	//// END OF THOMAS' CODE \\\\


}
