package ph.mar.psereader.presentation;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import ph.mar.psereader.business.market.boundary.MarketManager;
import ph.mar.psereader.business.market.entity.MarketSummary;

@Named
@ViewScoped
public class IndexPage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	Logger log;

	@Inject
	MarketManager marketManager;

	private MarketSummary marketSummary;

	@PostConstruct
	void init() {
		marketSummary = marketManager.findLatestSummary();
	}

	public MarketSummary getMarketSummary() {
		return marketSummary;
	}

}
