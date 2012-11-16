import java.util.*;

public class City {
	// EVEN STREETS ARE HORIZONTAL (0-indexed), ODD STREETS ARE VERTICAL
	
	public static final int HORIZONTAL_STREETS = 2;
	public static final int VERTICAL_STREETS = 2;
	
	public static final double HORIZONTAL_STREET_LENGTH = 1000.0; // in meters
	public static final double VERTICAL_STREET_LENGTH = 1000.0; // in meters
	
	private Vector<Vector<Car>> cars;
	private Vector<Light> stoplights; // lights are numbered left to right, top to bottom
	
	public City () {
		// TODO: Fill this in
		
		this.cars = new Vector<Vector<Car>>();
		for (int i = 0; i < HORIZONTAL_STREETS + VERTICAL_STREETS; i++) {
			this.cars.add(new Vector<Car>());
			this.cars.elementAt(i).add(new Car(this, i));
		}
		
		this.stoplights = new Vector<Light>();
	}
	
	public Light.LightState getLightState(int street, int index) {
		if (street % 2 == 0) {
			return stoplights.elementAt(street / 2 + index).getState(street);
		} else {
			return stoplights.elementAt(index * VERTICAL_STREETS + (street - 1) / 2).getState(street);
		}
	}
	
	public void step() {
		
		// First step with each car
		for (int street = 0; street < this.cars.size(); street++) {
			for (int car = 0; car < this.cars.elementAt(street).size(); car++) {
				this.cars.elementAt(street).elementAt(car).step();
			}
		}
		
		// Now potentially add new cars
		
		
		// Then let each light make its decision
		for (int light = 0; light < this.stoplights.size(); light++) {
			this.stoplights.elementAt(light).step();
		}
	}
	
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
			System.err.println("Slept!");
		}
	}
}
