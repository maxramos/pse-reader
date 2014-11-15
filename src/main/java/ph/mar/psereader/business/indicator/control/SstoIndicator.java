package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.ActionType;
import ph.mar.psereader.business.indicator.entity.BoardLotAndPriceFluctuations;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.SstoResult;
import ph.mar.psereader.business.indicator.entity.TrendType;
import ph.mar.psereader.business.indicator.entity.ValueHolder;
import ph.mar.psereader.business.stock.entity.Quote;

/**
 * This implements the Slow Stochastic (SSto).
 *
 * Computations:
 *   n = look-back period (14)
 *   m = smoothing period (3)
 *
 *   FAST_%K = (LAST_CLOSING - LOWEST_LOW(n)) / (HIGHEST_HIGH(n) - LOWEST_LOW(n)) * 100
 *   FAST_%D = SMAm(FAST_%K)
 *
 *   SLOW_%K = FAST_%D
 *   SLOW_%D = SMAm(SLOW_%K)
 *
 *
 * Actions:
 *   Trending:
 *     BUY:
 *       OVERSOLD --- DOWNTREND && (%K or %D) < 20
 *     SELL:
 *       OVERBOUGHT --- UPTREND && (%K or %D) > 80
 *
 *   Ranging:
 *     BUY:
 *       BULLISH_DIVERGENCE (n/a, See Chart) --- DOWNTREND(PRICE) && UPTREND(%D) && FIRST_TROUGH < 20
 *       BULLISH_DIP --- PREV_%K2 > 20 && PREV_%K1 < 20 && %K > 20 || PREV_%D2 > 20 && PREV_%D1 < 20 && %D > 20
 *       BULLISH_CROSSOVER --- PREV_%K < PREV_%D && %K > %D
 *     SELL:
 *       BEARISH_DIVERGENCE (n/a, See Chart) --- UPTREND(PRICE) && DOWNTREND(%D) && FIRST_PEAK > 80
 *       BEARISH_DIP --- PREV_%K2 < 80 && PREV_%K1 > 80 && %K < 80 || PREV_%D2 < 80 && PREV_%D1 > 80 && %D < 80
 *       BEARISH_CROSSOVER --- PREV_%K > PREV_%D && %K < %D
 *
 *
 * Trailing Stop Loss:
 *   Buy:
 *     BUY_STOP --- if CURRENT_HIGH < PREVIOUS_HIGH ? CURRENT_HIGH + PRICE_FLUCTUATION(CURRENT_HIGH) : PREVIOUS_BUY_STOP
 *     STOP_LOSS --- CURRENT_LOW < PREV_STOP_LOSS ? CURRENT_LOW : PREV_STOP_LOSS
 *
 *   Sell:
 *     SELL_STOP --- if CURRENT_LOW > PREVIOUS_LOW ? CURRENT_LOW - PRICE_FLUCTUATION(CURRENT_LOW) : PREVIOUS_SELL_STOP
 *     STOP_LOSS --- CURRENT_HIGH > PREV_STOP_LOSS ? CURRENT_HIGH : PREV_STOP_LOSS
 */
public class SstoIndicator implements Callable<SstoResult> {

	private static final BigDecimal _100 = new BigDecimal("100");
	private static final BigDecimal BUY = new BigDecimal("20");
	private static final BigDecimal SELL = new BigDecimal("100");

	private static final int PERIOD = 14;
	private static final int SMOOTHING = 3;

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;
	private SstoResult result;
	private TrendType trend;

	public SstoIndicator(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public SstoResult call() throws Exception {
		if (result == null) {
			result = _results.size() < 2 ? initialSsto() : succeedingSsto();
		} else {
			result = determineTradingSignal();
		}

		return result;
	}

	public void setTrend(TrendType trend) {
		this.trend = trend;
	}

	private SstoResult initialSsto() {
		int size = PERIOD + SMOOTHING * 2 - 1 - 1; // 18
		List<Quote> trimmedQuotes = _quotes.subList(0, size);

		List<SstoResult.Holder> kDataList = kData(trimmedQuotes);
		List<BigDecimal> fastKList = fastK(kDataList);
		List<BigDecimal> fastDList = IndicatorUtil.sma(fastKList, SMOOTHING, 2);

		BigDecimal slowK = fastDList.get(0);
		BigDecimal slowD = IndicatorUtil.avg(fastDList, 2);
		BigDecimal fastK = fastKList.get(0);

		return new SstoResult(slowK, slowD, fastK);
	}

	private SstoResult succeedingSsto() {
		int size = PERIOD;
		List<Quote> trimmedQuotes = _quotes.subList(0, size);
		Quote currentQuote = trimmedQuotes.get(0);
		SstoResult previous1SstoResult = _results.get(0).getSstoResult();
		SstoResult previous2SstoResult = _results.get(1).getSstoResult();

		BigDecimal fastK = fastK(currentQuote.getClose(), lowestLow(trimmedQuotes), highestHigh(trimmedQuotes));
		List<BigDecimal> fastKList = Arrays.asList(new BigDecimal[] { fastK, previous1SstoResult.getFastK(), previous2SstoResult.getFastK() });
		BigDecimal slowK = IndicatorUtil.avg(fastKList, 2);
		List<BigDecimal> fastDList = Arrays.asList(new BigDecimal[] { slowK, previous1SstoResult.getSlowK(), previous2SstoResult.getSlowK() });
		BigDecimal slowD = IndicatorUtil.avg(fastDList, 2);

		return new SstoResult(slowK, slowD, fastK);
	}

	private List<SstoResult.Holder> kData(List<Quote> quotes) {
		int size = SMOOTHING * 2 - 1;
		List<SstoResult.Holder> kDataList = new ArrayList<>(size);

		for (int i = 0, start = 0, end = PERIOD; i < size; i++, start++, end++) {
			List<Quote> period = quotes.subList(start, end);
			BigDecimal lastClosing = period.get(0).getClose();
			BigDecimal lowestLow = lowestLow(period);
			BigDecimal higestHigh = highestHigh(period);
			SstoResult.Holder data = new SstoResult.Holder(lastClosing, lowestLow, higestHigh);
			kDataList.add(data);
		}

		return kDataList;
	}

	private List<BigDecimal> fastK(List<SstoResult.Holder> dataList) {
		List<BigDecimal> fastKList = new ArrayList<>(dataList.size());

		for (SstoResult.Holder data : dataList) {
			BigDecimal lastClosing = data.getLastClosing();
			BigDecimal lowestLow = data.getLowestLow();
			BigDecimal highestHigh = data.getHighestHigh();
			BigDecimal fastK = fastK(lastClosing, lowestLow, highestHigh);
			fastKList.add(fastK);
		}

		return fastKList;
	}

	private BigDecimal fastK(BigDecimal lastClosing, BigDecimal lowestLow, BigDecimal highestHigh) {
		if (highestHigh.compareTo(lowestLow) == 0) {
			return BigDecimal.ZERO;
		}

		// (LAST_CLOSING - LOWEST_LOW(n)) / (HIGHEST_HIGH(n) - LOWEST_LOW(n)) * 100
		return lastClosing.subtract(lowestLow).divide(highestHigh.subtract(lowestLow), 4, RoundingMode.HALF_UP).multiply(_100);
	}

	private BigDecimal lowestLow(List<Quote> quotes) {
		BigDecimal lowestLow = quotes.get(0).getLow();

		for (int i = 1; i < quotes.size(); i++) {
			if (quotes.get(i).getLow().compareTo(lowestLow) < 0) {
				lowestLow = quotes.get(i).getLow();
			}
		}

		return lowestLow;
	}

	private BigDecimal highestHigh(List<Quote> quotes) {
		BigDecimal highestHigh = quotes.get(0).getHigh();

		for (int i = 1; i < quotes.size(); i++) {
			if (quotes.get(i).getHigh().compareTo(highestHigh) > 0) {
				highestHigh = quotes.get(i).getHigh();
			}
		}

		return highestHigh;
	}

	private SstoResult determineTradingSignal() {
		BigDecimal k = result.getSlowK();
		BigDecimal d = result.getSlowD();
		ActionType action;
		SstoResult.Reason reason = null;

		if (trend == TrendType.UP || trend == TrendType.STRONG_UP) { // UPTREND
			if (k.compareTo(SELL) > 0 || d.compareTo(SELL) > 0) {
				action = ActionType.SELL; // UP && (%K or %D) > 80
				reason = SstoResult.Reason.OVERBOUGHT;
			} else {
				action = ActionType.HOLD;
			}
		} else if (trend == TrendType.DOWN || trend == TrendType.STRONG_DOWN) { // DOWNTREND
			if (k.compareTo(BUY) < 0 || d.compareTo(BUY) < 0) {
				action = ActionType.BUY; // DOWN && (%K or %D) < 20
				reason = SstoResult.Reason.OVERSOLD;
			} else {
				action = ActionType.HOLD;
			}
		} else { // SIDEWAYS
			List<ValueHolder> sstoResults = consolidateSstoResult();

			if (sstoResults.size() < 2) {
				action = ActionType.HOLD;
			} else {
				SstoResult previous1Ssto = _results.get(0).getSstoResult();
				BigDecimal prevK1 = previous1Ssto.getSlowK();
				BigDecimal prevD1 = previous1Ssto.getSlowD();
				SstoResult previous2Ssto = _results.size() >= 3 ? _results.get(1).getSstoResult() : null;
				BigDecimal prevK2 = previous2Ssto == null ? null : previous2Ssto.getSlowK();
				BigDecimal prevD2 = previous2Ssto == null ? null : previous2Ssto.getSlowD();

				if (prevK2 != null && prevK2.compareTo(BUY) > 0 && prevK1.compareTo(BUY) < 0 && k.compareTo(BUY) > 0 || prevD2 != null
						&& prevD2.compareTo(BUY) > 0 && prevD1.compareTo(BUY) < 0 && d.compareTo(BUY) > 0) {
					action = ActionType.BUY; // PREV_%K2 > 20 && PREV_%K1 < 20 && %K > 20 || PREV_%D2 > 20 && PREV_%D1 < 20 && %D > 20
					reason = SstoResult.Reason.BULLISH_DIP;
				} else if (prevK2 != null && prevK2.compareTo(SELL) < 0 && prevK1.compareTo(SELL) > 0 && k.compareTo(SELL) < 0 || prevD2 != null
						&& prevD2.compareTo(SELL) < 0 && prevD1.compareTo(SELL) > 0 && d.compareTo(SELL) < 0) {
					action = ActionType.SELL; // PREV_%K2 < 80 && PREV_%K1 > 80 && %K < 80 || PREV_%D2 < 80 && PREV_%D1 > 80 && %D < 80
					reason = SstoResult.Reason.BEARISH_DIP;
				} else if (prevK1.compareTo(prevD1) < 0 && k.compareTo(d) > 0) {
					action = ActionType.BUY; // PREV_%K < PREV_%D && %K > %D
					reason = SstoResult.Reason.BULLISH_CROSSOVER;
				} else if (prevK1.compareTo(prevD1) > 0 && k.compareTo(d) < 0) {
					action = ActionType.SELL; // PREV_%K > PREV_%D && %K < %D
					reason = SstoResult.Reason.BEARISH_CROSSOVER;
				} else {
					action = ActionType.HOLD;
				}
			}
		}

		BigDecimal buyStop;
		BigDecimal sellStop;
		BigDecimal stopLoss;

		if (action == ActionType.BUY) {
			buyStop = buyStop();
			sellStop = null;
			stopLoss = stopLoss(action);
		} else if (action == ActionType.SELL) {
			buyStop = null;
			sellStop = sellStop();
			stopLoss = stopLoss(action);
		} else {
			buyStop = null;
			sellStop = null;
			stopLoss = null;
		}

		result.setAction(action);
		result.setReason(reason);
		result.setBuyStop(buyStop);
		result.setSellStop(sellStop);
		result.setStopLoss(stopLoss);

		return result;
	}

	private List<ValueHolder> consolidateSstoResult() {
		List<ValueHolder> sstoResults = new ArrayList<>();
		sstoResults.add(result);

		for (IndicatorResult _result : _results) {
			sstoResults.add(_result.getSstoResult());
		}

		return sstoResults;
	}

	private BigDecimal buyStop() {
		BigDecimal currentHigh = _quotes.get(0).getHigh();
		BigDecimal buyStop;

		if (_results.isEmpty()) {
			BigDecimal priceFluctuation = BoardLotAndPriceFluctuations.determinePriceFluctuation(currentHigh);
			buyStop = currentHigh.add(priceFluctuation);
		} else {
			BigDecimal previousBuyStop = _results.get(0).getSstoResult().getBuyStop();

			if (previousBuyStop == null) {
				BigDecimal priceFluctuation = BoardLotAndPriceFluctuations.determinePriceFluctuation(currentHigh);
				buyStop = currentHigh.add(priceFluctuation);
			} else {
				BigDecimal previousHigh = _quotes.get(1).getHigh();

				if (currentHigh.compareTo(previousHigh) < 0) {
					BigDecimal priceFluctuation = BoardLotAndPriceFluctuations.determinePriceFluctuation(currentHigh);
					buyStop = currentHigh.add(priceFluctuation);
				} else {
					buyStop = previousBuyStop;
				}
			}
		}

		return buyStop;
	}

	private BigDecimal sellStop() {
		BigDecimal currentLow = _quotes.get(0).getLow();
		BigDecimal sellStop;

		if (_results.isEmpty()) {
			BigDecimal priceFluctuation = BoardLotAndPriceFluctuations.determinePriceFluctuation(currentLow);
			sellStop = currentLow.subtract(priceFluctuation);
		} else {
			BigDecimal previousSellStop = _results.get(0).getSstoResult().getSellStop();

			if (previousSellStop == null) {
				BigDecimal priceFluctuation = BoardLotAndPriceFluctuations.determinePriceFluctuation(currentLow);
				sellStop = currentLow.subtract(priceFluctuation);
			} else {
				BigDecimal previousLow = _quotes.get(1).getLow();

				if (currentLow.compareTo(previousLow) > 0) {
					BigDecimal priceFluctuation = BoardLotAndPriceFluctuations.determinePriceFluctuation(currentLow);
					sellStop = currentLow.subtract(priceFluctuation);
				} else {
					sellStop = previousSellStop;
				}
			}
		}

		return sellStop;
	}

	private BigDecimal stopLoss(ActionType action) {
		BigDecimal stopLoss;

		if (action == ActionType.BUY) {
			BigDecimal currentLow = _quotes.get(0).getLow();

			if (_results.isEmpty()) {
				stopLoss = currentLow;
			} else {
				ActionType previousAction = _results.get(0).getSstoResult().getAction();
				BigDecimal previousStopLoss = previousAction == ActionType.SELL ? null : _results.get(0).getSstoResult().getStopLoss();

				if (previousStopLoss == null) {
					stopLoss = currentLow;
				} else {
					if (currentLow.compareTo(previousStopLoss) < 0) {
						stopLoss = currentLow;
					} else {
						stopLoss = previousStopLoss;
					}
				}
			}
		} else if (action == ActionType.SELL) {
			BigDecimal currentHigh = _quotes.get(0).getHigh();

			if (_results.isEmpty()) {
				stopLoss = currentHigh;
			} else {
				ActionType previousAction = _results.get(0).getSstoResult().getAction();
				BigDecimal previousStopLoss = previousAction == ActionType.BUY ? null : _results.get(0).getSstoResult().getStopLoss();

				if (previousStopLoss == null) {
					stopLoss = currentHigh;
				} else {
					if (currentHigh.compareTo(previousStopLoss) > 0) {
						stopLoss = currentHigh;
					} else {
						stopLoss = previousStopLoss;
					}
				}
			}
		} else {
			stopLoss = null;
		}

		return stopLoss;
	}

}
