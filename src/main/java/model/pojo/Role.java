package model.pojo;

import java.util.List;
import java.io.Serializable;
import java.sql.Timestamp;

import model.pojo.interfaces.Pojo;


/**
 * <p>Pojo mapping TABLE role</p>
 * <p></p>
 *
 * <p>Generated at Thu Mar 22 14:32:50 EET 2012</p>
 * @author Salto-db Generator v1.0.16 / Hibernate pojos and xml mapping files.
 * 
 */
public class Role implements Serializable, Pojo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8806343924433119573L;

	/**
	 * Attribute id.
	 */
	private Long id;
	
	/**
	 * Attribute name.
	 */
	private String name;
	
	/**
	 * Attribute createDate.
	 */
	private Timestamp createTime;
	
	/**
	 * List of UserRole
	 */
	private List<UserRole> userRoles = null;

	
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
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name new value for name 
	 */
	public void setName(String name) {
		this.name = name;
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
	
	/**
	 * Get the list of UserRole
	 */
	 public List<UserRole> getUserRoles() {
	 	return this.userRoles;
	 }
	 
	/**
	 * Set the list of UserRole
	 */
	 public void setUserRoles(List<UserRole> userRoles) {
	 	this.userRoles = userRoles;
	 }


}