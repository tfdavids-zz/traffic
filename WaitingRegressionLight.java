import java.util.*;

public class WaitingRegressionLight extends Light {
    public WaitingRegressionLight(City city, int verticalStreet, int horizontalStreet) {
        super(city, verticalStreet, horizontalStreet);

		if (weights.size() == 0) {
			for (int i = 0; i < NUM_FEATURES; i++) {
				weights.add(0.0);
			}
		}
    }

	private static Vector<Double> weights = new Vector<Double>();
	private Vector<Vector<Double>> inputs = new Vector<Vector<Double>>();

	private static final int LOOKAHEAD = 20;
	private static final int YELLOW_LIGHT = 5;
	private static final int MIN_GREEN_LIGHT = 2;
	private static final int NUM_FEATURES = 3;

	private static final double REG_ALPHA = 0.0001;
	private static final double FVI_ALPHA = 0.1;

	private int yellowLightCounter = -1;
	private int greenLightCounter = -1;

	/**
	 * @return A vector with the following features (in order):
     * - number of waiting cars in the green direction
     * - number of waiting cars in the red direction
	 */
	private Vector<Double> getCurrentState() {
		Vector<Double> state = new Vector<Double>();

        state.add(city.getNumWaitingCars(this, this.getVerticalStreet()));
        state.add(city.getNumWaitingCars(this, this.getHorizontalStreet()));

		if (this.verticalState == LightState.GREEN) {
			// do nothing
		} else if (this.horizontalState == LightState.GREEN) {
			Collections.reverse(state);
		} else {
			System.err.println("Error: taking features from a light in transition");
		}

		return state;
	}

	@Override protected void step() {
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
			double didSwitch = 0.0;
/*
			if (inputs.size() < LOOKAHEAD * City.HORIZONTAL_STREETS * City.VERTICAL_STREETS) {
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
					double newValue = weights.elementAt(i) + REG_ALPHA * (currentStateCost - predictedCost) * oldInputs.elementAt(i);
					if (Double.isInfinite(newValue)) {
						System.err.println("Error: infinite weights");
					}
					weights.setElementAt(newValue, i);
				}
				//if (!Double.isInfinite(weights.elementAt(0)) && !Double.isNaN(weights.elementAt(0)))
					//System.err.println("Weights = " + weights);

				didSwitch = constantStateScore >= switchedStateScore ? 0.0 : 1.0;
				Vector<Double> currentState = getCurrentState();
				if (didSwitch == 1.0)
					Collections.reverse(currentState);
				currentState.add(didSwitch);
				inputs.add(currentState);			
			}
*/

            if (this.verticalState == LightState.GREEN && city.getNumWaitingCars(this, this.getHorizontalStreet()) >= 5) {
                didSwitch = 1.0;
            } else if (this.horizontalState == LightState.GREEN && city.getNumWaitingCars(this, this.getVerticalStreet()) >= 5) {
                didSwitch = 1.0;
            }

			if (didSwitch == 1.0) {
				yellowLightCounter = 0;

				if (this.verticalState == LightState.GREEN) {
					this.verticalState = LightState.YELLOW;
				} else if (this.horizontalState == LightState.GREEN) {
					this.horizontalState = LightState.YELLOW;
				}
			}

            // System.err.println(city.getNumWaitingCars(this, this.getVerticalStreet()));
		}
	}

	//// END OF THOMAS' CODE \\\\

}
