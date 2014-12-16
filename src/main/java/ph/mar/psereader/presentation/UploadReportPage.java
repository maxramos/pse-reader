package ph.mar.psereader.presentation;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.pdfbox.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;

import ph.mar.psereader.business.report.boundary.PseReportManager;
import ph.mar.psereader.presentation.util.MessageUtil;

@Named
@ViewScoped
public class UploadReportPage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	Logger log;

	@Inject
	PseReportManager pseReportManager;

	private Map<String, byte[]> fileMap = new TreeMap<>();
	private Set<String> fileNames = new TreeSet<>();

	public void onUpload(FileUploadEvent event) throws IOException {
		UploadedFile file = event.getFile();
		String fileName = file.getFileName();
		byte[] content = IOUtils.toByteArray(file.getInputstream());

		fileMap.put(fileName, content);
		fileNames.add(fileName);

		log.info("File {} uploaded.", fileName);
	}

	public void save() {
		List<byte[]> files = new ArrayList<>(fileMap.values());
		Map<String, Integer> results = pseReportManager.addAll(files);
		fileMap = new TreeMap<>();
		fileNames = new TreeSet<>();
		List<String> messages = new ArrayList<>();

		log.info("{} new report/s added.", results.get("reports"));
		messages.add(String.format("%s new report/s added.", results.get("reports")));

		if (results.get("stocks") != 0) {
			log.info("{} new stock/s added.", results.get("stocks"));
			messages.add(String.format("%s new stock/s added.", results.get("stocks")));
		}

		if (results.get("suspended") != 0) {
			log.info("{} stock/s suspended.", results.get("suspended"));
			messages.add(String.format("%s stock/s suspended.", results.get("suspended")));
		}

		if (results.get("indices") != 0) {
			log.info("{} indices added.", results.get("indices"));
			messages.add(String.format("%s indices added.", results.get("indices")));
		}

		MessageUtil.sendInfo(messages);

	}

	public Set<String> getFileNames() {
		return fileNames;
	}

}
