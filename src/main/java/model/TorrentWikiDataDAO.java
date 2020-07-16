package model;

import java.util.Collection;

import model.pojo.TorrentWikiData;

/**
 * <p>
 * Generic DAO layer for UserRoles
 * </p>
 * <p>
 * Generated at Tue Apr 14 19:54:58 EEST 2015
 * </p>
 *
 * @author Salto-db Generator v1.0.16 / Pojos + Hibernate mapping + Generic DAO
 * @see http://www.hibernate.org/328.html
 */
public interface TorrentWikiDataDAO extends GenericDAO<TorrentWikiData, Long> {

	/**
	 * Find TorrentWikiData by id
	 */
	public Collection<TorrentWikiData> findById(Long id);
	
	/**
	 * Find TorrentWikiData by wikiDataId
	 */
	public Collection<TorrentWikiData> findByWikiDataId(Long wikiDataId);
	
	
	public Collection<TorrentWikiData> findByWikiDataId(Long torrentId, Long wikiDataId);

	/**
	 * Find TorrentWikiData by torrentId
	 */
	public Collection<TorrentWikiData> findByTorrentId(Long torrentId);

	/**
	 * Find TorrentWikiData by languageCode
	 */
	public Collection<TorrentWikiData> findByLanguageCode(Long torrentId, String languageCode);

}