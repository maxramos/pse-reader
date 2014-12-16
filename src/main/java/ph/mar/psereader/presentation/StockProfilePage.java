package ph.mar.psereader.presentation;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import ph.mar.psereader.business.pseindex.entity.PseIndex;
import ph.mar.psereader.business.stock.boundary.StockManager;
import ph.mar.psereader.business.stock.entity.SectorType;
import ph.mar.psereader.business.stock.entity.Stock;
import ph.mar.psereader.business.stock.entity.SubSectorType;

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

	public void onSectorChange() {
		String subSector = stock.getSector().getFirstSubSector();
		stock.setSubSector(SubSectorType.valueOf(subSector));
		updateStock();
	}

	public void onIndexChange() {
		if (stock.isPsei() || stock.getSectoralIndex() != null) {
			stock.setAllShares(true);
		}

		updateStock();
	}

	public void updateStock() {
		stock = stockManager.update(stock);
	}

	public SectorType[] getSectors() {
		return SectorType.getStockRelatedSectors();
	}

	public SubSectorType[] getSubSectors() {
		return SubSectorType.subSectorsOf(stock.getSector());
	}

	public PseIndex.Type[] getSectoralIndices() {
		return PseIndex.Type.getSectoralIndices();
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
