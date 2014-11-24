package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class EmaResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false, precision = 14, scale = 10)
	private BigDecimal ema;

	@Transient
	private TrendType trend;

	@Transient
	private RecommendationType recommendation;

	public EmaResult() {
		super();
	}

	public EmaResult(BigDecimal ema) {
		this.ema = ema;
	}

	public BigDecimal getEma() {
		return ema;
	}

	public TrendType getTrend() {
		return trend;
	}

	public void setTrend(TrendType trend) {
		this.trend = trend;
	}

	public RecommendationType getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(RecommendationType recommendation) {
		this.recommendation = recommendation;
	}

	@Override
	public String toString() {
		return String.format("EmaResult [ema=%s]", ema);
	}

}
