public class WeightedLight extends Light {
    public WeightedLight(City city, int verticalStreet, int horizontalStreet) {
        super(city, verticalStreet, horizontalStreet);
    }

	@Override protected void step() {
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
}
