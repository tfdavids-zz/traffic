import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;


public class Light {

	public enum LightState {
		RED, YELLOW, GREEN;
	}

	public enum LightMethod {
		CONSTANT, REGRESSION, RANDOM, FVI, WEIGHTED; // FVI = fitted value iteration, WEIGHTED uses results of linear regression
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

		setupFVI();	

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
		} else if (method == LightMethod.FVI) {
			fittedValueIterationStep();
		} else if (method == LightMethod.WEIGHTED) {
			weightedStep();
		}

	}

	private boolean fixedLengthStep() {
		counter++;

		if ((float)counter * City.TIME_STEP == 15) {
			if (this.verticalState == LightState.GREEN) {
				this.verticalState = LightState.YELLOW;
			} else if (this.horizontalState == LightState.GREEN) {
				this.horizontalState = LightState.YELLOW;
			}

			return true;
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

		return false;
	}

	private void randomLengthStep() {

	}
	
	private void weightedStep() {
		// weights were calculated using results of linear regression
		weights = new Vector<Double>(Arrays.asList(0.3820357630506505, 0.08312630688930392, 0.03879866966691007, -0.009008062075713244, -0.013287962019461756, 0.04121574263074275, 0.08053497462507155));
		
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

			double didSwitch = constantStateScore >= switchedStateScore ? 0.0 : 1.0;

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

	//// code for fitted value iteration \\\\

	/*
	 * Order:
	 *  0. Constant 1.0 value.
	 *  1. Num cars on vertical street
	 *  2. Avg. speed of vertical steet
	 *  3. Sum of speeds of vertical street
	 *  4. Num cars on horizontal street
	 *  5. Avg. speed of horizontal street
	 *  6. Sum of speeds of horizontal street
	 *  7. Choice (0: stay, 1: switch)
	 *  8. Light state (#:v/h -- 0:red/green, 1:red/yellow, 2:green/red, 3:yellow/red) -- not run on regression
	 */
	private Vector<Double> getFVIState() {
		Vector<Double> state = new Vector<Double>();
		state.add(1.0);
		state.add(city.getNumCars(this, this.getVerticalStreet()));
		state.add(city.getAverageSpeed(this, this.getVerticalStreet()));
		state.add(state.get(1) * state.get(2));
		state.add(city.getNumCars(this, this.getHorizontalStreet()));
		state.add(city.getAverageSpeed(this, this.getHorizontalStreet()));
		state.add(state.get(4) * state.get(5));
		state.add(-1.0); //placeholder
		if(this.verticalState == LightState.RED) {
			if(this.horizontalState == LightState.GREEN) 
				state.add(0.0);
			else // light is yellow
				state.add(1.0);
		} else { // horiz is red
			if(this.verticalState == LightState.GREEN)
				state.add(2.0);
			else // vert is yellow
				state.add(3.0);
		}

		return state;
	}

	/* Log of all of the states. */
	private Vector<Vector<Double>> timeSeries = new Vector<Vector<Double>>();
	/* 
	 * Index #:
	 *  1: 0/1/2/3 light state
	 *  2: long time series
	 *  3: 0/1 whether we're looking at input/before (0) or output/next-step (1)
	 *  4: State indeces
	 */
	private Vector<Vector<Vector<Vector<Double>>>> timeSeriesByLightState = new Vector<Vector<Vector<Vector<Double>>>>();


	/* Pointer to the pair used for the previous step and current step. */
	private Vector<Double> previousState = null;

	Vector<Double[][]> As = new Vector<Double[][]>();


	private void setupFVI() {
		timeSeriesByLightState.add(new Vector<Vector<Vector<Double>>>()); // 0
		timeSeriesByLightState.add(new Vector<Vector<Vector<Double>>>()); // 1
		timeSeriesByLightState.add(new Vector<Vector<Vector<Double>>>()); // 2
		timeSeriesByLightState.add(new Vector<Vector<Vector<Double>>>()); // 3

		As.add(new Double[6][8]); // A0
		As.add(new Double[6][8]); // A1
		As.add(new Double[6][8]); // A2
		As.add(new Double[6][8]); // A3
		for(int i = 0; i < As.size(); i ++)
			for(int j = 0; j < As.get(i).length; j ++)
				for(int k = 0; k < As.get(i)[j].length; k ++)
					As.get(i)[j][k] = 0.0;
	}

	/* Find the As using the timeSeries. */
	private void findAs() {
		//System.out.println("Find As!");
		for(int a = 0; a < 4; a ++) {
			Double[][] A = As.get(a);
			Vector<Vector<Vector<Double>>> trainingExamples = timeSeriesByLightState.get(a);

			for(int row = 0; row < A.length; row ++) {
				// run an entire linear regression algorithm to find this row of A
				boolean convergence = false;
				int counter = 0;
				while(! convergence) {

					// find our predictions
					Vector<Double> predictions = new Vector<Double>();
					for(Vector<Vector<Double>> example : trainingExamples) {
						double prediction = 0.0;
						for(int j = 0; j < A[row].length; j ++) {
							prediction += A[row][j] * example.get(0).get(j);
						}
						predictions.add(prediction);
					}

					// evaulate our predictions against actuality and update A appropriately 
					for(int col = 0; col < A[0].length; col ++) {
						double sum = 0.0;
						for(int ex = 0; ex < trainingExamples.size(); ex ++) {
							Vector<Vector<Double>> example = trainingExamples.get(ex);
							double actualY = example.get(1).get(row + 1); // reason we're doing + 1: we're not trying to predict the first element of the state vector, which is always 1
							sum += (actualY - predictions.get(ex)) * example.get(0).get(col);
						}

						A[row][col] += FVI_ALPHA * sum;
					}

					counter ++;
					if(counter > 50)
						convergence = true; // TODO figure out a better way to detect convergence
				}
			}
		}
	}


	private Vector<Double> predictNextState(Vector<Double> currentState, int lightStatus) {
		//System.out.println("Predict Next State!");
		Double[][] A = As.get(lightStatus);
		Vector<Double> predictedNextState = new Vector<Double>();
		predictedNextState.add(1.0);

		for(int row = 0; row < A.length; row ++) {
			double sum = 0.0;
			for(int col = 0; col < A[row].length; col ++) {
				sum += A[row][col] * currentState.get(col);
			}

			predictedNextState.add(sum);
		}

		return predictedNextState;
	}


	private double reward(Vector<Double> state) {
		double numerator = state.get(3) + state.get(6);
		double denominator = state.get(1) + state.get(4);
		if(denominator == 0)
			return 50;
		return numerator / denominator;
	}

	double GAMMA = 0.95;

	private double findValue(Vector<Double> state, Double[] theta) {
		double sum = 0.0;

		for(int i = 0; i < state.size(); i ++) {
			sum += theta[i] * state.get(i);
		}

		return sum;
	}

	private Vector<Double[]> findThetas() {
		//System.out.println("Find Thetas!");
		Vector<Double[]> thetas = new Vector<Double[]>();

		for(int a = 0; a < 4; a ++) {
			Double[] theta = new Double[7]; // Don't consider the next decision or the next light state
			for(int i = 0; i < 7; i ++)
				theta[i] = 0.0;

			Vector<Vector<Double>> randomStates = new Vector<Vector<Double>>();
			for(int m = 0; m < 50; m ++) {
				int randomIndex = (int) Math.floor(Math.random() * (double) timeSeriesByLightState.get(a).size());
				randomStates.add(timeSeriesByLightState.get(a).get(randomIndex).get(0));
			}

			int counter = 0;
			boolean convergence = false;
			int decisionIndex = 7;
			Vector<Double> value = new Vector<Double>();
			while(! convergence) {
				for(Vector<Double> randomStateOriginal : randomStates) {					
					Vector<Double> randomStateClone = (Vector<Double>) randomStateOriginal.clone();

					randomStateClone.set(decisionIndex, 0.0);
					Vector<Double> nextState0 = predictNextState(randomStateClone, a);
					randomStateClone.set(decisionIndex, 1.0);
					Vector<Double> nextState1 = predictNextState(randomStateClone,a);


					double value0 = reward(randomStateOriginal) + GAMMA * findValue(nextState0, theta);
					double value1 = reward(randomStateOriginal) + GAMMA * findValue(nextState1, theta);

					value.add(Math.max(value0, value1));
				}

				// Run Linear Regression
				// find our predictions
				boolean regConvergence = false;
				int regCounter = 0;
				while(! regConvergence) {	
					Vector<Double> predictions = new Vector<Double>();
					for(Vector<Double> example : randomStates) {
						double prediction = 0.0;
						for(int j = 0; j < theta.length; j ++) {
							prediction += theta[j] * example.get(j);
						}
						predictions.add(prediction);
					}

					// evaulate our predictions against actuality and update A appropriately 
					for(int pos = 0; pos < theta.length; pos ++) {
						double sum = 0.0;
						for(int ex = 0; ex < randomStates.size(); ex ++) {
							sum += (value.get(ex) - predictions.get(ex)) * randomStates.get(ex).get(pos);
						}

						theta[pos] += FVI_ALPHA * sum;
					}

					regCounter ++;
					if(regCounter > 50)
						regConvergence = true; // TODO figure out a better way to detect convergence

				}

				counter ++;
				if (counter > 50)
					convergence = true;

			}

			thetas.add(theta);
		}

		return thetas;
	}

	private int FVIcounter = 0; // how many FVI steps we've taken
	private final int FVI_RECALCULATE_INTERVAL = 200;
	private Vector<Double[]> currentThetas;

	/* One step using the fitted value iteration reinforcement learning model. */
	private void fittedValueIterationStep() {
		//System.out.println("FVI step #" + FVIcounter);

		Vector<Double> currentState = getFVIState();

		/* If the light is yellow, no decision is to be made. */
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

				currentState.set(7, 1.0); // keeping track of whether or not this step was right before the yellow turned red
				yellowLightCounter = -1; // this will freeze it until we decide to switch
			} else {
				currentState.set(7, 0.0); // keeping track of whether or not this step was right before the yellow turned red
			}

			/* If the light has just turned green, no decision is to be made. */
		} else if (greenLightCounter >= 0) {
			greenLightCounter++;
			if ((float)greenLightCounter * City.TIME_STEP >= MIN_GREEN_LIGHT) {
				greenLightCounter = -1;
			}

			currentState.set(7, 0.0); // no change was made here either
		} else {
			/* Need to make a decision. */
			boolean change = false; // the final decision: whether or not to change.

			if( timeSeriesByLightState.get(0).size() > 50 &&
					timeSeriesByLightState.get(1).size() > 50 &&
					timeSeriesByLightState.get(2).size() > 50 &&
					timeSeriesByLightState.get(3).size() > 50) {

				if(FVIcounter == 0) {
					city.resetCounter();
				}

				// Run our FVI decision algorithm
				if(FVIcounter % FVI_RECALCULATE_INTERVAL == 0) {
					// first time running FVI
					findAs();
					currentThetas = findThetas();	
				}

				currentState.set(7, 0.0); // decision gets set at end of method based on result, so are free to edit it here
				Vector<Double> nextState0 = predictNextState(currentState, currentState.get(8).intValue());
				currentState.set(7,1.0);
				Vector<Double> nextState1 = predictNextState(currentState, currentState.get(8).intValue());

				double value0 = findValue(nextState0,currentThetas.get(currentState.get(8).intValue()));
				double value1 = findValue(nextState1,currentThetas.get(currentState.get(8).intValue()));

				if(value1 > value0) {
					change = true;
				} else {
					change = false;
				}

				FVIcounter ++;
			} else {
				// Need to simulate some starting steps
				change = fixedLengthStep();
			}


			if(change) {
				yellowLightCounter = 0;

				if (this.verticalState == LightState.GREEN) {
					this.verticalState = LightState.YELLOW;
				} else if (this.horizontalState == LightState.GREEN) {
					this.horizontalState = LightState.YELLOW;
				}

				currentState.set(7, 1.0);
			} else {
				currentState.set(7, 0.0);
			}
		}

		/* Add current state to the log. */
		timeSeries.add(currentState);
		if(previousState != null) {
			Vector<Vector<Double>> stepPair = new Vector<Vector<Double>>();
			stepPair.add(previousState);
			stepPair.add(currentState);
			timeSeriesByLightState.get(previousState.get(8).intValue()).add(stepPair);
		}

		previousState = currentState;
	}

	//// code for linear regression \\\\

	private static Vector<Double> weights = new Vector<Double>();
	private Vector<Vector<Double>> inputs = new Vector<Vector<Double>>();

	private static final int LOOKAHEAD = 20;
	private static final int YELLOW_LIGHT = 5;
	private static final int MIN_GREEN_LIGHT = 2;
	private static final int NUM_FEATURES = 7;

	private static final double REG_ALPHA = 0.0001;
	private static final double FVI_ALPHA = 0.1;

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
		//state.add(city.getAverageSpeed(this, this.getVerticalStreet()) * city.getNumCars(this,  this.getVerticalStreet()));
		state.add(city.getNumCars(this, this.getVerticalStreet()));
		state.add(city.getNumCars(this, this.getHorizontalStreet()));
		state.add(city.getAverageSquaredSpeed(this, this.getHorizontalStreet()));
		//state.add(city.getAverageSpeed(this, this.getHorizontalStreet()) * city.getNumCars(this,  this.getHorizontalStreet()));
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
