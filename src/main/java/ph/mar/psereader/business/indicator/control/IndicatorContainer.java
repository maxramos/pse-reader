package ph.mar.psereader.business.indicator.control;

import static ph.mar.psereader.business.repository.control.QueryParameter.with;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

import org.slf4j.Logger;

import ph.mar.psereader.business.indicator.entity.DmiResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.MacdResult;
import ph.mar.psereader.business.indicator.entity.ObvResult;
import ph.mar.psereader.business.indicator.entity.RsiResult;
import ph.mar.psereader.business.indicator.entity.SstoResult;
import ph.mar.psereader.business.indicator.entity.VrResult;
import ph.mar.psereader.business.repository.control.Repository;
import ph.mar.psereader.business.stock.entity.Quote;
import ph.mar.psereader.business.stock.entity.Stock;

@Stateless
public class IndicatorContainer {

	@Inject
	Logger log;

	@Inject
	Repository repository;

	@Resource(lookup = "concurrent/psereader")
	ManagedExecutorService executorService;

	@Asynchronous
	public Future<Stock> run(Stock stock, Date date, int minQuoteSize, int minIndicatorResultSize) {
		List<Quote> quotes = findAllQuotesByStockAndDate(stock, date, minQuoteSize);
		List<IndicatorResult> results = findAllIndicatorResultsByStock(stock, minIndicatorResultSize);

		Future<SstoResult> sstoResult = executorService.submit(new SstoIndicator(quotes, results));
		Future<RsiResult> rsiResult = executorService.submit(new RsiIndicator(quotes, results));
		Future<DmiResult> dmiResult = executorService.submit(new DmiIndicator(quotes, results));
		Future<MacdResult> macdResult = executorService.submit(new MacdIndicator(quotes, results));
		Future<ObvResult> obvResult = executorService.submit(new ObvIndicator(quotes, results));
		Future<VrResult> vrResult = executorService.submit(new VrIndicator(stock, quotes, results));

		while (!sstoResult.isDone() || !rsiResult.isDone() || !dmiResult.isDone() || !macdResult.isDone() || !obvResult.isDone()
				|| !vrResult.isDone()) {
			continue;
		}

		IndicatorResult indicatorResult = new IndicatorResult(stock, date);

		try {
			indicatorResult.setSstoResult(sstoResult.get());
			indicatorResult.setRsiResult(rsiResult.get());
			indicatorResult.setDmiResult(dmiResult.get());
			indicatorResult.setMacdResult(macdResult.get());
			indicatorResult.setObvResult(obvResult.get());
			indicatorResult.setVrResult(vrResult.get());
		} catch (ExecutionException | InterruptedException e) {
			throw new EJBException(e);
		}

		stock.add(indicatorResult);
		log.info("{} processed.", stock.getSymbol());
		return new AsyncResult<>(stock);
	}

	private List<Quote> findAllQuotesByStockAndDate(Stock stock, Date date, int minQuoteSize) {
		return repository.find(Quote.ALL_INDICATOR_DATA_BY_STOCK_AND_DATE, with("stock", stock).and("date", date).asParameters(), Quote.class,
				minQuoteSize);
	}

	private List<IndicatorResult> findAllIndicatorResultsByStock(Stock stock, int minIndicatorResultSize) {
		// Quick fix for issue with @Order By and Join Fetch.
		return repository.find(IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK, with("stock", stock).asParameters(), IndicatorResult.class,
				minIndicatorResultSize);
	}

}
