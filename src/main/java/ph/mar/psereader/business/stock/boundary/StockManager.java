package ph.mar.psereader.business.stock.boundary;

import static ph.mar.psereader.business.repository.control.QueryParameter.with;

import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import ph.mar.psereader.business.repository.control.Repository;
import ph.mar.psereader.business.stock.entity.Quote;
import ph.mar.psereader.business.stock.entity.Stock;

@Startup
@Singleton
public class StockManager {

	@Inject
	Logger log;

	@Inject
	Repository repository;

	public List<String> findAllSymbols() {
		return repository.find(Stock.ALL_SYMBOLS, String.class);
	}

	public Stock findBySymbol(String symbol) {
		List<Stock> stocks = repository.find(Stock.BY_SYMBOL, with("symbol", symbol).asParameters(), Stock.class);

		if (stocks.isEmpty()) {
			return null;
		}

		return stocks.get(0);
	}

	public List<Quote> findAllQuotesByStock(Stock stock) {
		return repository.find(Quote.BY_STOCK, with("stock", stock).asParameters(), Quote.class);
	}

	public Stock update(Stock stock) {
		return repository.update(stock);
	}

}
