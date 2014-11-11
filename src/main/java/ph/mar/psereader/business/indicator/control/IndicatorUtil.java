package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import ph.mar.psereader.business.indicator.entity.SentimentType;
import ph.mar.psereader.business.indicator.entity.TrendType;
import ph.mar.psereader.business.indicator.entity.ValueHolder;
import ph.mar.psereader.business.stock.entity.Quote;

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

	public static <T> SentimentType divergence(List<Quote> quotes, List<ValueHolder> values) {
		if (values.size() < 5) {
			return SentimentType.RANGE;
		}

		List<Quote> trimmedQuotes = quotes.subList(0, 5);
		List<ValueHolder> trimmedValues = values.subList(0, 5);

		TrendType priceTrend = priceTrend(trimmedQuotes);
		TrendType indicatorTrend = indicatorTrend(trimmedValues);
		SentimentType sentiment;

		if (priceTrend == TrendType.DOWN && indicatorTrend == TrendType.UP) {
			sentiment = SentimentType.BULLISH;
		} else if (priceTrend == TrendType.UP && indicatorTrend == TrendType.DOWN) {
			sentiment = SentimentType.BEARISH;
		} else {
			sentiment = SentimentType.RANGE;
		}

		return sentiment;
	}

	private static TrendType priceTrend(List<Quote> values) {
		BigDecimal value0 = values.get(0).getClose();
		BigDecimal value1 = values.get(1).getClose();
		BigDecimal value2 = values.get(2).getClose();
		BigDecimal value3 = values.get(3).getClose();
		BigDecimal value4 = values.get(4).getClose();
		TrendType trend;

		if (value1.compareTo(value3) > 0 && value3.compareTo(value4) > 0 && value3.compareTo(value2) > 0 && value1.compareTo(value0) > 0) {
			trend = TrendType.UP;
		} else if (value1.compareTo(value3) < 0 && value3.compareTo(value4) < 0 && value3.compareTo(value2) < 0 && value1.compareTo(value0) < 0) {
			trend = TrendType.DOWN;
		} else {
			trend = TrendType.SIDEWAYS;
		}

		return trend;
	}

	private static TrendType indicatorTrend(List<ValueHolder> values) {
		BigDecimal value0 = values.get(0).getValue();
		BigDecimal value1 = values.get(1).getValue();
		BigDecimal value2 = values.get(2).getValue();
		BigDecimal value3 = values.get(3).getValue();
		BigDecimal value4 = values.get(4).getValue();
		TrendType trend;

		if (value1.compareTo(value3) < 0 && value3.subtract(value1).compareTo(BigDecimal.ONE) > 0 && value1.compareTo(value2) > 0
				&& value1.compareTo(value0) > 0 && value3.compareTo(value4) > 0) {
			trend = TrendType.DOWN;
		} else if (value1.compareTo(value3) > 0 && value1.subtract(value3).compareTo(BigDecimal.ONE) > 0 && value1.compareTo(value2) < 0
				&& value1.compareTo(value0) < 0 && value3.compareTo(value4) < 0) {
			trend = TrendType.UP;
		} else {
			trend = TrendType.SIDEWAYS;
		}

		return trend;
	}

}
