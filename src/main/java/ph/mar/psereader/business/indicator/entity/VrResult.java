package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class VrResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "vr", nullable = false, precision = 3, scale = 2)
	private BigDecimal vr;

	@Column(name = "vr_volatility", nullable = false, length = 12)
	private VolatilityType volatility;

	@Column(name = "vr_smoothed_tr", nullable = false, precision = 13, scale = 10)
	private BigDecimal smoothedTr;

	public VrResult() {
		super();
	}

	public VrResult(BigDecimal vr, VolatilityType volatility, BigDecimal smoothedTr) {
		this.vr = vr;
		this.volatility = volatility;
		this.smoothedTr = smoothedTr;
	}

	public BigDecimal getVr() {
		return vr;
	}

	public VolatilityType getVolatility() {
		return volatility;
	}

	public BigDecimal getSmoothedTr() {
		return smoothedTr;
	}

	@Override
	public String toString() {
		return String.format("VrResult [vr=%s, volatility=%s, smoothedTr=%s]", vr, volatility, smoothedTr);
	}

}
