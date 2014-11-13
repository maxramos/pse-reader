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
	@Column(name = "ssto_reason", length = 18)
	private Reason reason;

	@Column(name = "ssto_fast_k", nullable = false, precision = 5, scale = 2)
	private BigDecimal fastK;

	@Column(name = "ssto_buy_stop", precision = 8, scale = 4)
	private BigDecimal buyStop;

	@Column(name = "ssto_sell_stop", precision = 8, scale = 4)
	private BigDecimal sellStop;

	@Column(name = "ssto_stop_loss", precision = 8, scale = 4)
	private BigDecimal stopLoss;

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

	public Reason getReason() {
		return reason;
	}

	public void setReason(Reason reason) {
		this.reason = reason;
	}

	public BigDecimal getFastK() {
		return fastK;
	}

	public BigDecimal getBuyStop() {
		return buyStop;
	}

	public void setBuyStop(BigDecimal buyStop) {
		this.buyStop = buyStop;
	}

	public BigDecimal getSellStop() {
		return sellStop;
	}

	public void setSellStop(BigDecimal sellStop) {
		this.sellStop = sellStop;
	}

	public BigDecimal getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(BigDecimal stopLoss) {
		this.stopLoss = stopLoss;
	}

	@Override
	public String toString() {
		return String.format("SstoResult [slowK=%s, slowD=%s, action=%s, reason=%s, fastK=%s, buyStop=%s, sellStop=%s, stopLoss=%s]", slowK, slowD,
				action, reason, fastK, buyStop, sellStop, stopLoss);
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

	public static enum Reason {

		BULLISH_DIP("Bullish Dip", "%K or %D falls below the Oversold level and rises back above it."),
		BEARISH_DIP("Bearish Dip", "%K or %D rises above the Overbought level then falls back below it."),
		BULLISH_CROSSOVER("Bullish Crossover", "%K crosses to above %D."),
		BEARISH_CROSSOVER("Bearish Crossover", "%K crosses to below %D."),
		OVERSOLD("Oversold", "%K or %D falls below the Oversold line."),
		OVERBOUGHT("Overbought", "%K or %D rises above the Overbought line.");

		private String name;
		private String description;

		private Reason(String name, String description) {
			this.name = name;
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

	}

}
