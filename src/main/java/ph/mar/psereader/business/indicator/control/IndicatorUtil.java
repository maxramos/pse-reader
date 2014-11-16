package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class IndicatorUtil {

	public static BigDecimal avg(List<BigDecimal> values, int decimalPlaces) {
		BigDecimal sum = BigDecimal.ZERO;

		for (BigDecimal value : values) {
			sum = sum.add(value);
		}

		return sum.divide(new BigDecimal(values.size()), decimalPlaces, RoundingMode.HALF_UP);
	}

}
