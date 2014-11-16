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
import ph.mar.psereader.business.indicator.entity.EmaResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.ObvResult;
import ph.mar.psereader.business.indicator.entity.SstoResult;
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
	public Future<Stock> run(Stock stock, Date date, int quoteSize, int indicatorResultSize) {
		List<Quote> quotes = findAllQuotesByStockAndDate(stock, date, quoteSize);
		List<IndicatorResult> results = findAllIndicatorResultsByStock(stock, indicatorResultSize);

		Future<DmiResult> dmiResult = executorService.submit(new DMI(quotes, results));
		Future<SstoResult> sstoResult = executorService.submit(new SSTO(quotes, results));
		Future<EmaResult> emaResult = executorService.submit(new EMA(quotes, results));
		Future<ObvResult> obvResult = executorService.submit(new OBV(quotes, results));

		while (!dmiResult.isDone() || !sstoResult.isDone() || !emaResult.isDone() || !obvResult.isDone()) {
			continue;
		}

		try {
			IndicatorResult indicatorResult = new IndicatorResult(stock, date);
			indicatorResult.setDmiResult(dmiResult.get());
			indicatorResult.setSstoResult(sstoResult.get());
			indicatorResult.setEmaResult(emaResult.get());
			indicatorResult.setObvResult(obvResult.get());
			indicatorResult.process(quotes, results);
			stock.add(indicatorResult);
			log.info("{} processed.", stock.getSymbol());
			return new AsyncResult<>(stock);
		} catch (ExecutionException | InterruptedException e) {
			throw new EJBException(e);
		}
	}

	private List<Quote> findAllQuotesByStockAndDate(Stock stock, Date date, int quoteSize) {
		return repository.find(Quote.ALL_INDICATOR_DATA_BY_STOCK_AND_DATE, with("stock", stock).and("date", date).asParameters(), Quote.class,
				quoteSize);
	}

	private List<IndicatorResult> findAllIndicatorResultsByStock(Stock stock, int indicatorResultSize) {
		// Quick fix for issue with @Order By and Join Fetch.
		return repository.find(IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK, with("stock", stock).asParameters(), IndicatorResult.class,
				indicatorResultSize);
	}

}
