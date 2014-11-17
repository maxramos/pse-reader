package ph.mar.psereader.presentation;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ph.mar.psereader.business.indicator.boundary.IndicatorManager;
import ph.mar.psereader.business.indicator.entity.ActionType;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.operation.boundary.OperationManager;

@Named
@ViewScoped
public class IndicatorsPage implements Serializable {


	private static final long serialVersionUID = 1L;
	private static final String ALL = "ALL";

	@Inject
	IndicatorManager indicatorManager;

	@Inject
	OperationManager operationManager;

	private Date lastProcessedDate;
	private List<IndicatorResult> results;
	private List<IndicatorResult> filteredResults;
	private String selectedAction;

	@PostConstruct
	void init() {
		lastProcessedDate = operationManager.findLastProcessedDate();
		results = indicatorManager.findAllByDate(lastProcessedDate);
		selectedAction = ALL;
	}

	public void onActionChange() {
		if (ALL.equals(selectedAction)) {
			results = indicatorManager.findAllByDate(lastProcessedDate);
		} else {
			results = indicatorManager.findAllByDateAndAction(lastProcessedDate, ActionType.valueOf(selectedAction));
		}
	}

	public Date getLastProcessedDate() {
		return lastProcessedDate;
	}

	public List<IndicatorResult> getResults() {
		return results;
	}

	public List<IndicatorResult> getFilteredResults() {
		return filteredResults;
	}

	public void setFilteredResults(List<IndicatorResult> filteredResults) {
		this.filteredResults = filteredResults;
	}

	public String getSelectedAction() {
		return selectedAction;
	}

	public void setSelectedAction(String selectedAction) {
		this.selectedAction = selectedAction;
	}

}
