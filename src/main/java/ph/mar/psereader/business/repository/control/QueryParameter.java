package ph.mar.psereader.business.repository.control;

import java.util.HashMap;
import java.util.Map;

public class QueryParameter {

	private Map<String, Object> parameters;

	private QueryParameter(String key, Object value) {
		parameters = new HashMap<>();
		parameters.put(key, value);
	}

	public static QueryParameter with(String key, Object value) {
		return new QueryParameter(key, value);
	}

	public QueryParameter and(String key, Object value) {
		parameters.put(key, value);
		return this;
	}

	public Map<String, Object> asParameters() {
		return parameters;
	}

}
