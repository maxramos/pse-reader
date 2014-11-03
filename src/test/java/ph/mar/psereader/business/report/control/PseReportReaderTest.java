package ph.mar.psereader.business.report.control;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.junit.Test;

import ph.mar.psereader.business.report.control.PseReportReader;
import ph.mar.psereader.business.report.entity.PseReport;
import ph.mar.psereader.business.report.entity.PseReportRow;

public class PseReportReaderTest {

	@Test
	public void shouldReturnContent() throws FileNotFoundException {
		PseReportReader reader = new PseReportReader();
		PseReport report = reader.read("/home/mar/stockQuotes_09262014.pdf");

		try (PrintWriter printer = new PrintWriter("/home/mar/psereport.txt")) {
			printer.println("SIZE: " + report.getRows().size());

			for (PseReportRow row : report.getRows()) {
				printer.format("%-5s   %7s   %7s   %7s   %7s   %12s\n", row.getSymbol(), row.getOpen(), row.getHigh(), row.getLow(), row.getClose(),
						row.getVolume());
			}
		}
	}

}
