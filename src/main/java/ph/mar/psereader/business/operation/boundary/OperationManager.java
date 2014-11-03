package ph.mar.psereader.business.operation.boundary;

import static ph.mar.psereader.business.repository.control.QueryParameter.with;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import ph.mar.psereader.business.indicator.boundary.IndicatorManager;
import ph.mar.psereader.business.operation.entity.Settings;
import ph.mar.psereader.business.report.entity.PseReport;
import ph.mar.psereader.business.repository.control.Repository;
import ph.mar.psereader.business.stock.entity.Quote;

@Startup
@Singleton
public class OperationManager {

	@Inject
	Logger log;

	@Inject
	Repository repository;

	@Inject
	IndicatorManager indicatorManager;

	@PostConstruct
	void init() {
		List<Settings> settingsList = repository.find(Settings.ALL, Settings.class);

		try {
			if (settingsList.isEmpty()) {
				Settings settings = new Settings(Quote.DATE_FORMAT.parse("2014-01-01"), Quote.DATE_FORMAT.parse("2013-12-01"));
				repository.add(settings);
			}
		} catch (ParseException e) {
			throw new EJBException(e);
		}
	}

	@Asynchronous
	public void run() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		log.info("Operation started.");
		Settings settings = findLatestSettings();
		List<Date> reportDates = repository.find(PseReport.ALL_DATES_BY_DATE, with("date", settings.getLastProcessedDate()).asParameters(),
				Date.class);

		for (Date date : reportDates) {
			try {
				log.info("Processing Report {}.", Quote.DATE_FORMAT.format(date));
				indicatorManager.process(date);
			} catch (EJBException e) {
				stopWatch.stop();
				log.error("Processing Report {} interrupted after {}", Quote.DATE_FORMAT.format(date), stopWatch.toString());
				throw e;
			}
		}

		stopWatch.stop();
		log.info("Operation finished after {}", stopWatch.toString());
	}

	public Date findLastProcessedDate() {
		Settings settings = findLatestSettings();
		return settings == null ? null : settings.getLastProcessedDate();
	}

	public Settings findLatestSettings() {
		List<Settings> settingsList = repository.find(Settings.ALL, Settings.class);

		if (settingsList.isEmpty()) {
			return null;
		}

		return settingsList.get(0);
	}

}
