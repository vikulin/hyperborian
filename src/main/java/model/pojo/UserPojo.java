package model.pojo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import model.pojo.interfaces.Pojo;

/**
 * <p>Pojo mapping TABLE user</p>
 * <p></p>
 *
 * <p>Generated at Thu Mar 22 14:32:50 EET 2012</p>
 * @author Salto-db Generator v1.0.16 / Hibernate pojos and xml mapping files.
 * 
 */
public class UserPojo implements Serializable, Pojo {

	private static final long serialVersionUID = 1L;

	/**
	 * Attribute id.
	 */
	private Long id;
	
	/**
	 * Attribute username.
	 */
	private String username;
	
	/**
	 * Attribute email.
	 */
	private String email;
	
	/**
	 * Attribute password.
	 */
	private String password;
	
	/**
	 * Attribute createTime.
	 */
	private Timestamp createTime;
	
	/**
	 * Collection of UserRole
	 */
	private Collection<UserRole> userRoles = null;
	

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
	 * <p> 
	 * </p>
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username new value for username 
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password new value for password 
	 */
	public void setPassword(String password) {
		this.password = password;
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
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Get the list of UserRole
	 */
	 public Collection<UserRole> getUserRoles() {
	 	return this.userRoles;
	 }
	 
	/**
	 * Set the list of UserRole
	 */
	 public void setUserRoles(Set<UserRole> userRoles) {
	 	this.userRoles = userRoles;
	 }

}