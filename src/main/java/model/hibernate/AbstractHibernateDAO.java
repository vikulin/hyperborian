package model.hibernate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import model.GenericDAO;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 * Generated at Thu Mar 22 14:32:50 EET 2012
 *
 * @author Salto-db Generator v1.0.16 / Pojos + Hibernate mapping + Generic DAO
 * @see http://www.hibernate.org/328.html
 */
public abstract class AbstractHibernateDAO<T, ID extends Serializable> implements GenericDAO<T, ID> {

	private Session session;

	private Class<T> persistentClass;

	public AbstractHibernateDAO() {
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

    public void setSession(Session session) {
		this.session = session;
	}

	protected Session getSession() {
		if (session == null || !session.isConnected())
           session = HibernateUtil.getSessionFactory().getCurrentSession();
       return session;
    }

	public Class<T> getPersistentClass() {
        return persistentClass;
    }

	@SuppressWarnings("unchecked")
	public T getById(ID id) {
		return (T) getSession().get(getPersistentClass(), id);
	}

	@SuppressWarnings("unchecked")
	public T getById(ID id, boolean lock) {
		if (lock) {
			return (T) getSession().get(getPersistentClass(), id,
					LockOptions.UPGRADE);
		} else
			return getById(id);
	}

	@SuppressWarnings("unchecked")
	public T loadById(ID id) {
		return (T) getSession().load(getPersistentClass(), id);
	}

	public void save(T entity) {
		getSession().save(entity);
	}

	public void update(T entity) {
		getSession().update(entity);
	}

	public void saveOrUpdate(T entity) {
		getSession().saveOrUpdate(entity);
	}

	public void delete(T entity) {
		getSession().delete(entity);
	}

	public void deleteById(ID id) 	{
		getSession().delete(loadById(id));
	}

	public List<T> findAll() {
        return findByCriteria();
    }
	
	/**
     * Use this inside subclasses as a convenience method.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        for (Criterion c : criterion) {
            crit.add(c);
        }
        return crit.list();
   }
   
   	/**
 	 * Find by criteria.
	 */
	@SuppressWarnings("unchecked")
	public List<T> findByCriteria(Map criterias) {
	
		Criteria criteria = getSession().createCriteria(getPersistentClass());
		criteria.add(Restrictions.allEq(criterias));
		return criteria.list();
	}
	
	/**
	 * This method will execute an HQL query and return the number of affected entities.
	 */
	protected int executeQuery(String query, String namedParams[],	Object params[]) {
		Query q = getSession().createQuery(query);
		
		if (namedParams != null) {
			for (int i = 0; i < namedParams.length; i++) {
				q.setParameter(namedParams[i], params[i]);
			}
		}

		return q.executeUpdate();
	}
	
	protected int executeQuery(String query) {
		return executeQuery(query, null, null);
	}
	
	/**
	 * This method will execute a Named HQL query and return the number of affected entities.
	 */
	protected int executeNamedQuery(String namedQuery, String namedParams[],	Object params[]) {
		Query q = getSession().getNamedQuery(namedQuery);
		
		if (namedParams != null) {
			for (int i = 0; i < namedParams.length; i++) {
				q.setParameter(namedParams[i], params[i]);
			}
		}

		return q.executeUpdate();
	}
	
	protected int executeNamedQuery(String namedQuery) {
		return executeNamedQuery(namedQuery, null, null);
	}
	
	@SuppressWarnings("unchecked")
    public List<T> findByExample(T exampleInstance, String[] excludeProperty) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        Example example =  Example.create(exampleInstance).excludeZeroes().enableLike().ignoreCase();
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        return crit.list();
    }
    
	public List<T> findByCriteriaWith(Object... criterion) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        for (Object c : criterion) {
        	if (c instanceof Criterion)
        		crit.add((Criterion)c);
        	else
        		if (c instanceof String)
        			crit.setFetchMode(c.toString(), FetchMode.JOIN);
        }
        return crit.list();
   }
}
