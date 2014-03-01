
public class Simulator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		City city = new GraphicsCity(Light.LightMethod.CONSTANT);
		double constant = city.simulate();
		city = new GraphicsCity(Light.LightMethod.REGRESSION);
		double regression = city.simulate();
		
		System.out.println("Average speed for benchmark (constant) was " + constant + " meters per second.");
		System.out.println("Average speed for regression was " + regression + " meters per second.");
		//System.out.println("Average speed for FVI was " + fvi + " meters per second.");
		System.out.println("Regression algorithm saved " + (regression-constant) + " meters per second.");
		//System.out.println("FVI saved " + (fvi-constant) + " meters per second.");
	}

}
