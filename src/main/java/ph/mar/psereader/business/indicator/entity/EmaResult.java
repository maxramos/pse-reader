package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class EmaResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false, precision = 14, scale = 10)
	private BigDecimal ema;

	public EmaResult() {
		super();
	}

	public EmaResult(BigDecimal ema) {
		this.ema = ema;
	}

	public BigDecimal getEma() {
		return ema;
	}

	@Override
	public String toString() {
		return String.format("EmaResult [ema=%s]", ema);
	}

}
