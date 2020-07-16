package model.hibernate;

import java.sql.Timestamp;
import java.util.Collection;

import model.UserDAO;
import model.pojo.User;
import model.pojo.UserPojo;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * <p>Hibernate DAO layer for Users</p>
 * <p>Generated at Thu Mar 22 14:32:50 EET 2012</p>
 *
 * @author Salto-db Generator v1.0.16 / Pojos + Hibernate mapping + Generic DAO
 * @see http://www.hibernate.org/328.html
 */
public class UserHibernateDAO extends
		GenericHibernateDAO<UserPojo, Long> implements
		UserDAO, UserDetailsService {

	/**
	 * Find User by username
	 */
	public Collection<UserPojo> findByUsername(String username) {
		return findByCriteria(Restrictions.eq("username", username));
	}
	
	/**
	 * Find User by password
	 */
	public Collection<UserPojo> findByPassword(String password) {
		return findByCriteria(Restrictions.eq("password", password));
	}
	
	/**
	 * Find User by createDate
	 */
	public Collection<UserPojo> findByCreateDate(Timestamp createDate) {
		return findByCriteria(Restrictions.eq("createDate", createDate));
	}
	
	public Collection<UserPojo> findByLoginWithRole(String login) {
		return findByCriteriaWith(Restrictions.eq("username", login), "username");
	}
	
	public UserDetails loadUserByUsername(String userName)
			throws UsernameNotFoundException, DataAccessException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		setSession(session);
		Collection<UserPojo> results = findByLoginWithRole(userName);
		session.getTransaction().commit();
		if(results.size() == 0) {
            throw new UsernameNotFoundException(userName + "not found");
		}
		UserPojo userPojo = results.iterator().next();
		return (UserDetails)(new User(userPojo));
	}
	

}
