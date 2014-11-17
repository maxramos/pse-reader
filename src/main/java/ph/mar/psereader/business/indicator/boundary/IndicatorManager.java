package ph.mar.psereader.business.indicator.boundary;

import static ph.mar.psereader.business.repository.control.QueryParameter.with;

import java.util.ArrayList;
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

import ph.mar.psereader.business.indicator.control.IndicatorContainer;
import ph.mar.psereader.business.indicator.entity.ActionType;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.operation.entity.Settings;
import ph.mar.psereader.business.repository.control.Repository;
import ph.mar.psereader.business.stock.entity.Stock;

@Startup
@Singleton
public class IndicatorManager {

	@Inject
	Logger log;

	@Inject
	Repository repository;

	@Inject
	IndicatorContainer indicatorContainer;

	int quoteSize = 21;
	int indicatorResultSize = 2;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void process(Date date) {
		List<Future<Stock>> results = new ArrayList<>();
		List<Stock> stocks = repository.find(Stock.ALL_WOWO_INDICATOR_RESULTS_BY_DATE_AND_COUNT, with("date", date).and("count", quoteSize)
				.asParameters(), Stock.class);

		for (Stock stock : stocks) {
			repository.detach(stock);
			Future<Stock> result = indicatorContainer.run(stock, date, quoteSize, indicatorResultSize);
			results.add(result);
		}

		addIndicatorResults(date, results);
		updateLastProcessedDate(date);
	}

	public List<IndicatorResult> findAllByDate(Date date) {
		return repository.find(IndicatorResult.ALL_INDICATOR_DATA_BY_DATE, with("date", date).asParameters(), IndicatorResult.class);
	}

	public List<IndicatorResult> findAllByDateAndAction(Date date, ActionType action) {
		return repository.find(IndicatorResult.ALL_INDICATOR_DATA_BY_DATE_AND_ACTION, with("date", date).and("action", action).asParameters(),
				IndicatorResult.class);
	}

	private void addIndicatorResults(Date date, List<Future<Stock>> results) {
		for (Future<Stock> result : results) {
			while (!result.isDone()) {
				continue;
			}

			try {
				Stock stock = result.get();
				repository.update(stock);
			} catch (ExecutionException | InterruptedException e) {
				throw new EJBException(e);
			}
		}
	}

	private void updateLastProcessedDate(Date date) {
		List<Settings> settingsList = repository.find(Settings.ALL, Settings.class);
		Settings settings = settingsList.get(0);
		settings.setLastProcessedDate(date);
		repository.update(settings);
	}

}
