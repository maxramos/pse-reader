package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.ObvResult;
import ph.mar.psereader.business.stock.entity.Quote;

public class OBV implements Callable<ObvResult> {

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;

	public OBV(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public ObvResult call() throws Exception {
		return _results.isEmpty() ? initialObv() : succeedingObv();
	}

	private ObvResult initialObv() {
		Quote quote = _quotes.get(0);
		BigDecimal close = quote.getClose();
		BigDecimal previousClose = _quotes.get(1).getClose();
		Long volume = quote.getVolume();
		Long previousObv = 0L;

		Long obv = obv(close, previousClose, volume, previousObv);
		return new ObvResult(obv);
	}

	private ObvResult succeedingObv() {
		Quote quote = _quotes.get(0);
		BigDecimal close = quote.getClose();
		BigDecimal previousClose = _quotes.get(1).getClose();
		Long volume = quote.getVolume();
		Long previousObv = _results.get(0).getObvResult().getObv();

		Long obv = obv(close, previousClose, volume, previousObv);
		return new ObvResult(obv);
	}

	private Long obv(BigDecimal close, BigDecimal previousClose, Long volume, Long previousObv) {
		Long obv = previousObv;

		if (close.compareTo(previousClose) > 0) {
			obv += volume;
		} else if (close.compareTo(previousClose) < 0) {
			obv -= volume;
		}

		return obv;
	}

}
