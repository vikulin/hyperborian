package model;

import model.hibernate.HibernateDAOFactory;

/**
 * Generated at Thu Mar 22 14:32:50 EET 2012
 *
 * @see http://www.hibernate.org/328.html
 * @author Salto-db Generator v1.0.16 / Pojos + Hibernate mapping + Generic DAO
 */

public abstract class DAOFactory {

	private static final DAOFactory HIBERNATE = new HibernateDAOFactory();

	public static final DAOFactory DEFAULT = HIBERNATE;
	
    /**
     * Factory method for instantiation of concrete factories.
     */
    public static DAOFactory instance(Class factory) {
        try {
            return (DAOFactory)factory.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't create DAOFactory: " + factory);
        }
    }
	
	public abstract RoleDAO buildRoleDAO();
	
	public abstract UserDAO buildUserDAO();
	
	public abstract UserRoleDAO buildUserRoleDAO();
	
}
