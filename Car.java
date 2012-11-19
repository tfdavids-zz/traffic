
public class Car {

	private int street;
	private double speed;
	private double position;
	private City city;
	private int steps;

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
		//return "Car [street=" + street + ", speed=" + speed + ", position="
		//		+ position + ", city=" + city + "]";
		return "Car [street=" + street + ", average_speed=" + (position / steps) + "]";
	}

	public Car(City city, int street, double speed) {
		this.city = city;
		this.street = street;
		this.speed = speed; // this may need to be adjusted -- speed of car immediately in front of it?
		this.position = 0.0;
		this.steps = 0;
	}

	public void step() {
		// TODO: finish implementing
		
		position += speed;

		if (street % 2 == 0) {
			if (position > City.HORIZONTAL_STREET_LENGTH) {
				city.removeCar(this, street);
			}
		} else {
			if (position > City.VERTICAL_STREET_LENGTH) {
				city.removeCar(this, street);
			}
		}

		Car nextVehicle = getLeader();
		Light nextLight = city.getNextLight(street, position, speed);
		double nextLightPosition = city.getNextLightPosition(street, position, speed);
		
		if (nextVehicle == null && nextLight == null) {
			// no interaction component if we can't see another car
			
			double freeComponent = MAX_ACCEL * (1.0 - Math.pow((speed / MAX_SPEED), 4));

			speed += freeComponent;
			
		} else if ((nextVehicle == null && nextLight != null) ||
				(nextVehicle != null && nextLightPosition < nextVehicle.getPosition())) {
			// treat the light like a stop sign
			
			double currentDistance = nextLightPosition - position;
			double approachingRate = speed - 0;

			double freeComponent = MAX_ACCEL * (1.0 - Math.pow((speed / MAX_SPEED), 4));
			double intComponent = -MAX_ACCEL * Math.pow((JAM_DIST + speed * SAFE_TIME) / currentDistance + speed * approachingRate / (2.0 * Math.pow(MAX_ACCEL * MAX_DECEL, 0.5) * currentDistance), 2);

			speed += freeComponent + intComponent;
			
			//System.err.println("Slowing for light on street " + street);
			
		} else if ((nextVehicle != null && nextLight == null) ||
			(nextVehicle != null && nextLightPosition > nextVehicle.getPosition())) {

			double currentDistance = nextVehicle.getPosition() - position;
			double approachingRate = speed - nextVehicle.getSpeed();

			double freeComponent = MAX_ACCEL * (1.0 - Math.pow((speed / MAX_SPEED), 4));
			double intComponent = -MAX_ACCEL * Math.pow((JAM_DIST + speed * SAFE_TIME) / currentDistance + speed * approachingRate / (2.0 * Math.pow(MAX_ACCEL * MAX_DECEL, 0.5) * currentDistance), 2);

			speed += freeComponent + intComponent;
		} 

		steps++;
		
	}

	public double getPosition() {
		return this.position;
	}

	public double getSpeed() {
		return this.speed;
	}
	
	public City getCity() {
		return this.city;
	}

	// get the car in front of this one
	private Car getLeader() {
		return this.city.getLeader(this, street);
	}

}
