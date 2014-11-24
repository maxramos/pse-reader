package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_DATE, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.trend, ir.recommendation, ir.reason, ir.buyRisk, ir.sellRisk, ir.movement, ir.buyStop, ir.sellStop, ir.stopLoss, ir.atrResult, ir.sstoResult, ir.emaResult, ir.obvResult, ir.priceActionResult, ir.stock, q) FROM IndicatorResult ir, Quote q WHERE ir.stock = q.stock AND ir.date = :date AND q.date = :date ORDER BY ir.stock.symbol"),
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_DATE_AND_RECOMMENDATION, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.trend, ir.recommendation, ir.reason, ir.buyRisk, ir.sellRisk, ir.movement, ir.buyStop, ir.sellStop, ir.stopLoss, ir.atrResult, ir.sstoResult, ir.emaResult, ir.obvResult, ir.priceActionResult, ir.stock, q) FROM IndicatorResult ir, Quote q WHERE ir.stock = q.stock AND ir.date = :date AND q.date = :date AND ir.recommendation = :recommendation ORDER BY ir.stock.symbol"),
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.atrResult, ir.sstoResult, ir.emaResult, ir.obvResult, ir.priceActionResult) FROM IndicatorResult ir WHERE ir.stock = :stock ORDER BY ir.date DESC") })
public class IndicatorResult implements Serializable {

	public static final String ALL_BY_STOCK_AND_DATE = "IndicatorResult.ALL_BY_STOCK_AND_DATE";
	public static final String ALL_INDICATOR_DATA_BY_DATE = "IndicatorResult.ALL_INDICATOR_DATA_BY_DATE";
	public static final String ALL_INDICATOR_DATA_BY_DATE_AND_RECOMMENDATION = "IndicatorResult.ALL_INDICATOR_DATA_BY_DATE_AND_RECOMMENDATION";
	public static final String ALL_INDICATOR_DATA_BY_STOCK = "IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK";

	private static final long serialVersionUID = 1L;
	private static final BigDecimal BUY_DANGER_LEVEL = new BigDecimal("-0.08");
	private static final BigDecimal SELL_DANGER_LEVEL = new BigDecimal("0.08");
	private static final BigDecimal CRITICAL_LEVEL = BigDecimal.ZERO;
	private static final BigDecimal BUY_SIGNAL = new BigDecimal("30");
	private static final BigDecimal SELL_SIGNAL = new BigDecimal("70");

	@Id
	@SequenceGenerator(name = "seq_indicator_result", sequenceName = "seq_indicator_result", allocationSize = 1)
	@GeneratedValue(generator = "seq_indicator_result")
	private Long id;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date date;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 11)
	private TrendType trend;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 12)
	private RecommendationType recommendation;

	@Enumerated(EnumType.STRING)
	@Column(length = 18)
	private ReasonType reason;

	@Enumerated(EnumType.STRING)
	@Column(name = "buy_risk", length = 8)
	private RiskType buyRisk;

	@Enumerated(EnumType.STRING)
	@Column(name = "sell_risk", length = 8)
	private RiskType sellRisk;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private MovementType movement;

	@Column(name = "buy_stop", precision = 8, scale = 4)
	private BigDecimal buyStop;

	@Column(name = "sell_stop", precision = 8, scale = 4)
	private BigDecimal sellStop;

	@Column(name = "stop_loss", precision = 8, scale = 4)
	private BigDecimal stopLoss;

	@Embedded
	private AtrResult atrResult;

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
	public IndicatorResult(AtrResult atrResult, SstoResult sstoResult, EmaResult emaResult, ObvResult obvResult, PriceActionResult priceActionResult) {
		this.atrResult = atrResult;
		this.sstoResult = sstoResult;
		this.emaResult = emaResult;
		this.obvResult = obvResult;
		this.priceActionResult = priceActionResult;
	}

	/**
	 * Used for displaying indicator results.
	 */
	public IndicatorResult(TrendType trend, RecommendationType recommendation, ReasonType reason, RiskType buyRisk, RiskType sellRisk,
			MovementType movement, BigDecimal buyStop, BigDecimal sellStop, BigDecimal stopLoss, AtrResult atrResult, SstoResult sstoResult,
			EmaResult emaResult, ObvResult obvResult, PriceActionResult priceActionResult, Stock stock, Quote quote) {
		this.trend = trend;
		this.recommendation = recommendation;
		this.reason = reason;
		this.buyRisk = buyRisk;
		this.sellRisk = sellRisk;
		this.movement = movement;
		this.buyStop = buyStop;
		this.sellStop = sellStop;
		this.stopLoss = stopLoss;
		this.atrResult = atrResult;
		this.sstoResult = sstoResult;
		this.emaResult = emaResult;
		this.obvResult = obvResult;
		this.priceActionResult = priceActionResult;
		this.stock = stock;
		this.quote = quote;
	}

	public void process(List<Quote> quotes, List<IndicatorResult> results) {
		determineTrend();
		determineMovement();

		if (results.isEmpty()) {
			recommendation = RecommendationType.HOLD;
			return;
		}

		determineRecommendation(results);
		determineRisk();
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

	public RecommendationType getRecommendation() {
		return recommendation;
	}

	public ReasonType getReason() {
		return reason;
	}

	public RiskType getBuyRisk() {
		return buyRisk;
	}

	public RiskType getSellRisk() {
		return sellRisk;
	}

	public MovementType getMovement() {
		return movement;
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

	public AtrResult getAtrResult() {
		return atrResult;
	}

	public void setAtrResult(AtrResult atrResult) {
		this.atrResult = atrResult;
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
				.format("IndicatorResult [id=%s, date=%s, trend=%s, recommendation=%s, reason=%s, buyRisk=%s, sellRisk=%s, movement=%s, buyStop=%s, sellStop=%s, stopLoss=%s, atrResult=%s, sstoResult=%s, emaResult=%s, obvResult=%s, priceActionResult=%s, stock=%s]",
						id, date, trend, recommendation, reason, buyRisk, sellRisk, movement, buyStop, sellStop, stopLoss, atrResult, sstoResult,
						emaResult, obvResult, priceActionResult, stock == null ? null : stock.getId());
	}

	private void determineTrend() {
		// TODO
	}

	private void determineMovement() {
		BigDecimal priceChange = priceActionResult.getPriceChange();

		if (priceChange.compareTo(BigDecimal.ZERO) > 0) {
			movement = MovementType.GAIN;
		} else if (priceChange.compareTo(BigDecimal.ZERO) < 0) {
			movement = MovementType.LOSS;
		} else {
			movement = MovementType.NO_CHANGED;
		}
	}

	private void determineRecommendation(List<IndicatorResult> results) {
		BigDecimal k = sstoResult.getSlowK();
		BigDecimal d = sstoResult.getSlowD();

		if (trend == TrendType.UP || trend == TrendType.STRONG_UP) {
			BigDecimal ema = emaResult.getEma();
			BigDecimal previousEma = results.get(0).getEmaResult().getEma();

			if ((k.compareTo(SELL_SIGNAL) > 0 || d.compareTo(SELL_SIGNAL) > 0) && ema.compareTo(previousEma) < 0) {
				recommendation = RecommendationType.SELL;
				reason = ReasonType.OVERBOUGHT;
			} else {
				recommendation = RecommendationType.HOLD;
				reason = null;
			}
		} else if (trend == TrendType.DOWN || trend == TrendType.STRONG_DOWN) {
			BigDecimal ema = emaResult.getEma();
			BigDecimal previousEma = results.get(0).getEmaResult().getEma();

			if ((k.compareTo(BUY_SIGNAL) < 0 || d.compareTo(BUY_SIGNAL) < 0) && ema.compareTo(previousEma) > 0) {
				recommendation = RecommendationType.BUY;
				reason = ReasonType.OVERSOLD;
			} else {
				recommendation = RecommendationType.HOLD;
				reason = null;
			}
		} else {
			SstoResult previous1Ssto = results.get(0).getSstoResult();
			BigDecimal prevK1 = previous1Ssto.getSlowK();
			BigDecimal prevD1 = previous1Ssto.getSlowD();
			SstoResult previous2Ssto = results.size() >= 2 ? results.get(1).getSstoResult() : null;
			BigDecimal prevK2 = previous2Ssto == null ? null : previous2Ssto.getSlowK();
			BigDecimal prevD2 = previous2Ssto == null ? null : previous2Ssto.getSlowD();

			if (prevK2 != null && prevK2.compareTo(BUY_SIGNAL) > 0 && prevK1.compareTo(BUY_SIGNAL) < 0 && k.compareTo(BUY_SIGNAL) > 0
					|| prevD2 != null && prevD2.compareTo(BUY_SIGNAL) > 0 && prevD1.compareTo(BUY_SIGNAL) < 0 && d.compareTo(BUY_SIGNAL) > 0) {
				recommendation = RecommendationType.BUY;
				reason = ReasonType.BULLISH_DIP;
			} else if (prevK2 != null && prevK2.compareTo(SELL_SIGNAL) < 0 && prevK1.compareTo(SELL_SIGNAL) > 0 && k.compareTo(SELL_SIGNAL) < 0
					|| prevD2 != null && prevD2.compareTo(SELL_SIGNAL) < 0 && prevD1.compareTo(SELL_SIGNAL) > 0 && d.compareTo(SELL_SIGNAL) < 0) {
				recommendation = RecommendationType.SELL;
				reason = ReasonType.BEARISH_DIP;
			} else if (prevK1.compareTo(prevD1) < 0 && k.compareTo(d) > 0) {
				recommendation = RecommendationType.BUY;
				reason = ReasonType.BULLISH_CROSSOVER;
			} else if (prevK1.compareTo(prevD1) > 0 && k.compareTo(d) < 0) {
				recommendation = RecommendationType.SELL;
				reason = ReasonType.BEARISH_CROSSOVER;
			} else {
				recommendation = RecommendationType.HOLD;
				reason = null;
			}
		}
	}

	private void determineRisk() {

		if (recommendation == RecommendationType.BUY) {
			BigDecimal percentChangeFrom52WeekHigh = priceActionResult.getPercentChangeFrom52WeekHigh();

			if (percentChangeFrom52WeekHigh.compareTo(CRITICAL_LEVEL) == 0) {
				buyRisk = RiskType.CRITICAL;
			} else if (percentChangeFrom52WeekHigh.compareTo(BUY_DANGER_LEVEL) >= 0) {
				buyRisk = RiskType.DANGER;
			} else {
				buyRisk = RiskType.SAFE;
			}
		} else if (recommendation == RecommendationType.SELL) {
			BigDecimal percentChangeFrom52WeekLow = priceActionResult.getPercentChangeFrom52WeekLow();

			if (percentChangeFrom52WeekLow.compareTo(CRITICAL_LEVEL) == 0) {
				sellRisk = RiskType.CRITICAL;
			} else if (percentChangeFrom52WeekLow.compareTo(SELL_DANGER_LEVEL) <= 0) {
				sellRisk = RiskType.DANGER;
			} else {
				sellRisk = RiskType.SAFE;
			}
		}
	}

	private void determineTrailingStop(List<Quote> quotes, List<IndicatorResult> results) {
		if (recommendation == RecommendationType.BUY) {
			buyStop = buyStop(quotes, results);
			sellStop = null;
			stopLoss = stopLoss(quotes, results);
		} else if (recommendation == RecommendationType.SELL) {
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

		if (recommendation == RecommendationType.BUY) {
			BigDecimal currentLow = quotes.get(0).getLow();
			RecommendationType previousAction = results.get(0).getRecommendation();
			BigDecimal previousStopLoss = previousAction == RecommendationType.SELL ? null : results.get(0).getStopLoss();

			if (previousStopLoss == null) {
				_stopLoss = currentLow;
			} else {
				if (currentLow.compareTo(previousStopLoss) < 0) {
					_stopLoss = currentLow;
				} else {
					_stopLoss = previousStopLoss;
				}
			}
		} else if (recommendation == RecommendationType.SELL) {
			BigDecimal currentHigh = quotes.get(0).getHigh();
			RecommendationType previousAction = results.get(0).getRecommendation();
			BigDecimal previousStopLoss = previousAction == RecommendationType.BUY ? null : results.get(0).getStopLoss();

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
