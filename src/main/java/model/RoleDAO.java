package model;

import java.sql.Timestamp;
import java.util.Collection;

import model.pojo.Role;

/**
 * <p>
 * Generic DAO layer for Roles
 * </p>
 * <p>
 * Generated at Tue Apr 14 19:54:58 EEST 2015
 * </p>
 *
 * @author Salto-db Generator v1.0.16 / Pojos + Hibernate mapping + Generic DAO
 * @see http://www.hibernate.org/328.html
 */
public interface RoleDAO extends GenericDAO<Role, Long> {

	/*
	 * TODO : Add specific businesses daos here. These methods will be
	 * overwrited if you re-generate this interface. You might want to extend
	 * this interface and to change the dao factory to return an instance of the
	 * new implemenation in buildRoleDAO()
	 */
	/**
	 * Find Role by name
	 */
	public Collection<Role> findByName(String name);

	/**
	 * Find Role by createDate
	 */
	public Collection<Role> findByCreateDate(Timestamp createDate);

}