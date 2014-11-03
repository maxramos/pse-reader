package ph.mar.psereader.business.indicator.control;

import javax.ejb.ApplicationException;

@ApplicationException
public class IndicatorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IndicatorException() {
		super();
	}

	public IndicatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IndicatorException(String message, Throwable cause) {
		super(message, cause);
	}

	public IndicatorException(String message) {
		super(message);
	}

	public IndicatorException(Throwable cause) {
		super(cause);
	}

}
