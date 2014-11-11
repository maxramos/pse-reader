package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class RsiResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "rsi", nullable = false, precision = 5, scale = 2)
	private BigDecimal rsi;

	@Enumerated(EnumType.STRING)
	@Column(name = "rsi_action", nullable = false, length = 12)
	private ActionType action;

	@Column(name = "rsi_avg_gain", nullable = false, precision = 13, scale = 10)
	private BigDecimal avgGain;

	@Column(name = "rsi_avg_loss", nullable = false, precision = 13, scale = 10)
	private BigDecimal avgLoss;

	public RsiResult() {
		super();
	}

	public RsiResult(BigDecimal rsi, ActionType action, BigDecimal avgGain, BigDecimal avgLoss) {
		this.rsi = rsi;
		this.action = action;
		this.avgGain = avgGain;
		this.avgLoss = avgLoss;
	}

	public BigDecimal getRsi() {
		return rsi;
	}

	public ActionType getAction() {
		return action;
	}

	public BigDecimal getAvgGain() {
		return avgGain;
	}

	public BigDecimal getAvgLoss() {
		return avgLoss;
	}

	@Override
	public String toString() {
		return String.format("RsiResult [rsi=%s, action=%s, avgGain=%s, avgLoss=%s]", rsi, action, avgGain, avgLoss);
	}

	public static class Holder {

		private List<BigDecimal> gains;
		private List<BigDecimal> losses;

		public Holder(int size) {
			gains = new ArrayList<>(size);
			losses = new ArrayList<>(size);
		}

		public void add(BigDecimal gain, BigDecimal loss) {
			gains.add(gain);
			losses.add(loss);
		}

		public List<BigDecimal> getGains() {
			return gains;
		}

		public List<BigDecimal> getLosses() {
			return losses;
		}

	}

}
