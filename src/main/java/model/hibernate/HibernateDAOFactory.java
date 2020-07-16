package model.hibernate;

import model.DAOFactory;
import model.RoleDAO;
import model.UserDAO;
import model.UserRoleDAO;

/**
 * Generated at Thu Mar 22 14:32:50 EET 2012
 *
 * @see http://www.hibernate.org/43.html
 * @author Salto-db Generator v1.0.16 / Pojos + Hibernate mapping + Generic DAO
 */
public class HibernateDAOFactory extends DAOFactory {
	
	/* (non-Javadoc)
	 * @see model.DAOFactory#buildRoleDAO()
	 */
	@Override
	public RoleDAO buildRoleDAO() {
		return new RoleHibernateDAO();
	}
	
	/* (non-Javadoc)
	 * @see model.DAOFactory#buildUserDAO()
	 */
	@Override
	public UserDAO buildUserDAO() {
		return new UserHibernateDAO();
	}
	
	/* (non-Javadoc)
	 * @see model.DAOFactory#buildUserRoleDAO()
	 */
	@Override
	public UserRoleDAO buildUserRoleDAO() {
		return new UserRoleHibernateDAO();
	}
	
}
