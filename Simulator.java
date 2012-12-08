
public class Simulator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		City city = new City();
		double constant = city.simulate(Light.LightMethod.CONSTANT);
		city = new City();
		double regression = city.simulate(Light.LightMethod.REGRESSION);
//		city = new City();
		// double fvi = city.simulate(Light.LightMethod.FVI);
		
		
		System.out.println("Average speed for regression was " + regression + " meters per second.");
		System.out.println("Average speed for benchmark (constant) was " + constant + " meters per second.");
		System.out.println("Learning algorithm saved " + (regression-constant) + " meters per second.");
		//System.out.println("Average speed for FVI was " + fvi + " meters per second.");
	}

}
