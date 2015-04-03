package ph.mar.psereader.presentation;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import ph.mar.psereader.business.indicator.boundary.IndicatorManager;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.market.boundary.MarketManager;
import ph.mar.psereader.business.market.entity.MarketSummary;
import ph.mar.psereader.business.operation.boundary.OperationManager;

@Named
@ViewScoped
public class HomePage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	Logger log;

	@Inject
	MarketManager marketManager;

	@Inject
	OperationManager operationManager;

	@Inject
	IndicatorManager indicatorManager;

	private MarketSummary marketSummary;
	private List<IndicatorResult> actives;
	private List<IndicatorResult> gainers;
	private List<IndicatorResult> losers;

	@PostConstruct
	void init() {
		marketSummary = marketManager.findLatestSummary();
		Date lastProcessedDate = operationManager.findLastProcessedDate();
		int maxResult = 10;
		actives = indicatorManager.findAllActivesByDate(lastProcessedDate, maxResult);
		gainers = indicatorManager.findAllGainersByDate(lastProcessedDate, maxResult);
		losers = indicatorManager.findAllLosersByDate(lastProcessedDate, maxResult);
	}

	public MarketSummary getMarketSummary() {
		return marketSummary;
	}

	public List<IndicatorResult> getActives() {
		return actives;
	}

	public List<IndicatorResult> getGainers() {
		return gainers;
	}

	public List<IndicatorResult> getLosers() {
		return losers;
	}

}
