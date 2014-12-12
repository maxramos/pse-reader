package ph.mar.psereader.business.report.control;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.junit.Test;

public class PseReportReaderTest {

	@Test
	public void shouldReturnContent() throws FileNotFoundException {
		PseReportReader reader = new PseReportReader();
		String report = reader.read("/home/mar/stockQuotes_12112014.pdf");

		try (PrintWriter printer = new PrintWriter("/home/mar/psereport.txt")) {
			printer.print(report);
		}
	}

}
