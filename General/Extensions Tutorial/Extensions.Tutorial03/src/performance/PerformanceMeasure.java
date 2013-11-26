package performance;

import org.apache.log4j.Logger;
import org.junit.Test;

public class PerformanceMeasure {
	Logger _logger = Logger.getLogger(PerformanceMeasure.class);
	private int windowSize = 5;
	private Stats[] statsList = new Stats[windowSize];
	private int currentStatIndex = 0;
	Stats currentStat = new Stats(0, 0);

	public void updateRollingStatsWindow(double newTurnAroundTime) {
		// _logger.debug("Current log time is "+ newTurnAroundTime);
		if (currentStatIndex >= windowSize) {
			// Reset the Rolling Window
			currentStatIndex = 0;
			currentStat = null;
		}

		if (currentStatIndex > 0)
			currentStat = statsList[currentStatIndex - 1];
		else if (currentStat == null && currentStatIndex == 0)
			currentStat = statsList[windowSize - 1];

		currentStat.addAvgTurnAroundTime(newTurnAroundTime);
		statsList[currentStatIndex] = currentStat;

		currentStatIndex++;
	}

	public Stats getCurrentStats() {
		return statsList[currentStatIndex - 1];
	}

	public String printCurrentStats() {
		Stats curStats = getCurrentStats();
		String str = "================== Stats for Performance Measure ========================\n";
		str += " Total Receives " + curStats.getCompletedConv();
		str += "\navg Turnaround Time for conv. "
				+ curStats.getAvgTurnAroundTime()
				+ "  Overall total time for conv "
				+ curStats.getTotalTurnAroundTime();
		str += "\ncurrent Turnaround Time : "
				+ curStats.getCurrentTurnAroundTime()
				+ " avg Minimum Turnaround Time for conv. "
				+ curStats.getMinTurnAroundTime()
				+ "  Maximum Turnaround time for conv "
				+ curStats.getMaxTurnAroundTime();
		return str;
	}

	@Test
	public void testUpdateRollingWindowStats() {
		long[] turnAroundTimes = new long[] { 2, 1, 3, 6, 1, 3, 6, 1, 1, 2, 4,3 };

		for (int i = 0; i < 12; i++) {
			updateRollingStatsWindow(turnAroundTimes[i]);
		}
		System.out.println(printCurrentStats());
	}
}
