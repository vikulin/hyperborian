package model.hibernate;

import java.util.Collection;

import org.hibernate.criterion.Restrictions;

import model.TorrentWikiDataDAO;
import model.pojo.TorrentWikiData;

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
public class TorrentWikiDataHibernateDAO extends GenericHibernateDAO<TorrentWikiData, Long>
		implements TorrentWikiDataDAO {

	@Override
	public Collection<TorrentWikiData> findById(Long id) {
		return findByCriteria(Restrictions.eq("id", id));
	}

	
	@Override
	public Collection<TorrentWikiData> findByWikiDataId(Long wikiDataId) {
		return findByCriteria(Restrictions.eq("wikiDataId", wikiDataId));
	}
	
	@Override
	public Collection<TorrentWikiData> findByWikiDataId(Long torrentId, Long wikiDataId) {
		return findByCriteria(Restrictions.and(Restrictions.eq("torrentId", torrentId),Restrictions.eq("wikiDataId", wikiDataId)));
	}

	@Override
	public Collection<TorrentWikiData> findByTorrentId(Long torrentId) {
		return findByCriteria(Restrictions.eq("torrentId", torrentId));
	}

	@Override
	public Collection<TorrentWikiData> findByLanguageCode(Long torrentId, String languageCode) {
		return findByCriteria(Restrictions.and(Restrictions.eq("torrentId", torrentId),Restrictions.eq("languageCode", languageCode)));
	}

}
