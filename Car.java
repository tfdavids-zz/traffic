
public class Car {

	private int street;
	private double speed;
	private double position;
	private City city;
	private double time;

	// Parameters for traffic flow
	public static final double MAX_SPEED = 30.0;
	public static final double SAFE_TIME = 1.5;
	public static final double MAX_ACCEL = 1.0;
	public static final double MAX_DECEL = 3.0;
	public static final int ACCEL_EXP = 4;
	public static final double JAM_DIST = 2.0;
	public static final double VEHICLE_LEN = 5.0;
	

	@Override
	public String toString() {
		return "Car [street=" + street + ", speed=" + speed + ", position="
				+ position + ", city=" + city + "]";
		// return "Car [street=" + street + ", average_speed=" + (position / steps) + "]";
	}

	public Car(City city, int street, double speed) {
		this.city = city;
		this.street = street;
		this.speed = speed;
		this.position = 0.0;
		this.time = 0.0;
	}

	public void step() {		
		Car nextVehicle = getLeader();
		Light nextLight = city.getNextLight(street, position, speed);
		double nextLightPosition = 0; // will get set if it gets used
		if (nextLight != null) {
			nextLightPosition = nextLight.getPosition(street);
		}

		// First, advance by the amount of the car's speed
        // However, if this would take us into another vehicle or into a red light, stop
        if (nextVehicle != null && nextVehicle.getPosition() - VEHICLE_LEN - JAM_DIST - position < speed * City.TIME_STEP) {
            position = nextVehicle.getPosition() - VEHICLE_LEN - JAM_DIST;
        } else if (nextLight != null && nextLightPosition - position < speed * City.TIME_STEP) {
            position = nextLightPosition;
        } else {
		    position += speed * City.TIME_STEP;
        }

		// If this takes us beyond the edge of the city, remove the car
		if (street % 2 == 0 && position > City.HORIZONTAL_STREET_LENGTH) {
			city.removeCar(this, street);
		} else if (street % 2 == 1 && position > City.VERTICAL_STREET_LENGTH) {
			city.removeCar(this, street);
		}
/*
		// Now figure out where the next car and next light are
		Car nextVehicle = getLeader();
		Light nextLight = city.getNextLight(street, position, speed);
		double nextLightPosition = 0; // will get set if it gets used
		if (nextLight != null) {
			nextLightPosition = nextLight.getPosition(street);
		}
*/
		/* Four cases: we can't see either a car or a light, we can see just a car,
		 * we can see just a light, or we can see both.
		 */
		if (nextVehicle == null && nextLight == null) {
			double freeComponent = MAX_ACCEL * (1.0 - Math.pow((speed / MAX_SPEED), 4));
			
			speed += freeComponent * City.TIME_STEP;
		} else if (nextVehicle == null && nextLight != null) {
			double currentDistance = nextLightPosition - position;
			double approachingRate = speed - 0;

			double freeComponent = MAX_ACCEL * (1.0 - Math.pow((speed / MAX_SPEED), 4));
			double intComponent = -MAX_ACCEL * Math.pow((JAM_DIST + speed * SAFE_TIME) / currentDistance + speed * approachingRate / (2.0 * Math.pow(MAX_ACCEL * MAX_DECEL, 0.5) * currentDistance), 2);

			speed += (freeComponent + intComponent) * City.TIME_STEP;
		} else if (nextVehicle != null && nextLight == null) {
			double currentDistance = nextVehicle.getPosition() - position;
			double approachingRate = speed - nextVehicle.getSpeed();

			double freeComponent = MAX_ACCEL * (1.0 - Math.pow((speed / MAX_SPEED), 4));
			double intComponent = -MAX_ACCEL * Math.pow((JAM_DIST + speed * SAFE_TIME) / currentDistance + speed * approachingRate / (2.0 * Math.pow(MAX_ACCEL * MAX_DECEL, 0.5) * currentDistance), 2);

			speed += (freeComponent + intComponent) * City.TIME_STEP;
		} else {
			// car first

			double currentDistanceCar = nextVehicle.getPosition() - position;
			double approachingRateCar = speed - nextVehicle.getSpeed();

			double freeComponentCar = MAX_ACCEL * (1.0 - Math.pow((speed / MAX_SPEED), 4));
			double intComponentCar = -MAX_ACCEL * Math.pow((JAM_DIST + speed * SAFE_TIME) / currentDistanceCar + speed * approachingRateCar / (2.0 * Math.pow(MAX_ACCEL * MAX_DECEL, 0.5) * currentDistanceCar), 2);

			double carAccel = freeComponentCar + intComponentCar;

			// now for light

			double currentDistanceLight = nextLightPosition - position;
			double approachingRateLight = speed - 0;

			double freeComponentLight = MAX_ACCEL * (1.0 - Math.pow((speed / MAX_SPEED), 4));
			double intComponentLight = -MAX_ACCEL * Math.pow((JAM_DIST + speed * SAFE_TIME) / currentDistanceLight + speed * approachingRateLight / (2.0 * Math.pow(MAX_ACCEL * MAX_DECEL, 0.5) * currentDistanceLight), 2);

			double lightAccel = freeComponentLight + intComponentLight;

			if (lightAccel < carAccel) {
				speed += lightAccel * City.TIME_STEP;
			} else {
				speed += carAccel * City.TIME_STEP;
			}
		}

		// in rare cases, due to discreteness of time steps, speed can drop fractionally below 0 -- we don't want that
		speed = Math.max(speed, 0);
		
		// Finally, increment time by the proper amount
		this.time += City.TIME_STEP;
	}

	public double getPosition() {
		return this.position;
	}

	public double getSpeed() {
		return this.speed;
	}
	
	public double getAverageSpeed() {
		if (this.time > 0) {
			return this.position / this.time;
		} else {
			return 0.0;
		}
	}
	
	public double getTime() {
		return this.time;
	}

	public City getCity() {
		return this.city;
	}

	// get the car in front of this one
	private Car getLeader() {
        
		return this.city.getLeader(this, street);
	}

}
