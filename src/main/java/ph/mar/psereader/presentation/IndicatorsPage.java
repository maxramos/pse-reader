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
import ph.mar.psereader.business.indicator.entity.DmiResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.RecommendationType;
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
	private String selectedRecommendation;
	private IndicatorResult selectedResult;
	private OhlcChartModel quoteModel;
	private LineChartModel emaModel;
	private LineChartModel sstoModel;
	private LineChartModel dmiModel;
	private LineChartModel obvModel;
	private LineChartModel atrModel;

	@PostConstruct
	void init() {
		lastProcessedDate = operationManager.findLastProcessedDate();
		results = indicatorManager.findAllByDate(lastProcessedDate);
		selectedRecommendation = ALL;
	}

	public void onRecommendationChange() {
		if (ALL.equals(selectedRecommendation)) {
			results = indicatorManager.findAllByDate(lastProcessedDate);
		} else {
			results = indicatorManager.findAllByDateAndAction(lastProcessedDate, RecommendationType.valueOf(selectedRecommendation));
		}
	}

	public void onRowToggle(ToggleEvent event) {
		if (event.getVisibility() == Visibility.VISIBLE) {
			selectedResult = (IndicatorResult) event.getData();
			initCharts();
		} else {
			selectedResult = null;
		}
	}

	public void updateCharts() {
		List<Quote> quotes = indicatorManager.findAllQuoteByStockAndDate(selectedResult.getStock(), lastProcessedDate, 200);
		Collections.reverse(quotes);
		List<IndicatorResult> indicatorResults = indicatorManager.findAllByStockAndDate(selectedResult.getStock(), lastProcessedDate, 200);
		Collections.reverse(indicatorResults);

		LineChartSeries priceSeries = new LineChartSeries();
		priceSeries.setLabel("Price");
		priceSeries.setShowMarker(false);
		LineChartSeries emaSeries = new LineChartSeries();
		emaSeries.setLabel("EMA");
		emaSeries.setShowMarker(false);

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

		LineChartSeries obvSeries = new LineChartSeries();
		obvSeries.setShowMarker(false);

		LineChartSeries atrSeries = new LineChartSeries();
		atrSeries.setShowMarker(false);

		for (int i = 0; i < quotes.size(); i++) {
			Quote quote = quotes.get(i);
			quoteModel.add(new OhlcChartSeries(i, quote.getOpen().doubleValue(), quote.getHigh().doubleValue(), quote.getLow().doubleValue(), quote
					.getClose().doubleValue()));
		}

		for (int i = 0; i < indicatorResults.size(); i++) {
			IndicatorResult result = indicatorResults.get(i);

			priceSeries.set(i, result.getPriceActionResult().getPrice());
			emaSeries.set(i, result.getEmaResult().getEma());

			DmiResult dmiResult = result.getDmiResult();
			adxSeries.set(i, dmiResult.getAdx());
			plusDiSeries.set(i, dmiResult.getPlusDi());
			minusDiSeries.set(i, dmiResult.getMinusDi());

			SstoResult sstoResult = result.getSstoResult();
			kSeries.set(i, sstoResult.getSlowK());
			dSeries.set(i, sstoResult.getSlowD());

			obvSeries.set(i, result.getObvResult().getObv());

			atrSeries.set(i, result.getDmiResult().getAtr());
		}

		emaModel.addSeries(priceSeries);
		emaModel.addSeries(emaSeries);

		sstoModel.clear();
		sstoModel.addSeries(kSeries);
		sstoModel.addSeries(dSeries);

		dmiModel.clear();
		dmiModel.addSeries(adxSeries);
		dmiModel.addSeries(plusDiSeries);
		dmiModel.addSeries(minusDiSeries);

		obvModel.clear();
		obvModel.addSeries(obvSeries);

		atrModel.clear();
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

	public String getSelectedRecommendation() {
		return selectedRecommendation;
	}

	public void setSelectedRecommendation(String selectedRecommendation) {
		this.selectedRecommendation = selectedRecommendation;
	}

	public OhlcChartModel getQuoteModel() {
		return quoteModel;
	}

	public LineChartModel getEmaModel() {
		return emaModel;
	}

	public LineChartModel getSstoModel() {
		return sstoModel;
	}

	public LineChartModel getDmiModel() {
		return dmiModel;
	}

	public LineChartModel getObvModel() {
		return obvModel;
	}

	public LineChartModel getAtrModel() {
		return atrModel;
	}

	private void initCharts() {
		quoteModel = new OhlcChartModel();
		quoteModel.setTitle("Price");
		quoteModel.setShadow(false);
		quoteModel.setAnimate(true);
		quoteModel.setZoom(true);
		quoteModel.setExtender("extendQuoteModel");

		emaModel = new LineChartModel();
		emaModel.setTitle("EMA");
		emaModel.setLegendPosition("nw");
		emaModel.setLegendCols(2);
		emaModel.setSeriesColors("008800, 000088");
		emaModel.setShadow(false);
		emaModel.setAnimate(true);
		emaModel.setZoom(true);
		emaModel.setExtender("extendEmaModel");

		sstoModel = new LineChartModel();
		sstoModel.setTitle("SSTO");
		sstoModel.setLegendPosition("nw");
		sstoModel.setLegendCols(2);
		sstoModel.setSeriesColors("000088, 880000");
		sstoModel.setShadow(false);
		sstoModel.setAnimate(true);
		sstoModel.setZoom(true);
		sstoModel.setExtender("extendSstoModel");

		dmiModel = new LineChartModel();
		dmiModel.setTitle("DMI");
		dmiModel.setLegendPosition("nw");
		dmiModel.setLegendCols(3);
		dmiModel.setSeriesColors("000088, 008800, 880000");
		dmiModel.setShadow(false);
		dmiModel.setAnimate(true);
		dmiModel.setZoom(true);
		dmiModel.setExtender("extendDmiModel");

		obvModel = new LineChartModel();
		obvModel.setTitle("OBV");
		obvModel.setSeriesColors("000088");
		obvModel.setShadow(false);
		obvModel.setAnimate(true);
		obvModel.setZoom(true);
		obvModel.setExtender("extendObvModel");

		atrModel = new LineChartModel();
		atrModel.setTitle("ATR");
		atrModel.setSeriesColors("000088");
		atrModel.setShadow(false);
		atrModel.setAnimate(true);
		atrModel.setZoom(true);
		atrModel.setExtender("extendAtrModel");
	}

}
