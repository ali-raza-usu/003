package utilities.messages;

import java.io.Serializable;
import java.util.Date;

import utilities.messages.WeatherDataVector.LocType;
import utilities.messages.WeatherDataVector.ObservationType;


public class WeatherDataReading implements Serializable {

	private static final long serialVersionUID = 1L;
	private byte windSpeed; // Miles per hour
	private byte windDirection;// Degree
	private byte humidity;// Grams per cubic meter
	private double precipitation;// Cubic Kilometer
	private LocType facilityLocType;
	private double pressure;// lb
	private double cloudHeight;
	private byte visibility;
	private byte solarRadiations;
	private byte snowDepth;
	private ObservationType obsType;
	private short temperature;// celcius
	private Date _sendTime;// time when packet was sent
	private int size;// Not sure?
	private int resolution; // Not sure?

	public WeatherDataReading() {
	}

	public WeatherDataReading(byte windSpeed, byte windDirection,
			byte humidity, double precipitation, LocType facilityLocType,
			double pressure, double cloudHeight, byte visibility,
			byte solarRadiations, byte snowDepth, ObservationType obsType,
			short temperature, Date _sendTime, int size, int resolution) {
		super();
		this.windSpeed = windSpeed;
		this.windDirection = windDirection;
		this.humidity = humidity;
		this.precipitation = precipitation;
		this.facilityLocType = facilityLocType;
		this.pressure = pressure;
		this.cloudHeight = cloudHeight;
		this.visibility = visibility;
		this.solarRadiations = solarRadiations;
		this.snowDepth = snowDepth;
		this.obsType = obsType;
		this.temperature = temperature;
		this._sendTime = _sendTime;
		this.size = size;
		this.resolution = resolution;
	}

	public String printReading() {
		return " wind speed : " + windSpeed + " : wind direction : "
				+ windDirection + " : humidity : " + humidity
				+ " : precipitaiton : " + precipitation + " : facility type "
				+ facilityLocType.name() + " : pressure " + pressure
				+ " : cloud height " + cloudHeight + " : visibility : "
				+ visibility + " : solar radiations " + solarRadiations
				+ " : snow depth " + snowDepth + " : obs type "
				+ obsType.name() + " : temperature " + temperature
				+ " : send time " + _sendTime + " : size " + size
				+ " : resolution " + resolution;
	}

	public byte getSpeed() {
		return windSpeed;
	}

	public void setSpeed(byte speed) {
		this.windSpeed = speed;
	}

	public short getTemperature() {
		return temperature;
	}

	public void setTemperature(short temperature) {
		this.temperature = temperature;
	}

	public Date getSendTime() {
		return _sendTime;
	}

	public void setSendTime(Date _sendTime) {
		this._sendTime = _sendTime;
	}

	public byte getWindDirection() {
		return windDirection;
	}

	public void setWindDirection(byte windDirection) {
		this.windDirection = windDirection;
	}

	public byte getHumidity() {
		return humidity;
	}

	public void setHumidity(byte humidity) {
		this.humidity = humidity;
	}

	public double getPrecipitation() {
		return precipitation;
	}

	public void setPrecipitation(double precipitation) {
		this.precipitation = precipitation;
	}

	public LocType getFacilityLocType() {
		return facilityLocType;
	}

	public void setFacilityLocType(LocType facilityLocType) {
		this.facilityLocType = facilityLocType;
	}

	public double getPressure() {
		return pressure;
	}

	public void setPressure(double pressure) {
		this.pressure = pressure;
	}

	public ObservationType getObsType() {
		return obsType;
	}

	public void setObsType(ObservationType obsType) {
		this.obsType = obsType;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getResolution() {
		return resolution;
	}

	public void setResolution(int resolution) {
		this.resolution = resolution;
	}

	public double getCloudHeight() {
		return cloudHeight;
	}

	public void setCloudHeight(double cloudHeight) {
		this.cloudHeight = cloudHeight;
	}

	public byte getVisibility() {
		return visibility;
	}

	public void setVisibility(byte visibility) {
		this.visibility = visibility;
	}

	public byte getSolarRadiations() {
		return solarRadiations;
	}

	public void setSolarRadiations(byte solarRadiations) {
		this.solarRadiations = solarRadiations;
	}

	public byte getSnowDepth() {
		return snowDepth;
	}

	public void setSnowDepth(byte snowDepth) {
		this.snowDepth = snowDepth;
	}
}
