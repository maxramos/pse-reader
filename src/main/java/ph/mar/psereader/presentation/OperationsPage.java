package ph.mar.psereader.presentation;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ph.mar.psereader.business.operation.boundary.OperationManager;
import ph.mar.psereader.business.operation.entity.Settings;
import ph.mar.psereader.presentation.util.MessageUtil;

@Named
@ViewScoped
public class OperationsPage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	OperationManager operationManager;

	private Settings settings;

	@PostConstruct
	void init() {
		settings = operationManager.findLatestSettings();
	}

	public void run() {
		operationManager.run();
		MessageUtil.sendInfo("Report processing started.");
	}

	public Settings getSettings() {
		return settings;
	}

}
