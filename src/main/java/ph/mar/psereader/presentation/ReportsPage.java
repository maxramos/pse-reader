package ph.mar.psereader.presentation;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import ph.mar.psereader.business.market.boundary.MarketManager;
import ph.mar.psereader.business.report.boundary.PseReportManager;
import ph.mar.psereader.business.report.entity.PseReportRow;

@Named
@ViewScoped
public class ReportsPage implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");

	@Inject
	Logger log;

	@Inject
	MarketManager marketManager;

	@Inject
	PseReportManager pseReportManager;

	private List<String> dates;
	private String selectedDate;
	private List<PseReportRow> rows;
	private List<PseReportRow> filteredRows;

	@PostConstruct
	void init() {
		dates = new ArrayList<>();

		for (Date date : marketManager.findAllDates()) {
			dates.add(DATE_FORMAT.format(date));
		}

		if (!dates.isEmpty()) {
			selectedDate = dates.get(0);
			onDateChange();
		}
	}

	public void onDateChange() {
		try {
			rows = pseReportManager.findAllRowsByDate(DATE_FORMAT.parse(selectedDate));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getDates() {
		return dates;
	}

	public String getSelectedDate() {
		return selectedDate;
	}

	public void setSelectedDate(String selectedDate) {
		this.selectedDate = selectedDate;
	}

	public List<PseReportRow> getRows() {
		return rows;
	}

	public List<PseReportRow> getFilteredRows() {
		return filteredRows;
	}

	public void setFilteredRows(List<PseReportRow> filteredRows) {
		this.filteredRows = filteredRows;
	}

}
