package ph.mar.psereader.presentation;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import ph.mar.psereader.business.stock.boundary.StockManager;
import ph.mar.psereader.business.stock.entity.Quote;
import ph.mar.psereader.business.stock.entity.Stock;

@Named
@ViewScoped
public class StocksPage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	Logger log;

	@Inject
	StockManager stockManager;

	private List<String> symbols;
	private String selectedSymbol;
	private Stock stock;
	private List<Quote> quotes;

	@PostConstruct
	void init() {
		symbols = stockManager.findAllSymbols();

		if (!symbols.isEmpty()) {
			selectedSymbol = symbols.get(0);
			onSymbolChange();
		}
	}

	public void onSymbolChange() {
		stock = stockManager.findBySymbol(selectedSymbol);
		quotes = stockManager.findAllQuotesByStock(stock);
	}

	public List<String> getSymbols() {
		return symbols;
	}

	public String getSelectedSymbol() {
		return selectedSymbol;
	}

	public void setSelectedSymbol(String selectedSymbol) {
		this.selectedSymbol = selectedSymbol;
	}

	public Stock getStock() {
		return stock;
	}

	public List<Quote> getQuotes() {
		return quotes;
	}

}
