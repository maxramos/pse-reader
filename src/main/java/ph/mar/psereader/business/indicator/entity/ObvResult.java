package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ObvResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false)
	private Long obv;

	@Column(nullable = false, length = 9)
	private MovementType movement;

	public ObvResult() {
		super();
	}

	public ObvResult(Long obv, MovementType movement) {
		this.obv = obv;
		this.movement = movement;
	}

	public Long getObv() {
		return obv;
	}

	public MovementType getMovement() {
		return movement;
	}

	@Override
	public String toString() {
		return String.format("ObvResult [obv=%s, movement=%s]", obv, movement);
	}

}
