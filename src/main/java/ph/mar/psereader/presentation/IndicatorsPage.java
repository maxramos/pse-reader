package ph.mar.psereader.presentation;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.chart.LinearAxis;

import ph.mar.psereader.business.indicator.boundary.IndicatorManager;
import ph.mar.psereader.business.indicator.entity.ActionType;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.operation.boundary.OperationManager;

@Named
@ViewScoped
public class IndicatorsPage implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String ALL = "ALL";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	@Inject
	IndicatorManager indicatorManager;

	@Inject
	OperationManager operationManager;

	private Date lastProcessedDate;
	private List<IndicatorResult> results;
	private List<IndicatorResult> filteredResults;
	private String selectedAction;
	private LineChartModel priceModel;

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

	public void onRowToggle(ToggleEvent event) {
		if (event.getVisibility() == Visibility.HIDDEN) {
			return;
		}

		IndicatorResult selectedResult = (IndicatorResult) event.getData();
		List<IndicatorResult> dataList = indicatorManager.findAllByStockAndDate(selectedResult.getStock(), lastProcessedDate, 21);
		updateCharts(dataList);
	}

	private void updateCharts(List<IndicatorResult> dataList) {
		LineChartSeries priceSeries = new LineChartSeries();
		priceSeries.setLabel("Price");

		for (IndicatorResult data : dataList) {
			priceSeries.set(DATE_FORMAT.format(data.getDate()), data.getPrice());
		}

		priceModel = new LineChartModel();
		priceModel.setTitle("Price Chart");
		priceModel.setShowPointLabels(true);
		priceModel.addSeries(priceSeries);

		Axis axis = new LinearAxis("Dates");
		axis.setTickAngle(-50);
		priceModel.getAxes().put(AxisType.X, axis);
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

	public LineChartModel getPriceModel() {
		return priceModel;
	}

}
