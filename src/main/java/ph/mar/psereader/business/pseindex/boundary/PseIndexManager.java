package ph.mar.psereader.business.pseindex.boundary;

import static ph.mar.psereader.business.repository.control.QueryParameter.with;

import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import ph.mar.psereader.business.pseindex.entity.PseIndex;
import ph.mar.psereader.business.pseindex.entity.PseIndex.Type;
import ph.mar.psereader.business.repository.control.Repository;
import ph.mar.psereader.business.stock.entity.Stock;

@Startup
@Singleton
public class PseIndexManager {

	@Inject
	Logger log;

	@Inject
	Repository repository;

	public List<Stock> findAllByIndex(PseIndex.Type index) {
		if (index == Type.PSEI) {
			return repository.find(Stock.ALL_PSEI, Stock.class);
		}

		if (index == Type.ALL) {
			return repository.find(Stock.ALL_ALL_SHARES, Stock.class);
		}

		return repository.find(Stock.BY_SECTORAL_INDEX, with("sectoralIndex", index).asParameters(), Stock.class);
	}

}
