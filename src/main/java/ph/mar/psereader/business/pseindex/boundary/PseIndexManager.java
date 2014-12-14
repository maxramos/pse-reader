package ph.mar.psereader.business.pseindex.boundary;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import ph.mar.psereader.business.repository.control.Repository;

@Startup
@Singleton
public class PseIndexManager {

	@Inject
	Logger log;

	@Inject
	Repository repository;

}
