package ph.mar.psereader.presentation.stock;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import ph.mar.psereader.business.index.entity.PseIndex;
import ph.mar.psereader.business.stock.boundary.StockManager;
import ph.mar.psereader.business.stock.entity.Stock;

@Named
@ViewScoped
public class StockProfilePage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	Logger log;

	@Inject
	StockManager stockManager;

	private List<String> symbols;
	private String selectedSymbol;
	private Stock stock;

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
	}

	public void updateStock() {
		if (stock.isPsei() || stock.getSectoralIndex() != null) {
			stock.setAllShares(true);
		}
	}

	public PseIndex.Type[] getSectoralIndeces() {
		return PseIndex.Type.getSectoralIndeces();
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

}
