package ph.mar.psereader.business.indicator.boundary;

import static ph.mar.psereader.business.repository.control.QueryParameter.with;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import ph.mar.psereader.business.indicator.control.DmiIndicator;
import ph.mar.psereader.business.indicator.control.IndicatorException;
import ph.mar.psereader.business.indicator.control.RsiIndicator;
import ph.mar.psereader.business.indicator.control.SstoIndicator;
import ph.mar.psereader.business.indicator.entity.DmiResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.RsiResult;
import ph.mar.psereader.business.indicator.entity.SstoResult;
import ph.mar.psereader.business.operation.entity.Settings;
import ph.mar.psereader.business.repository.control.Repository;
import ph.mar.psereader.business.stock.entity.Quote;
import ph.mar.psereader.business.stock.entity.Stock;

@Startup
@Singleton
public class IndicatorManager {

	@Inject
	Logger log;

	@Inject
	Repository repository;

	@Inject
	SstoIndicator sstoIndicator;

	@Inject
	RsiIndicator rsiIndicator;

	@Inject
	DmiIndicator dmiIndicator;

	int minQuoteSize = 28;
	int minIndicatorResultSize = 2;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void process(Date date) {
		List<Stock> stocks = repository.find(Stock.ALL_WOWO_INDICATOR_RESULTS_BY_QUOTE_COUNT_AND_DATE, with("date", date).and("quoteCount", minQuoteSize)
				.asParameters(), Stock.class);

		for (Stock stock : stocks) {
			try {
				repository.detach(stock);
				List<Quote> quotes = findAllQuotesByStockAndDate(stock, date);
				List<IndicatorResult> results = findAllIndicatorResultsByStock(stock);

				Future<SstoResult> sstoResult = sstoIndicator.run(quotes, results);
				Future<RsiResult> rsiResult = rsiIndicator.run(quotes, results);
				Future<DmiResult> dmiResult = dmiIndicator.run(quotes, results);

				while (!sstoResult.isDone() || !rsiResult.isDone() || !dmiResult.isDone()) {
					continue;
				}

				IndicatorResult indicatorResult = new IndicatorResult(stock, date);
				indicatorResult.setSstoResult(sstoResult.get());
				indicatorResult.setRsiResult(rsiResult.get());
				indicatorResult.setDmiResult(dmiResult.get());

				log.info("{} processed.", stock.getSymbol());
				stock.add(indicatorResult);
				repository.update(stock);
			} catch (IndicatorException e) {
				log.info(e.getMessage());
			} catch (ExecutionException | InterruptedException e) {
				throw new EJBException(e);
			}
		}

		updateLastProcessedDate(date);
	}

	public List<IndicatorResult> findAllByDate(Date date) {
		return repository.find(IndicatorResult.ALL_BY_DATE, with("date", date).asParameters(), IndicatorResult.class);
	}

	private List<Quote> findAllQuotesByStockAndDate(Stock stock, Date date) {
		return repository.find(Quote.ALL_INDICATOR_DATA_BY_STOCK_AND_DATE, with("stock", stock).and("date", date).asParameters(), Quote.class,
				minQuoteSize);
	}

	private List<IndicatorResult> findAllIndicatorResultsByStock(Stock stock) {
		if (stock.getIndicatorResults().isEmpty()) {
			return Collections.emptyList();
		}

		// Quick fix for issue with @Order By and Join Fetch.
		return repository.find(IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK, with("stock", stock).asParameters(), IndicatorResult.class,
				minIndicatorResultSize);

	}

	private void updateLastProcessedDate(Date date) {
		List<Settings> settingsList = repository.find(Settings.ALL, Settings.class);
		Settings settings = settingsList.get(0);
		settings.setLastProcessedDate(date);
		repository.update(settings);
	}

}
