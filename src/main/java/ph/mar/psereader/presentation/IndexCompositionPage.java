package ph.mar.psereader.presentation;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import ph.mar.psereader.business.pseindex.boundary.PseIndexManager;
import ph.mar.psereader.business.pseindex.entity.PseIndex;
import ph.mar.psereader.business.pseindex.entity.PseIndex.Type;
import ph.mar.psereader.business.stock.entity.Stock;

@Named
@ViewScoped
public class IndexCompositionPage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	Logger log;

	@Inject
	PseIndexManager pseIndexManager;

	private List<Stock> stocks;
	private PseIndex.Type selectedIndex;

	@PostConstruct
	void init() {
		selectedIndex = Type.PSEI;
		onIndexChange();
	}

	public void onIndexChange() {
		stocks = pseIndexManager.findAllByIndex(selectedIndex);
	}

	public PseIndex.Type[] getIndices() {
		return PseIndex.Type.getIndices();
	}

	public List<Stock> getStocks() {
		return stocks;
	}

	public PseIndex.Type getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(PseIndex.Type selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

}
