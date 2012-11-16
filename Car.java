
public class Car {
	
	private int street;
	private double speed;
	private double position;
	private City city;
	public static final double MAX_SPEED = 45;
	
	@Override
	public String toString() {
		return "Car [street=" + street + ", speed=" + speed + ", position="
				+ position + ", city=" + city + "]";
	}

	public Car(City city, int street) {
		this.city = city;
		this.street = street;
		this.speed = MAX_SPEED; // this may need to be adjusted -- speed of car immediately in front of it?
		this.position = 0.0;
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
		
	}
	
	public double getPosition() {
		return this.position;
	}
	
	public City getCity() {
		return this.city;
	}
	
	// get the car in front of this one
	private Car getLeader() {
		return this.city.getLeader(this, street);
	}
	
}
