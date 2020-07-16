package model.pojo;

import java.io.Serializable;
import java.sql.Timestamp;

import model.pojo.interfaces.Pojo;


/**
 * <p>Pojo mapping TABLE user_role</p>
 * <p></p>
 *
 * <p>Generated at Thu Mar 22 14:32:50 EET 2012</p>
 * @author Salto-db Generator v1.0.16 / Hibernate pojos and xml mapping files.
 * 
 */
public class UserRole implements Serializable, Pojo {

	/**
	 * Attribute id.
	 */
	private Long id;
	
	/**
	 * Attribute createTime.
	 */
	private Timestamp createTime;
	
	/**
	 * Attribute user
	 */
	 private UserPojo user;	

	/**
	 * Attribute role
	 */
	 private Role role;	

	
	/**
	 * <p> 
	 * </p>
	 * @return id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id new value for id 
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * get user
	 */
	public UserPojo getUser() {
		return this.user;
	}
	
	/**
	 * set user
	 */
	public void setUser(UserPojo user) {
		this.user = user;
	}

	/**
	 * get role
	 */
	public Role getRole() {
		return this.role;
	}
	
	/**
	 * set role
	 */
	public void setRole(Role role) {
		this.role = role;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return createDate
	 */
	public Timestamp getCreateTime() {
		return createTime;
	}

	/**
	 * @param createDate new value for createDate 
	 */
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}



}