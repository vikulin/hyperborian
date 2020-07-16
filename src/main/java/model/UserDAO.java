package model;

import java.sql.Timestamp;
import java.util.Collection;

import model.pojo.UserPojo;

/**
 * <p>
 * Generic DAO layer for Users
 * </p>
 * <p>
 * Generated at Tue Apr 14 19:54:58 EEST 2015
 * </p>
 *
 * @author Salto-db Generator v1.0.16 / Pojos + Hibernate mapping + Generic DAO
 * @see http://www.hibernate.org/328.html
 */
public interface UserDAO extends GenericDAO<UserPojo, Long> {

	/*
	 * TODO : Add specific businesses daos here. These methods will be
	 * overwrited if you re-generate this interface. You might want to extend
	 * this interface and to change the dao factory to return an instance of the
	 * new implemenation in buildUserDAO()
	 */

	/**
	 * Find User by username
	 */
	public Collection<UserPojo> findByUsername(String username);

	/**
	 * Find User by password
	 */
	public Collection<UserPojo> findByPassword(String password);

	/**
	 * Find User by createDate
	 */
	public Collection<UserPojo> findByCreateDate(Timestamp createDate);

}