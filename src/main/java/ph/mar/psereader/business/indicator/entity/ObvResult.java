package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ObvResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false)
	private Long obv;

	public ObvResult() {
		super();
	}

	public ObvResult(Long obv) {
		this.obv = obv;
	}

	public Long getObv() {
		return obv;
	}

	@Override
	public String toString() {
		return String.format("ObvResult [obv=%s]", obv);
	}

}
