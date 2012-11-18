import java.util.*;

public class City {
	// EVEN STREETS ARE HORIZONTAL (0-indexed), ODD STREETS ARE VERTICAL

	public static final int HORIZONTAL_STREETS = 2;
	public static final int VERTICAL_STREETS = 2;

	public static final double HORIZONTAL_STREET_LENGTH = 1000.0; // in meters
	public static final double VERTICAL_STREET_LENGTH = 1000.0; // in meters

	public static final double CAR_GEN_PROB = 0.25;

	private Vector<Vector<Car>> cars;
	private Vector<Vector<Light>> stoplights; // first index of light is vertical street (x-coord), second is horizontal street

	Random rand = new Random();

	public City () {
		// TODO: Fill this in

		this.cars = new Vector<Vector<Car>>();
		for (int i = 0; i < HORIZONTAL_STREETS + VERTICAL_STREETS; i++) {
			this.cars.add(new Vector<Car>());
		}

		this.stoplights = new Vector<Vector<Light>>();
		for (int i = 0; i < VERTICAL_STREETS; i++) {
			this.stoplights.add(new Vector<Light>());
			for (int j = 0; j < HORIZONTAL_STREETS; j++) {
				this.stoplights.elementAt(i).add(new Light());
			}
		}
	}
	
	public Light getLight(int street1, int street2) {
		if (street1 % 2 == street2 % 2) {
			System.err.println("Error: trying to find intersection of parallel streets");
			return null;
		}
		
		if (street1 % 2 == 0) {
			return this.stoplights.elementAt((street2 - 1) / 2).elementAt(street1 / 2);
		} else {
			return this.stoplights.elementAt((street1 - 1) / 2).elementAt(street2 / 2);
		}
		
	}
	
	public double getLightPosition(int street, int cross) {
		if (street % 2 == 0) {
			return (((double) cross - 1) / 2) / (VERTICAL_STREETS + 1);
		} else {
			return ((double) cross / 2) / (HORIZONTAL_STREETS + 1);
		}
	}
	
	public Light getNextLight(int street, double position, double speed) {
		// returns the next light that will affect the travel of a car at this location and speed
		// NOTE: if a car can stop at a yellow light (or red light) it does, otherwise continues on
		
		// this method assumes we're going from low-indexed streets to high-indexed streets
		
		double stoppingDistance = speed * speed / (2 * Car.MAX_DECEL);
		
		if (street % 2 == 1) {
			for (int i = 0; i < stoplights.elementAt((street - 1) / 2).size(); i++) {
				if (getLightPosition((street - 1) / 2, 2 * i) > position + stoppingDistance)
					return stoplights.elementAt((street - 1) / 2).elementAt(i);
			}
		} else {
			for (int i = 0; i < stoplights.size(); i++) {
				if (getLightPosition(street / 2, 2 * i + 1) > position + stoppingDistance)
					return stoplights.elementAt(i).elementAt(street / 2);
			}
		}
		
		return null;
	}
	
	public double getNextLightPosition(int street, double position, double speed) {
		double stoppingDistance = speed * speed / (2 * Car.MAX_DECEL);
		
		if (street % 2 == 1) {
			for (int i = 0; i < stoplights.elementAt((street - 1) / 2).size(); i++) {
				if (getLightPosition((street - 1) / 2, 2 * i) > position + stoppingDistance)
					return getLightPosition((street - 1) / 2, 2 * i);
			}
		} else {
			for (int i = 0; i < stoplights.size(); i++) {
				if (getLightPosition(street / 2, 2 * i + 1) > position + stoppingDistance)
					return getLightPosition(street / 2, 2 * i + 1);
			}
		}
		
		return -1;
	}
	
/*
	public Light.LightState getLightState(int street, int index) {
		if (street % 2 == 0) {
			return stoplights.elementAt(street / 2 + index).getState(street);
		} else {
			return stoplights.elementAt(index * VERTICAL_STREETS + (street - 1) / 2).getState(street);
		}
	}
*/
	public void step() {

		// First step with each car
		for (int street = 0; street < this.cars.size(); street++) {
			for (int car = 0; car < this.cars.elementAt(street).size(); car++) {
				this.cars.elementAt(street).elementAt(car).step();
			}
		}

		// Now potentially add new cars
		for (int street = 0; street < this.cars.size(); street++) {
			if (this.cars.elementAt(street).size() > 0) {
				Car firstCar = this.cars.elementAt(street).lastElement();

				if (firstCar.getPosition() > Car.VEHICLE_LEN + 10.0) {
					// TODO: fix the conditional here

					double x = rand.nextDouble();
					if (x < CAR_GEN_PROB) {
						this.cars.elementAt(street).add(new Car(this, street, firstCar.getSpeed()));
						System.out.println("Added car: " + this.cars.elementAt(street).lastElement().toString());
					}
				}
			} else {
				double x = rand.nextDouble();
				if (x < CAR_GEN_PROB) {
					this.cars.elementAt(street).add(new Car(this, street, Car.MAX_SPEED));
					System.out.println("Added car: " + this.cars.elementAt(street).lastElement().toString());
				}
			}
		}

		// Then let each light make its decision
		for (int i = 0; i < stoplights.size(); i++) {
			for (int j = 0; j < stoplights.elementAt(i).size(); j++) {
				stoplights.elementAt(i).elementAt(j).step();
			}
		}
	}

	// Returns the car directly in front of this one, on the given street
	public Car getLeader(Car car, int street) {
		int index = cars.elementAt(street).indexOf(car);
		if (index < cars.elementAt(street).size() - 1) {
			return cars.elementAt(street).elementAt(index + 1);
		} else {
			return null;
		}
	}

	public void removeCar(Car car, int street) {
		cars.elementAt(street).remove(car);
		System.out.println("Removed car: " + car.toString());
	}

	public void simulate() {
		while (true) {
			step();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//System.err.println("Slept!");
		}
	}
}
