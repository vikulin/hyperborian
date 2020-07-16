package model.hibernate;

import java.util.Collection;

import model.UserRoleDAO;
import model.pojo.UserRole;

import org.hibernate.criterion.Restrictions;

/**
 * <p>
 * Hibernate DAO layer for UserRoles
 * </p>
 * <p>
 * Generated at Tue Apr 14 19:54:58 EEST 2015
 * </p>
 *
 * @author Salto-db Generator v1.0.16 / Pojos + Hibernate mapping + Generic DAO
 * @see http://www.hibernate.org/328.html
 */
public class UserRoleHibernateDAO extends GenericHibernateDAO<UserRole, Long>
		implements UserRoleDAO {

	/**
	 * Find UserRole by id
	 */
	public Collection<UserRole> findById(Long id) {
		return findByCriteria(Restrictions.eq("id", id));
	}

	/**
	 * Find UserRole by userId
	 */
	public Collection<UserRole> findByUserId(Long userId) {
		return findByCriteria(Restrictions.eq("user.id", userId));
	}

	/**
	 * Find UserRole by roleId
	 */
	public Collection<UserRole> findByRoleId(Long roleId) {
		return findByCriteria(Restrictions.eq("role.id", roleId));
	}

}
