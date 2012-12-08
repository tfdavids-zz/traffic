import java.util.*;

public class City {
	// EVEN STREETS ARE HORIZONTAL (0-indexed), ODD STREETS ARE VERTICAL

	public static final int HORIZONTAL_STREETS = 2;
	public static final int VERTICAL_STREETS = 2;

	public static final double HORIZONTAL_STREET_LENGTH = 900.0; // in meters
	public static final double VERTICAL_STREET_LENGTH = 900.0; // in meters

	public static final double CAR_GEN_PROB = 0.10;
	public static final double TIME_STEP = 0.1;

	private Vector<Vector<Car>> cars;
	private Vector<Vector<Light>> stoplights; // first index of light is (vertical street - 1) / 2 (x-coord), second is (horizontal street / 2)
	
	private static final int REPS = 10000;
	
	// for scoring
	private double totalPosition;
	private double totalTime;

	Random rand = new Random();

	public City () {

		this.cars = new Vector<Vector<Car>>();
		for (int i = 0; i < HORIZONTAL_STREETS + VERTICAL_STREETS; i++) {
			this.cars.add(new Vector<Car>());
		}

		this.stoplights = new Vector<Vector<Light>>();
		for (int i = 0; i < VERTICAL_STREETS; i++) {
			this.stoplights.add(new Vector<Light>());
			for (int j = 0; j < HORIZONTAL_STREETS; j++) {
				this.stoplights.elementAt(i).add(new Light(this, 2*i+1, 2*j));
			}
		}
	}

	/**
	 * @param street1 A street on the grid
	 * @param street2 An intersecting street on the grid
	 * @return The light at the intersection of street1 and street2
	 */
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

	/**
	 * NOTE: if a car can stop at a yellow light (or red light) it does, otherwise it continues on.
	 * It uses the equation vf2 = v02 + 2ad to find the stopping distance at the comfortable
	 * deceleration -- this should be checked.
	 * 
	 * @param street
	 * @param position
	 * @param speed
	 * @return The next light that will affect the travel of a car on the given street at
	 * the given position, traveling at the given speed
	 */
	public Light getNextLight(int street, double position, double speed) {
		double stoppingDistance = speed * speed / (2 * Car.MAX_DECEL);

		if (street % 2 == 1) {
			for (int i = 0; i < stoplights.elementAt((street - 1) / 2).size(); i++) {
				if (getLight(street, 2 * i).getPosition(street) > position + stoppingDistance
						&& getLight(street, 2 * i).getState(street) != Light.LightState.GREEN)
					return getLight(street, 2 * i);
			}
		} else {
			for (int i = 0; i < stoplights.size(); i++) {
				if (getLight(street, 2 * i + 1).getPosition(street) > position + stoppingDistance
						&& getLight(street, 2 * i + 1).getState(street) != Light.LightState.GREEN)
					return getLight(street, 2 * i + 1);
			}
		}

		return null;
	}

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

				if (firstCar.getPosition() > Car.VEHICLE_LEN + Car.JAM_DIST) {
					double x = rand.nextDouble();
					if (x < CAR_GEN_PROB * TIME_STEP) {
						this.cars.elementAt(street).add(new Car(this, street, firstCar.getSpeed()));
						//System.out.("Added car: " + this.cars.elementAt(street).lastElement().toString());
					}
				}
			} else {
				double x = rand.nextDouble();
				if (x < CAR_GEN_PROB * TIME_STEP) {
					this.cars.elementAt(street).add(new Car(this, street, Car.MAX_SPEED));
					//System.out.println("Added car: " + this.cars.elementAt(street).lastElement().toString());
				}
			}
		}

		// Then let each light make its decision
		for (int i = 0; i < stoplights.size(); i++) {
			for (int j = 0; j < stoplights.elementAt(i).size(); j++) {
				stoplights.elementAt(i).elementAt(j).step();
			}
		}

		// For debugging: print out street 0
		/*
		if ((int)(this.time * 10) == ((int)this.time) * 10) {

			for (int i = 0; i < cars.elementAt(0).size(); i++) {
				System.err.println("Car on street 0: " + cars.elementAt(0).elementAt(i));
			}
			for (int j = 0; j < stoplights.elementAt(0).size(); j++) {
				System.err.println("Light on street 0 at street " + (2 * j + 1) + " is " + stoplights.elementAt(j).elementAt(0).getState(0));
			}

		}
		 */
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
		//System.out.("Removed car: " + car.toString());
		totalPosition += car.getPosition();
		totalTime += car.getTime();
	}

	// features
	
	public double getAverageSpeed(Light light, int street) {
		double value = 0.0;
		int count = 0;
		
		for (int i = 0; i < this.cars.elementAt(street).size(); i++) {
			if (this.cars.elementAt(street).elementAt(i).getPosition() < light.getPosition(street)) {
				count++;
				value += this.cars.elementAt(street).elementAt(i).getSpeed();
			}
		}
		
		if (count > 0) {
			return (value / count) / Car.MAX_SPEED;
		} else {
			return 0.0;
		}
	}
	
	public double getAverageSquaredSpeed(Light light, int street) {
		double value = 0.0;
		int count = 0;
		
		for (int i = 0; i < this.cars.elementAt(street).size(); i++) {
			if (this.cars.elementAt(street).elementAt(i).getPosition() < light.getPosition(street)) {
				count++;
				value += Math.pow(this.cars.elementAt(street).elementAt(i).getSpeed(), 2);
			}
		}
		
		if (count > 0) {
			return (value / count) / Math.pow(Car.MAX_SPEED, 2);
		} else {
			return 0.0;
		}
	}
	
	public double getNumCars(Light light, int street) {
		int count = 0;
		
		for (int i = 0; i < this.cars.elementAt(street).size(); i++) {
			if (this.cars.elementAt(street).elementAt(i).getPosition() < light.getPosition(street)) {
				count++;
			}
		}
		
		return (double)count;
	}
	
	// cost function
	
	public double getAverageAverageSpeed() {
		double value = 0.0;
		int count = 0;
		
		for (int i = 0; i < this.cars.size(); i++) {
			for (int j = 0; j < this.cars.elementAt(i).size(); j++) {
				value += this.cars.elementAt(i).elementAt(j).getAverageSpeed();
				count++;
			}
		}
		
		if (count > 0) {
			return (value / count) / Car.MAX_SPEED;
		} else {
			return 0.0;
		}
	}
	
	public double simulate(Light.LightMethod method) {
		Light.method = method;
		
		this.cars = new Vector<Vector<Car>>();
		for (int i = 0; i < HORIZONTAL_STREETS + VERTICAL_STREETS; i++) {
			this.cars.add(new Vector<Car>());
		}
		
		totalPosition = 0;
		totalTime = 0;
		
		for (int i = 0; i < REPS; i++) {
			
			step();
			/*
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			*/
		}
		//System.out.println("Total score: " + (totalPosition / totalTime));
		
		return totalPosition / totalTime;
	}
	
	public void resetCounter() {
		totalPosition = 0;
		totalTime = 0;
	}
}
