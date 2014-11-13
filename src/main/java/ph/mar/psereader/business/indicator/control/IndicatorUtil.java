package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class IndicatorUtil {

	public static List<BigDecimal> sma(List<BigDecimal> values, int smoothing, int decimalPlaces) {
		if (smoothing >= values.size()) {
			return null;
		}

		List<BigDecimal> smaList = new ArrayList<>(smoothing);

		for (int i = 0, start = 0, end = smoothing; i < smoothing; i++, start++, end++) {
			BigDecimal avg = avg(values.subList(start, end), decimalPlaces);
			smaList.add(avg);
		}

		return smaList;
	}

	public static BigDecimal sma(BigDecimal previousAvg, BigDecimal currentVal, BigDecimal period, int decimalPlaces) {
		// (PREV_AVG * (PERIOD - 1) + CURRENT_VAL) / PERIOD
		return previousAvg.multiply(period.subtract(BigDecimal.ONE)).add(currentVal).divide(period, decimalPlaces, RoundingMode.HALF_UP);
	}

	public static List<BigDecimal> ema(List<BigDecimal> values, int period, BigDecimal factor, int decimalPlaces) {
		if (period >= values.size()) {
			return null;
		}

		List<BigDecimal> initialData = values.subList(0, period);
		List<BigDecimal> succeedingData = values.subList(period, values.size());

		List<BigDecimal> emaList = new ArrayList<>();
		BigDecimal avg = avg(initialData, decimalPlaces);
		emaList.add(avg);

		for (int i = 0; i < succeedingData.size(); i++) {
			avg = ema(avg, succeedingData.get(i), factor, decimalPlaces);
			emaList.add(avg);
		}

		return emaList;
	}

	public static BigDecimal ema(BigDecimal previousAvg, BigDecimal currentVal, BigDecimal factor, int decimalPlaces) {
		// (CURRENT_VAL - PREV_AVG) * FACTOR + PREV_AVG
		BigDecimal ema = currentVal.subtract(previousAvg).multiply(factor).add(previousAvg);
		return ema.divide(BigDecimal.ONE, decimalPlaces, RoundingMode.HALF_UP);
	}

	public static BigDecimal avg(List<BigDecimal> values, int decimalPlaces) {
		BigDecimal sum = BigDecimal.ZERO;

		for (BigDecimal value : values) {
			sum = sum.add(value);
		}

		return sum.divide(new BigDecimal(values.size()), decimalPlaces, RoundingMode.HALF_UP);
	}

}
