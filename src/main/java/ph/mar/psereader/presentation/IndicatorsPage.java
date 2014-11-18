package ph.mar.psereader.presentation;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.chart.OhlcChartModel;
import org.primefaces.model.chart.OhlcChartSeries;

import ph.mar.psereader.business.indicator.boundary.IndicatorManager;
import ph.mar.psereader.business.indicator.entity.ActionType;
import ph.mar.psereader.business.indicator.entity.DmiResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.SstoResult;
import ph.mar.psereader.business.operation.boundary.OperationManager;
import ph.mar.psereader.business.stock.entity.Quote;

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
	private IndicatorResult selectedResult;
	private OhlcChartModel quoteModel;
	private LineChartModel emaModel;
	private LineChartModel obvModel;
	private LineChartModel sstoModel;
	private LineChartModel dmiModel;
	private LineChartModel atrModel;

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
		if (event.getVisibility() == Visibility.VISIBLE) {
			selectedResult = (IndicatorResult) event.getData();
			quoteModel = null;
			emaModel = null;
			obvModel = null;
			sstoModel = null;
			dmiModel = null;
			atrModel = null;
		} else {
			selectedResult = null;
		}
	}

	public void updateCharts() {
		List<Quote> quotes = indicatorManager.findAllQuoteByStockAndDate(selectedResult.getStock(), lastProcessedDate, 200);
		Collections.reverse(quotes);

		quoteModel = new OhlcChartModel();
		quoteModel.setTitle("Price");
		quoteModel.setShadow(false);
		quoteModel.setAnimate(true);
		quoteModel.setZoom(true);
		quoteModel.setExtender("extendQuoteModel");

		for (int i = 0; i < quotes.size(); i++) {
			Quote quote = quotes.get(i);
			quoteModel.add(new OhlcChartSeries(i, quote.getOpen().doubleValue(), quote.getHigh().doubleValue(), quote.getLow().doubleValue(), quote
					.getClose().doubleValue()));
		}

		LineChartSeries priceSeries = new LineChartSeries();
		priceSeries.setLabel("Price");
		priceSeries.setShowMarker(false);
		LineChartSeries emaSeries = new LineChartSeries();
		emaSeries.setLabel("EMA");
		emaSeries.setShowMarker(false);

		LineChartSeries obvSeries = new LineChartSeries();
		obvSeries.setShowMarker(false);

		LineChartSeries kSeries = new LineChartSeries();
		kSeries.setLabel("%K");
		kSeries.setShowMarker(false);
		LineChartSeries dSeries = new LineChartSeries();
		dSeries.setLabel("%D");
		dSeries.setShowMarker(false);

		LineChartSeries adxSeries = new LineChartSeries();
		adxSeries.setLabel("ADX");
		adxSeries.setShowMarker(false);
		LineChartSeries plusDiSeries = new LineChartSeries();
		plusDiSeries.setLabel("+DI");
		plusDiSeries.setShowMarker(false);
		LineChartSeries minusDiSeries = new LineChartSeries();
		minusDiSeries.setLabel("-DI");
		minusDiSeries.setShowMarker(false);

		LineChartSeries atrSeries = new LineChartSeries();
		atrSeries.setShowMarker(false);

		List<IndicatorResult> indicatorResults = indicatorManager.findAllByStockAndDate(selectedResult.getStock(), lastProcessedDate, 200);
		Collections.reverse(indicatorResults);

		for (int i = 0; i < indicatorResults.size(); i++) {
			IndicatorResult result = indicatorResults.get(i);

			priceSeries.set(i, result.getPrice());
			emaSeries.set(i, result.getEmaResult().getEma());

			obvSeries.set(i, result.getObvResult().getObv());

			DmiResult dmiResult = result.getDmiResult();
			adxSeries.set(i, dmiResult.getAdx());
			plusDiSeries.set(i, dmiResult.getPlusDi());
			minusDiSeries.set(i, dmiResult.getMinusDi());

			SstoResult sstoResult = result.getSstoResult();
			kSeries.set(i, sstoResult.getSlowK());
			dSeries.set(i, sstoResult.getSlowD());

			atrSeries.set(i, result.getDmiResult().getAtr());
		}

		emaModel = new LineChartModel();
		emaModel.setTitle("EMA");
		emaModel.setLegendPosition("nw");
		emaModel.setLegendCols(2);
		emaModel.setSeriesColors("008800, 000088");
		emaModel.setShadow(false);
		emaModel.setAnimate(true);
		emaModel.setZoom(true);
		emaModel.setExtender("extendEmaModel");
		emaModel.addSeries(priceSeries);
		emaModel.addSeries(emaSeries);

		obvModel = new LineChartModel();
		obvModel.setTitle("OBV");
		obvModel.setSeriesColors("000088");
		obvModel.setShadow(false);
		obvModel.setAnimate(true);
		obvModel.setZoom(true);
		obvModel.setExtender("extendObvModel");
		obvModel.addSeries(obvSeries);

		sstoModel = new LineChartModel();
		sstoModel.setTitle("SSTO");
		sstoModel.setLegendPosition("nw");
		sstoModel.setLegendCols(2);
		sstoModel.setSeriesColors("000088, 880000");
		sstoModel.setShadow(false);
		sstoModel.setAnimate(true);
		sstoModel.setZoom(true);
		sstoModel.setExtender("extendSstoModel");
		sstoModel.addSeries(kSeries);
		sstoModel.addSeries(dSeries);

		dmiModel = new LineChartModel();
		dmiModel.setTitle("DMI");
		dmiModel.setLegendPosition("nw");
		dmiModel.setLegendCols(3);
		dmiModel.setSeriesColors("000088, 008800, 880000");
		dmiModel.setShadow(false);
		dmiModel.setAnimate(true);
		dmiModel.setZoom(true);
		dmiModel.setExtender("extendDmiModel");
		dmiModel.addSeries(adxSeries);
		dmiModel.addSeries(plusDiSeries);
		dmiModel.addSeries(minusDiSeries);

		atrModel = new LineChartModel();
		atrModel.setTitle("ATR");
		atrModel.setSeriesColors("000088");
		atrModel.setShadow(false);
		atrModel.setAnimate(true);
		atrModel.setZoom(true);
		atrModel.setExtender("extendAtrModel");
		atrModel.addSeries(atrSeries);
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

	public OhlcChartModel getQuoteModel() {
		return quoteModel;
	}

	public LineChartModel getEmaModel() {
		return emaModel;
	}

	public LineChartModel getObvModel() {
		return obvModel;
	}

	public LineChartModel getSstoModel() {
		return sstoModel;
	}

	public LineChartModel getDmiModel() {
		return dmiModel;
	}

	public LineChartModel getAtrModel() {
		return atrModel;
	}

}
