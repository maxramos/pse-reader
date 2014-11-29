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
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.BarChartSeries;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.chart.OhlcChartModel;
import org.primefaces.model.chart.OhlcChartSeries;

import ph.mar.psereader.business.indicator.boundary.IndicatorManager;
import ph.mar.psereader.business.indicator.entity.FstoResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.RecommendationType;
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
	private BarChartModel volumeModel;
	private LineChartModel emaModel;
	private LineChartModel fstoModel;
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
		List<Quote> quotes = indicatorManager.findAllQuoteByStockAndDate(selectedResult.getStock(), lastProcessedDate);
		Collections.reverse(quotes);
		List<IndicatorResult> indicatorResults = indicatorManager.findAllByStockAndDate(selectedResult.getStock(), lastProcessedDate);
		Collections.reverse(indicatorResults);

		LineChartSeries priceSeries = new LineChartSeries();
		priceSeries.setLabel("Price");
		priceSeries.setShowMarker(false);
		LineChartSeries emaSeries = new LineChartSeries();
		emaSeries.setLabel("EMA");
		emaSeries.setShowMarker(false);

		BarChartSeries volumeSeries = new BarChartSeries();
		volumeSeries.setLabel("Volume");
		priceSeries.setShowMarker(false);

		LineChartSeries kSeries = new LineChartSeries();
		kSeries.setLabel("%K");
		kSeries.setShowMarker(false);
		LineChartSeries dSeries = new LineChartSeries();
		dSeries.setLabel("%D");
		dSeries.setShowMarker(false);

		LineChartSeries obvSeries = new LineChartSeries();
		obvSeries.setShowMarker(false);

		LineChartSeries atrSeries = new LineChartSeries();
		atrSeries.setShowMarker(false);

		for (int i = 0; i < quotes.size(); i++) {
			Quote quote = quotes.get(i);
			quoteModel.add(new OhlcChartSeries(i, quote.getOpen().doubleValue(), quote.getHigh().doubleValue(), quote.getLow().doubleValue(), quote
					.getClose().doubleValue()));

			volumeSeries.set(i, quote.getVolume());
		}

		for (int i = 0; i < indicatorResults.size(); i++) {
			IndicatorResult result = indicatorResults.get(i);

			priceSeries.set(i, result.getPriceMovementResult().getPrice());
			emaSeries.set(i, result.getEmaResult().getEma());

			FstoResult fstoResult = result.getFstoResult();
			kSeries.set(i, fstoResult.getFastK());
			dSeries.set(i, fstoResult.getFastD());

			obvSeries.set(i, result.getObvResult().getObv());

			atrSeries.set(i, result.getAtrResult().getAtr());
		}

		volumeModel.addSeries(volumeSeries);

		emaModel.addSeries(priceSeries);
		emaModel.addSeries(emaSeries);

		fstoModel.addSeries(kSeries);
		fstoModel.addSeries(dSeries);

		obvModel.addSeries(obvSeries);

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

	public BarChartModel getVolumeModel() {
		return volumeModel;
	}

	public LineChartModel getEmaModel() {
		return emaModel;
	}

	public LineChartModel getFstoModel() {
		return fstoModel;
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

		volumeModel = new BarChartModel();
		volumeModel.setTitle("Volume");
		volumeModel.setShadow(false);
		volumeModel.setAnimate(true);
		volumeModel.setZoom(true);
		volumeModel.setExtender("extendVolumeModel");

		emaModel = new LineChartModel();
		emaModel.setTitle("EMA");
		emaModel.setLegendPosition("nw");
		emaModel.setLegendCols(2);
		emaModel.setSeriesColors("008800, 000088");
		emaModel.setShadow(false);
		emaModel.setAnimate(true);
		emaModel.setZoom(true);
		emaModel.setExtender("extendEmaModel");

		fstoModel = new LineChartModel();
		fstoModel.setTitle("FSTO");
		fstoModel.setLegendPosition("nw");
		fstoModel.setLegendCols(2);
		fstoModel.setSeriesColors("000088, 880000");
		fstoModel.setShadow(false);
		fstoModel.setAnimate(true);
		fstoModel.setZoom(true);
		fstoModel.setExtender("extendFstoModel");

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
