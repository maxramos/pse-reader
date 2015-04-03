package ph.mar.psereader.presentation;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import ph.mar.psereader.business.fundamental.boundary.FundamentalManager;
import ph.mar.psereader.business.fundamental.entity.Fundamental;
import ph.mar.psereader.business.stock.entity.Stock;

@Named
@ViewScoped
public class FundamentalsPage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	Logger log;

	@Inject
	FundamentalManager fundamentalManager;

	private List<Stock> stocks;
	private List<String> symbols;
	private Fundamental fundamental;
	private String selectedSymbol;

	@PostConstruct
	void init() {
		load();
	}

	public void save() {
		fundamentalManager.add(selectedSymbol, fundamental);
		load();
	}

	public List<Stock> getStocks() {
		return stocks;
	}

	public List<String> getSymbols() {
		return symbols;
	}

	public Fundamental getFundamental() {
		return fundamental;
	}

	public String getSelectedSymbol() {
		return selectedSymbol;
	}

	public void setSelectedSymbol(String selectedSymbol) {
		this.selectedSymbol = selectedSymbol;
	}

	private void load() {
		stocks = fundamentalManager.findAllStocksWithFundamental();
		symbols = fundamentalManager.findAllSymbolsWithoutFundamental();
		fundamental = new Fundamental();

		if (!symbols.isEmpty()) {
			selectedSymbol = symbols.get(0);
		}
	}

}
