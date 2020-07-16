package model.hibernate;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hyperborian.bt.pojo.TorrentInfoHashIndex;

import model.TorrentInfoHashIndexDAO;

public class TorrentInfoHashIndexHibernateDAO extends GenericHibernateDAO<TorrentInfoHashIndex, Long> implements TorrentInfoHashIndexDAO {
	
	public Collection<TorrentInfoHashIndex> findAllMostSeeding(int offset, int limit) {
		Criteria c = createCriteria();
		c.addOrder(Order.desc("seeds"));
		c.setFirstResult(offset).setMaxResults(limit);
        return findByCriteriaWith(c);
    }
	
	public Collection<TorrentInfoHashIndex> findAllLeastSeeding(int offset, int limit) {
		Criteria c = createCriteria();
		c.addOrder(Order.asc("seeds"));
		c.setFirstResult(offset).setMaxResults(limit);
        return findByCriteriaWith(c);
    }

}
