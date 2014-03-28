
public class Simulator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

        City city = new GraphicsCity(Light.LightMethod.CONSTANT);
        double regression = city.simulate();

        /*
		City city = new GraphicsCity(Light.LightMethod.WAITING);
		double regression = city.simulate();
		city = new City(Light.LightMethod.CONSTANT);
		double constant = city.simulate();

		System.out.println("Average speed for benchmark (constant) was " + constant + " meters per second.");
		System.out.println("Average speed for regression on waiting cars was " + regression + " meters per second.");
		System.out.println("Regression algorithm saved " + (regression-constant) + " meters per second.");
        */
	}

    private static void testRegression() {
		City city = new GraphicsCity(Light.LightMethod.CONSTANT);
		double constant = city.simulate();
		city = new GraphicsCity(Light.LightMethod.REGRESSION);
		double regression = city.simulate();
		
		System.out.println("Average speed for benchmark (constant) was " + constant + " meters per second.");
		System.out.println("Average speed for regression was " + regression + " meters per second.");
		System.out.println("Regression algorithm saved " + (regression-constant) + " meters per second.");
    }

}
