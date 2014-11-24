package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AtrResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false, precision = 13, scale = 10)
	private BigDecimal atr;

	public AtrResult() {
		super();
	}

	public AtrResult(BigDecimal atr) {
		this.atr = atr;
	}

	public BigDecimal getAtr() {
		return atr;
	}

	@Override
	public String toString() {
		return String.format("AtrResult [atr=%s]", atr);
	}

}
