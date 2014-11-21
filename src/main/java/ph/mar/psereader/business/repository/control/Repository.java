package ph.mar.psereader.business.repository.control;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.Dependent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

@Dependent
public class Repository {

	@PersistenceContext
	EntityManager em;

	public <T> T add(T t) {
		em.persist(t);
		em.flush();
		em.refresh(t);
		return t;
	}

	public <T> T update(T t) {
		T updatedT = em.merge(t);
		em.flush();
		return updatedT;
	}

	public <T> void remove(Class<T> type, Object id) {
		Object ref = em.getReference(type, id);
		em.remove(ref);
		em.flush();
	}

	public <T> void detach(T t) {
		em.detach(t);
	}

	public <T> T find(Class<T> type, Object id) {
		return em.find(type, id);
	}

	public <T> List<T> find(String queryName, Class<T> type) {
		return find(queryName, null, type);
	}

	public <T> List<T> find(String queryName, Map<String, Object> parameters, Class<T> type) {
		TypedQuery<T> query = em.createNamedQuery(queryName, type);

		if (parameters != null && !parameters.isEmpty()) {
			for (Entry<String, Object> parameter : parameters.entrySet()) {
				query.setParameter(parameter.getKey(), parameter.getValue());
			}
		}

		return query.getResultList();
	}

	public <T> List<T> find(String queryName, Class<T> type, int maxResult) {
		return find(queryName, null, type, maxResult);
	}

	public <T> List<T> find(String queryName, Map<String, Object> parameters, Class<T> type, int maxResult) {
		TypedQuery<T> query = em.createNamedQuery(queryName, type);

		if (parameters != null && !parameters.isEmpty()) {
			for (Entry<String, Object> parameter : parameters.entrySet()) {
				query.setParameter(parameter.getKey(), parameter.getValue());
			}
		}

		return query.setMaxResults(maxResult).getResultList();
	}

	public void flush() {
		em.flush();
	}

	public int bulkUpdate(String queryName) {
		return bulkUpdate(queryName, null);
	}

	public int bulkUpdate(String queryName, Map<String, Object> parameters) {
		Query query = em.createNamedQuery(queryName);

		if (parameters != null && !parameters.isEmpty()) {
			for (Entry<String, Object> parameter : parameters.entrySet()) {
				query.setParameter(parameter.getKey(), parameter.getValue());
			}
		}

		return query.executeUpdate();
	}

	public Object[] execute(String queryName, Map<String, Object> parameters) {
		TypedQuery<Object[]> query = em.createNamedQuery(queryName, Object[].class);

		if (parameters != null && !parameters.isEmpty()) {
			for (Entry<String, Object> parameter : parameters.entrySet()) {
				query.setParameter(parameter.getKey(), parameter.getValue());
			}
		}

		return query.getSingleResult();
	}

}
