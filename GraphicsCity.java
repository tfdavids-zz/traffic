import java.util.*;
import acm.graphics.*;
import acm.program.*;
import javax.swing.*;
import java.awt.*;
import java.lang.*;

public class GraphicsCity extends City {
    private static final int ROAD_WIDTH = 4;
    private static final int CAR_WIDTH = 2;
    private static final int CAR_LENGTH = 5;

    private GCanvas gc;
    private JFrame frame;
    private GRect city;
    private Vector<GRect> vertRoads = new Vector<GRect>();
    private Vector<GRect> horizRoads = new Vector<GRect>();

    private Vector<GRect> carsCache = new Vector<GRect>();

    public GraphicsCity (Light.LightMethod method) {
        super(method);
        renderCity();
    }

    public void step() {
        super.step();
        renderCars();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void renderCity() {
        gc = new GCanvas();
        frame = new JFrame();
        frame.getContentPane().add(BorderLayout.CENTER, gc);

        city = new GRect(HORIZONTAL_STREET_LENGTH, VERTICAL_STREET_LENGTH);
        city.setFilled(true);
        city.setColor(Color.GREEN);
        gc.add(city);

        for (int i = 0; i < HORIZONTAL_STREETS; i++) {
            GRect street = new GRect(0, VERTICAL_STREET_LENGTH * (i + 1) / (HORIZONTAL_STREETS + 1) - ROAD_WIDTH / 2, HORIZONTAL_STREET_LENGTH, ROAD_WIDTH);
            street.setFilled(true);
            street.setColor(Color.GRAY);
            horizRoads.add(street);
            gc.add(street);
        }

        for (int i = 0; i < VERTICAL_STREETS; i++) {
            GRect street = new GRect(HORIZONTAL_STREET_LENGTH * (i + 1) / (VERTICAL_STREETS + 1) - ROAD_WIDTH / 2, 0, ROAD_WIDTH, VERTICAL_STREET_LENGTH);
            street.setFilled(true);
            street.setColor(Color.GRAY);
            horizRoads.add(street);
            gc.add(street);
        }

        frame.setSize((int) HORIZONTAL_STREET_LENGTH, (int) VERTICAL_STREET_LENGTH);
        frame.show();
    }

    private void renderCars() {
        for (GRect car : carsCache) {
            gc.remove(car);
        }
        carsCache.clear();
        for (int i = 0; i < this.cars.size(); i++) {
            if (i % 2 == 0) {
                for (int j = 0; j < cars.elementAt(i).size(); j++) {
                    GRect car = new GRect(cars.elementAt(i).elementAt(j).getPosition() - CAR_LENGTH, VERTICAL_STREET_LENGTH * (i / 2 + 1) / (HORIZONTAL_STREETS + 1) - CAR_WIDTH / 2, CAR_LENGTH, CAR_WIDTH);
                    car.setFilled(true);
                    car.setColor(Color.BLACK);
                    gc.add(car);
                    carsCache.add(car);
                }
            } else if (i % 2 == 1) {
                for (int j = 0; j < cars.elementAt(i).size(); j++) {
                    GRect car = new GRect(HORIZONTAL_STREET_LENGTH * ((i + 1) / 2) / (VERTICAL_STREETS + 1) - CAR_WIDTH / 2, cars.elementAt(i).elementAt(j).getPosition() - CAR_LENGTH, CAR_WIDTH, CAR_LENGTH);
                    car.setFilled(true);
                    car.setColor(Color.BLACK);
                    gc.add(car);
                    carsCache.add(car);
                }
            }
        }
    }
}
