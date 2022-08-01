package cerberus.core.persistence.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import cerberus.core.persistence.EntityDao;

public abstract class AbstractDao<T extends Serializable, KEY> implements
		EntityDao<T, KEY> {

	private static final long serialVersionUID = 4897040151320828031L;

	private EntityManager entityManager = null;
	private Class<T> clazzInfo = null;

	public AbstractDao() {
	}

	public AbstractDao(EntityManager em, Class<T> clazz) {
		setEntityManager(em);
		setClazzInfo(clazz);
	}

	public T findByID(KEY key) {
		return getEntityManager().find(getClazzInfo(), key);
	}

	@Override
	public void remove(T item) {
		try {
			getEntityManager().getTransaction().begin();
			getEntityManager().remove(item);
			getEntityManager().getTransaction().commit();
		} catch (Exception e) {
			if (getEntityManager().getTransaction().isActive()) {
				getEntityManager().getTransaction().rollback();
			}
			throw new RuntimeException(e);
		}
	}

	@Override
	public void insert(T item) {
		try {
			getEntityManager().getTransaction().begin();
			getEntityManager().persist(item);
			getEntityManager().getTransaction().commit();
		} catch (Exception e) {
			if (getEntityManager().getTransaction().isActive()) {
				getEntityManager().getTransaction().rollback();
			}
			throw new RuntimeException(e);
		}
	}

	@Override
	public void update(T item) {
		try {
			getEntityManager().getTransaction().begin();
			getEntityManager().flush();
			getEntityManager().getTransaction().commit();
		} catch (Exception e) {
			if (getEntityManager().getTransaction().isActive()) {
				getEntityManager().getTransaction().rollback();
			}
			throw new RuntimeException(e);
		}
	}

	private List<T> findAllTypeSafe() {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(getClazzInfo());
		Root<T> rootEntry = cq.from(getClazzInfo());
		CriteriaQuery<T> all = cq.select(rootEntry);
		TypedQuery<T> allQuery = getEntityManager().createQuery(all);
		List<T> list = allQuery.getResultList();
		if (list == null)
			list = Collections.emptyList();
		return list;
	}

	@Override
	public List<T> findAll() {
		return findAllTypeSafe();
	}

	@Override
	public void close() {
		getEntityManager().close();
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Class<T> getClazzInfo() {
		return clazzInfo;
	}

	public void setClazzInfo(Class<T> clazzInfo) {
		this.clazzInfo = clazzInfo;
	}

}
