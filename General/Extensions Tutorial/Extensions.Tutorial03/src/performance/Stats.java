package performance;

import org.apache.log4j.Logger;

public class Stats {

	Logger _logger = Logger.getLogger(Stats.class);
	private int completedConv = 0;
	private double avgTurnAroundTime = 0;
	private double totalTurnAroundTime = 0;
	private double maxTurnAroundTime = Integer.MIN_VALUE;
	private double minTurnAroundTime = Integer.MAX_VALUE;
	private double currentTurnAroundTime = 0;

	private int receives = 0;

	public int getReceives() {
		return receives;
	}

	public int getCompletedConv() {
		return completedConv;
	}

	public double getAvgTurnAroundTime() {
		return avgTurnAroundTime;
	}

	public double getTotalTurnAroundTime() {
		return totalTurnAroundTime;
	}

	public Stats(int completedConv, long avgAroundTime) {
		super();
		this.completedConv = completedConv;
		this.avgTurnAroundTime = avgAroundTime;
	}

	public void addAvgTurnAroundTime(double _turnAroundTime) {
		// _logger.debug("Stats : time "+_turnAroundTime);
		this.currentTurnAroundTime = _turnAroundTime;
		this.totalTurnAroundTime += _turnAroundTime;
		// _logger.debug("Stats : total turnaround time "+totalTurnAroundTime);
		if (_turnAroundTime < minTurnAroundTime)
			minTurnAroundTime = _turnAroundTime;
		// _logger.debug("Stats : MIN turnaround time "+minTurnAroundTime);
		if (_turnAroundTime > maxTurnAroundTime)
			maxTurnAroundTime = _turnAroundTime;
		// _logger.debug("Stats : MAX turnaround time "+maxTurnAroundTime);

		avgTurnAroundTime = ((completedConv * avgTurnAroundTime) + _turnAroundTime)
				/ (++completedConv);
		// _logger.debug("Stats : avgTurnAroundTime "+avgTurnAroundTime);
	}

	public double getMaxTurnAroundTime() {
		return maxTurnAroundTime;
	}

	public void setMaxTurnAroundTime(double maxTurnAroundTime) {
		this.maxTurnAroundTime = maxTurnAroundTime;
	}

	public double getMinTurnAroundTime() {
		return minTurnAroundTime;
	}

	public void setMinTurnAroundTime(double minTurnAroundTime) {
		this.minTurnAroundTime = minTurnAroundTime;
	}

	public double getCurrentTurnAroundTime() {
		return currentTurnAroundTime;
	}

	public void setCurrentTurnAroundTime(double currentTurnAroundTime) {
		this.currentTurnAroundTime = currentTurnAroundTime;
	}

}
