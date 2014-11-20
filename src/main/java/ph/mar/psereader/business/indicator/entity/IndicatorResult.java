package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import ph.mar.psereader.business.stock.entity.Quote;
import ph.mar.psereader.business.stock.entity.Stock;

@Entity
@Table(name = "indicator_result", uniqueConstraints = @UniqueConstraint(columnNames = { "date", "stock_id" }), indexes = @Index(columnList = "stock_id,date"))
@NamedQueries({
	@NamedQuery(name = IndicatorResult.ALL_BY_STOCK_AND_DATE, query = "SELECT ir FROM IndicatorResult ir WHERE ir.stock = :stock AND ir.date <= :date ORDER BY ir.date DESC"),
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_DATE, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.trend, ir.action, ir.reason, ir.buyStop, ir.sellStop, ir.stopLoss, ir.dmiResult, ir.sstoResult, ir.emaResult, ir.obvResult, ir.priceActionResult, ir.stock, q) FROM IndicatorResult ir, Quote q WHERE ir.stock = q.stock AND ir.date = :date AND q.date = :date ORDER BY ir.stock.symbol"),
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_DATE_AND_ACTION, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.trend, ir.action, ir.reason, ir.buyStop, ir.sellStop, ir.stopLoss, ir.dmiResult, ir.sstoResult, ir.emaResult, ir.obvResult, ir.priceActionResult, ir.stock, q) FROM IndicatorResult ir, Quote q WHERE ir.stock = q.stock AND ir.date = :date AND q.date = :date AND ir.action = :action ORDER BY ir.stock.symbol"),
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.dmiResult, ir.sstoResult, ir.emaResult, ir.obvResult, ir.priceActionResult) FROM IndicatorResult ir WHERE ir.stock = :stock ORDER BY ir.date DESC") })
public class IndicatorResult implements Serializable {

	public static final String ALL_BY_STOCK_AND_DATE = "IndicatorResult.ALL_BY_STOCK_AND_DATE";
	public static final String ALL_INDICATOR_DATA_BY_DATE = "IndicatorResult.ALL_INDICATOR_DATA_BY_DATE";
	public static final String ALL_INDICATOR_DATA_BY_DATE_AND_ACTION = "IndicatorResult.ALL_INDICATOR_DATA_BY_DATE_AND_ACTION";
	public static final String ALL_INDICATOR_DATA_BY_STOCK = "IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK";

	private static final long serialVersionUID = 1L;
	private static final BigDecimal TREND_SIGNAL = new BigDecimal("20");
	private static final BigDecimal BUY = new BigDecimal("30");
	private static final BigDecimal SELL = new BigDecimal("70");

	@Id
	@SequenceGenerator(name = "seq_indicator_result", sequenceName = "seq_indicator_result", allocationSize = 1)
	@GeneratedValue(generator = "seq_indicator_result")
	private Long id;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date date;

	@Enumerated(EnumType.STRING)
	@Column(name = "trend", nullable = false, length = 11)
	private TrendType trend;

	@Enumerated(EnumType.STRING)
	@Column(name = "action", nullable = false, length = 12)
	private ActionType action;

	@Enumerated(EnumType.STRING)
	@Column(name = "reason", length = 18)
	private ReasonType reason;

	@Column(name = "buy_stop", precision = 8, scale = 4)
	private BigDecimal buyStop;

	@Column(name = "sell_stop", precision = 8, scale = 4)
	private BigDecimal sellStop;

	@Column(name = "stop_loss", precision = 8, scale = 4)
	private BigDecimal stopLoss;

	@Embedded
	private DmiResult dmiResult;

	@Embedded
	private SstoResult sstoResult;

	@Embedded
	private EmaResult emaResult;

	@Embedded
	private ObvResult obvResult;

	@Embedded
	private PriceActionResult priceActionResult;

	@ManyToOne
	private Stock stock;

	@Transient
	private Quote quote;

	public IndicatorResult() {
		super();
	}

	public IndicatorResult(Stock stock, Date date) {
		this.stock = stock;
		this.date = date;
	}

	/**
	 * Used for processing indicator results.
	 */
	public IndicatorResult(DmiResult dmiResult, SstoResult sstoResult, EmaResult emaResult, ObvResult obvResult, PriceActionResult priceActionResult) {
		this.dmiResult = dmiResult;
		this.sstoResult = sstoResult;
		this.emaResult = emaResult;
		this.obvResult = obvResult;
		this.priceActionResult = priceActionResult;
	}

	/**
	 * Used for displaying indicator results.
	 */
	public IndicatorResult(TrendType trend, ActionType action, ReasonType reason, BigDecimal buyStop, BigDecimal sellStop, BigDecimal stopLoss,
			DmiResult dmiResult, SstoResult sstoResult, EmaResult emaResult, ObvResult obvResult, PriceActionResult priceActionResult, Stock stock,
			Quote quote) {
		this.trend = trend;
		this.action = action;
		this.reason = reason;
		this.buyStop = buyStop;
		this.sellStop = sellStop;
		this.stopLoss = stopLoss;
		this.dmiResult = dmiResult;
		this.sstoResult = sstoResult;
		this.emaResult = emaResult;
		this.obvResult = obvResult;
		this.priceActionResult = priceActionResult;
		this.stock = stock;
		this.quote = quote;
	}

	public void process(List<Quote> quotes, List<IndicatorResult> results) {
		determineTrend();

		if (results.isEmpty()) {
			action = ActionType.HOLD;
			reason = null;
			buyStop = null;
			sellStop = null;
			stopLoss = null;
			return;
		}

		determineAction(results);
		determineTrailingStop(quotes, results);
	}

	public Long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public TrendType getTrend() {
		return trend;
	}

	public ActionType getAction() {
		return action;
	}

	public ReasonType getReason() {
		return reason;
	}

	public BigDecimal getBuyStop() {
		return buyStop;
	}

	public BigDecimal getSellStop() {
		return sellStop;
	}

	public BigDecimal getStopLoss() {
		return stopLoss;
	}

	public DmiResult getDmiResult() {
		return dmiResult;
	}

	public void setDmiResult(DmiResult dmiResult) {
		this.dmiResult = dmiResult;
	}

	public SstoResult getSstoResult() {
		return sstoResult;
	}

	public void setSstoResult(SstoResult sstoResult) {
		this.sstoResult = sstoResult;
	}

	public EmaResult getEmaResult() {
		return emaResult;
	}

	public void setEmaResult(EmaResult emaResult) {
		this.emaResult = emaResult;
	}

	public ObvResult getObvResult() {
		return obvResult;
	}

	public void setObvResult(ObvResult obvResult) {
		this.obvResult = obvResult;
	}

	public PriceActionResult getPriceActionResult() {
		return priceActionResult;
	}

	public void setPriceActionResult(PriceActionResult priceActionResult) {
		this.priceActionResult = priceActionResult;
	}

	public Stock getStock() {
		return stock;
	}

	public Quote getQuote() {
		return quote;
	}

	@Override
	public String toString() {
		return String
				.format("IndicatorResult [id=%s, date=%s, trend=%s, action=%s, reason=%s, buyStop=%s, sellStop=%s, stopLoss=%s, dmiResult=%s, sstoResult=%s, emaResult=%s, obvResult=%s, priceActionResult=%s, stock=%s]",
						id, date, trend, action, reason, buyStop, sellStop, stopLoss, dmiResult, sstoResult, emaResult, obvResult, priceActionResult,
						stock == null ? null : stock.getId());
	}

	private void determineTrend() {
		BigDecimal adx = dmiResult.getAdx();
		BigDecimal plusDi = dmiResult.getPlusDi();
		BigDecimal minusDi = dmiResult.getMinusDi();

		BigDecimal _adx = adx.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
		BigDecimal _plusDi = plusDi.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
		BigDecimal _minusDi = minusDi.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);

		if (_adx.compareTo(TREND_SIGNAL) > 0) {
			trend = _plusDi.compareTo(_minusDi) > 0 ? TrendType.UP : TrendType.DOWN;
		} else {
			trend = TrendType.SIDEWAYS;
		}
	}

	private void determineAction(List<IndicatorResult> results) {
		BigDecimal k = sstoResult.getSlowK();
		BigDecimal d = sstoResult.getSlowD();

		if (trend == TrendType.UP) {
			BigDecimal ema = emaResult.getEma();
			BigDecimal previousEma = results.get(0).getEmaResult().getEma();

			if ((k.compareTo(SELL) > 0 || d.compareTo(SELL) > 0) && ema.compareTo(previousEma) < 0) {
				action = ActionType.SELL;
				reason = ReasonType.OVERBOUGHT;
			} else {
				action = ActionType.HOLD;
				reason = null;
			}
		} else if (trend == TrendType.DOWN) {
			BigDecimal ema = emaResult.getEma();
			BigDecimal previousEma = results.get(0).getEmaResult().getEma();

			if ((k.compareTo(BUY) < 0 || d.compareTo(BUY) < 0) && ema.compareTo(previousEma) > 0) {
				action = ActionType.BUY;
				reason = ReasonType.OVERSOLD;
			} else {
				action = ActionType.HOLD;
				reason = null;
			}
		} else {
			SstoResult previous1Ssto = results.get(0).getSstoResult();
			BigDecimal prevK1 = previous1Ssto.getSlowK();
			BigDecimal prevD1 = previous1Ssto.getSlowD();
			SstoResult previous2Ssto = results.size() >= 2 ? results.get(1).getSstoResult() : null;
			BigDecimal prevK2 = previous2Ssto == null ? null : previous2Ssto.getSlowK();
			BigDecimal prevD2 = previous2Ssto == null ? null : previous2Ssto.getSlowD();

			if (prevK2 != null && prevK2.compareTo(BUY) > 0 && prevK1.compareTo(BUY) < 0 && k.compareTo(BUY) > 0 || prevD2 != null
					&& prevD2.compareTo(BUY) > 0 && prevD1.compareTo(BUY) < 0 && d.compareTo(BUY) > 0) {
				action = ActionType.BUY;
				reason = ReasonType.BULLISH_DIP;
			} else if (prevK2 != null && prevK2.compareTo(SELL) < 0 && prevK1.compareTo(SELL) > 0 && k.compareTo(SELL) < 0 || prevD2 != null
					&& prevD2.compareTo(SELL) < 0 && prevD1.compareTo(SELL) > 0 && d.compareTo(SELL) < 0) {
				action = ActionType.SELL;
				reason = ReasonType.BEARISH_DIP;
			} else if (prevK1.compareTo(prevD1) < 0 && k.compareTo(d) > 0) {
				action = ActionType.BUY;
				reason = ReasonType.BULLISH_CROSSOVER;
			} else if (prevK1.compareTo(prevD1) > 0 && k.compareTo(d) < 0) {
				action = ActionType.SELL;
				reason = ReasonType.BEARISH_CROSSOVER;
			} else {
				action = ActionType.HOLD;
				reason = null;
			}
		}
	}

	private void determineTrailingStop(List<Quote> quotes, List<IndicatorResult> results) {
		if (action == ActionType.BUY) {
			buyStop = buyStop(quotes, results);
			sellStop = null;
			stopLoss = stopLoss(quotes, results);
		} else if (action == ActionType.SELL) {
			buyStop = null;
			sellStop = sellStop(quotes, results);
			stopLoss = stopLoss(quotes, results);
		} else {
			buyStop = null;
			sellStop = null;
			stopLoss = null;
		}
	}

	private BigDecimal buyStop(List<Quote> quotes, List<IndicatorResult> results) {
		BigDecimal currentHigh = quotes.get(0).getHigh();
		BigDecimal previousBuyStop = results.get(0).getBuyStop();
		BigDecimal _buyStop;

		if (previousBuyStop == null) {
			BigDecimal priceFluctuation = BoardLotAndPriceFluctuation.determinePriceFluctuation(currentHigh);
			_buyStop = currentHigh.add(priceFluctuation);
		} else {
			BigDecimal previousHigh = quotes.get(1).getHigh();

			if (currentHigh.compareTo(previousHigh) < 0) {
				BigDecimal priceFluctuation = BoardLotAndPriceFluctuation.determinePriceFluctuation(currentHigh);
				_buyStop = currentHigh.add(priceFluctuation);
			} else {
				_buyStop = previousBuyStop;
			}
		}

		return _buyStop;
	}

	private BigDecimal sellStop(List<Quote> quotes, List<IndicatorResult> results) {
		BigDecimal currentLow = quotes.get(0).getLow();
		BigDecimal previousSellStop = results.get(0).getSellStop();
		BigDecimal _sellStop;

		if (previousSellStop == null) {
			BigDecimal priceFluctuation = BoardLotAndPriceFluctuation.determinePriceFluctuation(currentLow);
			_sellStop = currentLow.subtract(priceFluctuation);
		} else {
			BigDecimal previousLow = quotes.get(1).getLow();

			if (currentLow.compareTo(previousLow) > 0) {
				BigDecimal priceFluctuation = BoardLotAndPriceFluctuation.determinePriceFluctuation(currentLow);
				_sellStop = currentLow.subtract(priceFluctuation);
			} else {
				_sellStop = previousSellStop;
			}
		}

		return _sellStop;
	}

	private BigDecimal stopLoss(List<Quote> quotes, List<IndicatorResult> results) {
		BigDecimal _stopLoss;

		if (action == ActionType.BUY) {
			BigDecimal currentLow = quotes.get(0).getLow();
			ActionType previousAction = results.get(0).getAction();
			BigDecimal previousStopLoss = previousAction == ActionType.SELL ? null : results.get(0).getStopLoss();

			if (previousStopLoss == null) {
				_stopLoss = currentLow;
			} else {
				if (currentLow.compareTo(previousStopLoss) < 0) {
					_stopLoss = currentLow;
				} else {
					_stopLoss = previousStopLoss;
				}
			}
		} else if (action == ActionType.SELL) {
			BigDecimal currentHigh = quotes.get(0).getHigh();
			ActionType previousAction = results.get(0).getAction();
			BigDecimal previousStopLoss = previousAction == ActionType.BUY ? null : results.get(0).getStopLoss();

			if (previousStopLoss == null) {
				_stopLoss = currentHigh;
			} else {
				if (currentHigh.compareTo(previousStopLoss) > 0) {
					_stopLoss = currentHigh;
				} else {
					_stopLoss = previousStopLoss;
				}
			}
		} else {
			_stopLoss = null;
		}

		return _stopLoss;
	}

}
