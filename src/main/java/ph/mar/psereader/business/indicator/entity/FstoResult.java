package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class FstoResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "fsto_fast_k", nullable = false, precision = 5, scale = 2)
	private BigDecimal fastK;

	@Column(name = "fsto_fast_d", nullable = false, precision = 5, scale = 2)
	private BigDecimal fastD;

	public FstoResult() {
		super();
	}

	public FstoResult(BigDecimal fastK, BigDecimal fastD) {
		this.fastK = fastK;
		this.fastD = fastD;
	}

	public BigDecimal getFastK() {
		return fastK;
	}

	public BigDecimal getFastD() {
		return fastD;
	}

	@Override
	public String toString() {
		return String.format("FstoResult [fastK=%s, fastD=%s]", fastK, fastD);
	}

}
