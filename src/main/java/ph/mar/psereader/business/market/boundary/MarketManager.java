package ph.mar.psereader.business.market.boundary;

import java.util.Date;
import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import ph.mar.psereader.business.market.entity.MarketSummary;
import ph.mar.psereader.business.repository.control.Repository;

@Startup
@Singleton
public class MarketManager {

	@Inject
	Logger log;

	@Inject
	Repository repository;

	public List<Date> findAllDates() {
		return repository.find(MarketSummary.ALL_DATES, Date.class);
	}

	public MarketSummary findLatestSummary() {
		List<MarketSummary> summaries = repository.find(MarketSummary.ALL, MarketSummary.class, 1);

		if (summaries.isEmpty()) {
			return null;
		}

		return summaries.get(0);
	}

}
