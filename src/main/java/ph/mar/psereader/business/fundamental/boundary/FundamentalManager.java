package ph.mar.psereader.business.fundamental.boundary;

import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import ph.mar.psereader.business.fundamental.entity.Fundamental;
import ph.mar.psereader.business.repository.control.QueryParameter;
import ph.mar.psereader.business.repository.control.Repository;
import ph.mar.psereader.business.stock.entity.Stock;

@Startup
@Singleton
public class FundamentalManager {

	@Inject
	Logger log;

	@Inject
	Repository repository;

	public List<Stock> findAllStocksWithFundamental() {
		return repository.find(Stock.ALL_WITH_FUNDAMENTAL, Stock.class);
	}

	public List<String> findAllSymbolsWithoutFundamental() {
		return repository.find(Stock.ALL_SYMBOLS, String.class);
	}

	public Stock add(String symbol, Fundamental fundamental) {
		Stock stock = repository.find(Stock.BY_SYMBOL, QueryParameter.with("symbol", symbol).asParameters(), Stock.class).get(0);
		stock.setFundamental(fundamental);
		fundamental.setStock(stock);
		return repository.update(stock);
	}

}
