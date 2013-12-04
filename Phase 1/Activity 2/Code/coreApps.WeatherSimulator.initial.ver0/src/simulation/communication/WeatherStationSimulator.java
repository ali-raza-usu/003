package simulation.communication;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import utilities.messages.WeatherDataReading;
import utilities.messages.WeatherDataVector;
import utilities.messages.WeatherDataVector.LocType;
import utilities.messages.WeatherDataVector.ObservationType;

import org.apache.log4j.Logger;

//This class generates Wind data such as wind speed and direction in a winddatavector form
//The frequency attribute decided how frequent WindDataVector would be generated
//Compression value compression type to the corresponding data vector format
//It controls how big the size of these values
//Resolution decides how 
public class WeatherStationSimulator extends Thread {

	private WeatherDataVector weatherDataVector = null;

	private Thermometer thermometer = new Thermometer();
	private Anemometer anemometer = new Anemometer();
	private WindVane windVane = new WindVane();
	private Hygrometer hygrometer = new Hygrometer();
	private Barometer barometer = new Barometer();
	private Ceilometer ceilometer = new Ceilometer();
	private VisibilitySensor visibilitySensor = new VisibilitySensor();
	private RainGauge rainGauge = new RainGauge();
	private UltrasonicSnowSensor ultrasonicSnowSensor = new UltrasonicSnowSensor();
	private Pyranometer pyranometer = new Pyranometer();
	private Mast mast = new Mast();

	private Timer lapse = new Timer();
	private int freq;
	private int numOfReadings = mast.totalHeights();

	private Queue<WeatherDataVector> list = new LinkedList<WeatherDataVector>();
	private static Logger logger = Logger
			.getLogger(WeatherStationSimulator.class);
	// QoSMonitor qosMonitor = QoSMonitor.getInstance();
	public static WeatherStationSimulator sensor = null;

	public WeatherStationSimulator() {
		super();
	}

	public static WeatherStationSimulator createInstance(int freq,
			int resolution) {
		sensor = new WeatherStationSimulator(freq, resolution);
		sensor.start();
		// logger.debug("Started Sensor for file "+ fileName);
		return sensor;
	}

	public WeatherStationSimulator(int freq, int resoultion) {
		super();
		this.freq = freq;
		lapse.schedule(new GenerationTask(), this.freq, this.freq);
	}

	class GenerationTask extends TimerTask {
		public GenerationTask() {
		}

		@Override
		public void run() {
			getList().add(generateWeatherDataVector());
		}
	}

	public WeatherDataVector getWindDataVector() {
		return weatherDataVector;
	}

	public void setWindDataVector(WeatherDataVector windDataVector) {
		this.weatherDataVector = windDataVector;
	}

	public WeatherDataVector generateWeatherDataVector() {
		WeatherDataVector _vector = new WeatherDataVector();
		for (int i = 0; i < numOfReadings; i++) {
			byte windSpeed = anemometer.getWindSpeed();
			byte windDirection = windVane.getWindDirection();
			byte humidity = hygrometer.getHumidity();
			byte precipitation = pyranometer.getPrecipitation();
			LocType facilityLocType = generateFacilityLocType();
			double pressure = barometer.getPressure();
			double cloudHeight = ceilometer.getCloudHeight();
			byte visibility = visibilitySensor.getVisibility();
			byte solarRadiations = rainGauge.getSolarRadiations();
			byte snowDepth = ultrasonicSnowSensor.getSnowDepth();
			ObservationType obsType = generateObsType();
			short temperature = thermometer.getTemperature();
			Date _sendTime = new Date();
			int size = generateSize();
			int resolution = generateResolution();

			WeatherDataReading _reading = new WeatherDataReading(windSpeed,
					windDirection, humidity, precipitation, facilityLocType,
					pressure, cloudHeight, visibility, solarRadiations,
					snowDepth, obsType, temperature, _sendTime, size,
					resolution);
			logger.debug(_reading.printReading());
			_vector.getReadings().add(_reading);
		}
		return _vector;
	}

	private LocType generateFacilityLocType() {
		int pick = new Random().nextInt(LocType.values().length);
		return LocType.values()[pick];
	}

	private ObservationType generateObsType() {
		int pick = new Random().nextInt(ObservationType.values().length);
		return ObservationType.values()[pick];
	}

	private int generateSize() {
		int val = (int) (Math.random() * 100);
		return val;
	}

	private int generateResolution() {
		int val = (int) (Math.random() * 100);
		return val;
	}

	public Queue<WeatherDataVector> getList() {
		return list;
	}

	public void setList(Queue<WeatherDataVector> _list) {
		this.list = _list;
	}

	public int getNumOfReadings() {
		return numOfReadings;
	}

	public void setNumOfReadings(int numOfReadings) {
		this.numOfReadings = numOfReadings;
	}
}

class Thermometer {

	private String unit = "Celcius";

	public short getTemperature() {
		short val = (short) (Math.random() * 100);
		return val;
	}
}

class Anemometer {

	private String unit = "knot";

	public byte getWindSpeed() {
		byte val = (byte) (Math.random() * 200);
		return val;
	}
}

class WindVane {

	private String unit = "Degrees";

	public byte getWindDirection() {
		byte val = (byte) (Math.random() * 360);
		return val;
	}
}

class Hygrometer {

	private String unit = "Absolute Humidity";

	public byte getHumidity() {
		byte val = (byte) (Math.random() * 100);
		return val;
	}
}

class Barometer {

	private String unit = "pascal";

	public double getPressure() {
		double val = (double) (Math.random() * 200);
		return val;
	}
}

// Needs to include
class Ceilometer {

	private String unit = "hectopascal";

	public double getCloudHeight() {
		double val = (double) (Math.random() * 50);
		return val;
	}
}

class VisibilitySensor {

	private String unit = "millimeters";

	public byte getVisibility() {
		byte val = (byte) (Math.random() * 100);
		return val;
	}
}

class RainGauge {

	private String unit = "millimeters";

	public byte getSolarRadiations() {
		byte val = (byte) (Math.random() * 100);
		return val;
	}
}

class UltrasonicSnowSensor {

	private String unit = "millimeters";

	public byte getSnowDepth() {
		byte val = (byte) (Math.random() * 100);
		return val;
	}
}

class Pyranometer {

	private String unit = "millimeters";

	public byte getPrecipitation() {
		byte val = (byte) (Math.random() * 100);
		return val;
	}
}

class Mast {

	private enum Height {
		TWO_Meter, THREE_Meter, TEN_Meter, THIRTY_Meter
	}

	private Height height;

	public Height height() {
		return height;
	}

	public int totalHeights() {
		return height.values().length;
	}
}
