package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class MacdResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false, precision = 13, scale = 10)
	private BigDecimal macd;

	@Column(name = "macd_signal_line", nullable = false, precision = 13, scale = 10)
	private BigDecimal signalLine;

	@Column(name = "macd_histogram", nullable = false, precision = 13, scale = 10)
	private BigDecimal histogram;

	@Enumerated(EnumType.STRING)
	@Column(name = "macd_trend", nullable = false, length = 11)
	private TrendType trend;

	@Enumerated(EnumType.STRING)
	@Column(name = "macd_position", nullable = false, length = 12)
	private PositionType position;

	@Column(name = "macd_fast_ema", nullable = false, precision = 14, scale = 10)
	private BigDecimal fastEma;

	@Column(name = "macd_slow_ema", nullable = false, precision = 14, scale = 10)
	private BigDecimal slowEma;

	public MacdResult() {
		super();
	}

	public MacdResult(BigDecimal macd, BigDecimal signalLine, BigDecimal histogram, TrendType trend, PositionType position, BigDecimal fastEma,
			BigDecimal slowEma) {
		this.macd = macd;
		this.signalLine = signalLine;
		this.histogram = histogram;
		this.trend = trend;
		this.position = position;
		this.fastEma = fastEma;
		this.slowEma = slowEma;
	}

	public BigDecimal getMacd() {
		return macd;
	}

	public BigDecimal getSignalLine() {
		return signalLine;
	}

	public BigDecimal getHistogram() {
		return histogram;
	}

	public TrendType getTrend() {
		return trend;
	}

	public PositionType getPosition() {
		return position;
	}

	public BigDecimal getFastEma() {
		return fastEma;
	}

	public BigDecimal getSlowEma() {
		return slowEma;
	}

	@Override
	public String toString() {
		return String.format("MacdResult [macd=%s, signalLine=%s, histogram=%s, trend=%s, position=%s, fastEma=%s, slowEma=%s]", macd, signalLine,
				histogram, trend, position, fastEma, slowEma);
	}

}
