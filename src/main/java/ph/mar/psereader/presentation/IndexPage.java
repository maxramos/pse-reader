package ph.mar.psereader.presentation;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import ph.mar.psereader.business.report.boundary.PseReportManager;
import ph.mar.psereader.business.report.entity.PseMarketSummary;
import ph.mar.psereader.business.report.entity.PseReport;

@Named
@ViewScoped
public class IndexPage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	Logger log;

	@Inject
	PseReportManager pseReportManager;

	private PseReport report;
	private PseMarketSummary marketSummary;

	@PostConstruct
	void init() {
		report = pseReportManager.findLatestReport();
		
		if (report != null) {			
			marketSummary = report.getMarketSummary();
		}
	}

	public PseReport getReport() {
		return report;
	}

	public PseMarketSummary getMarketSummary() {
		return marketSummary;
	}

}
