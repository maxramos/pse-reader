package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SstoResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "ssto_slow_k", nullable = false, precision = 5, scale = 2)
	private BigDecimal slowK;

	@Column(name = "ssto_slow_d", nullable = false, precision = 5, scale = 2)
	private BigDecimal slowD;

	@Column(name = "ssto_fast_k", nullable = false, precision = 5, scale = 2)
	private BigDecimal fastK;

	public SstoResult() {
		super();
	}

	public SstoResult(BigDecimal slowK, BigDecimal slowD, BigDecimal fastK) {
		this.slowK = slowK;
		this.slowD = slowD;
		this.fastK = fastK;
	}

	public BigDecimal getSlowK() {
		return slowK;
	}

	public BigDecimal getSlowD() {
		return slowD;
	}

	public BigDecimal getFastK() {
		return fastK;
	}

	@Override
	public String toString() {
		return String.format("SstoResult [slowK=%s, slowD=%s, fastK=%s]", slowK, slowD, fastK);
	}

}
