package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class SstoResult implements Serializable, ValueHolder {

	private static final long serialVersionUID = 1L;

	@Column(name = "ssto_slow_k", nullable = false, precision = 5, scale = 2)
	private BigDecimal slowK;

	@Column(name = "ssto_slow_d", nullable = false, precision = 5, scale = 2)
	private BigDecimal slowD;

	@Enumerated(EnumType.STRING)
	@Column(name = "ssto_action", nullable = false, length = 12)
	private ActionType action;

	@Enumerated(EnumType.STRING)
	@Column(name = "ssto_signal", nullable = false, length = 18)
	private String signal;

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

	@Override
	public BigDecimal getValue() {
		return slowD;
	}

	public BigDecimal getSlowK() {
		return slowK;
	}

	public BigDecimal getSlowD() {
		return slowD;
	}

	public ActionType getAction() {
		return action;
	}

	public void setAction(ActionType action) {
		this.action = action;
	}

	public String getSignal() {
		return signal;
	}

	public void setSignal(String signal) {
		this.signal = signal;
	}

	public BigDecimal getFastK() {
		return fastK;
	}

	@Override
	public String toString() {
		return String.format("SstoResult [slowK=%s, slowD=%s, action=%s, signal=%s, fastK=%s]", slowK, slowD, action, signal, fastK);
	}

	public static class Holder {

		private BigDecimal lastClosing;
		private BigDecimal lowestLow;
		private BigDecimal highestHigh;

		public Holder(BigDecimal lastClosing, BigDecimal lowestLow, BigDecimal highestHigh) {
			this.lastClosing = lastClosing;
			this.lowestLow = lowestLow;
			this.highestHigh = highestHigh;
		}

		public BigDecimal getLastClosing() {
			return lastClosing;
		}

		public BigDecimal getLowestLow() {
			return lowestLow;
		}

		public BigDecimal getHighestHigh() {
			return highestHigh;
		}

	}

}
