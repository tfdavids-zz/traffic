public class ConstantLight extends Light {
    public ConstantLight(City city, int verticalStreet, int horizontalStreet) {
        super(city, verticalStreet, horizontalStreet);
    }

	@Override protected void step() {
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

}
