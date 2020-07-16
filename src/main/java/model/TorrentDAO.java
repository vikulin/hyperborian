package model;

import java.util.Collection;
import java.util.Set;

import model.pojo.Torrent;
import org.hyperborian.bt.pojo.classifier.TorrentClassifier;

/**
 * <p>Generic DAO layer for Sells</p>
 * <p>Generated at Mon Sep 19 10:34:20 EEST 2016</p>
 *
 * @author Salto-db Generator v1.0.16 / Pojos + Hibernate mapping + Generic DAO
 * @see http://www.hibernate.org/328.html
 */

public interface TorrentDAO extends GenericDAO<Torrent,Long> {

	/**
	 * Find Torrent by name
	 */
	public Collection<Torrent> findByName(String name, int offset, int limit);
	/**
	 * Find Torrent by totalSize
	 */
	public Collection<Torrent> findByTotalSize(Long from, Long to,int offset, int limit);
	
	public Collection<Torrent> findTopDownloads(int offset, int limit);
	
	public Collection<Torrent> findSimilarTorrent(Torrent torrent);
	
	public Collection<Torrent> findSimilarTorrent(Long wikiDataId, int offset, int limit);
	
	public Collection<Torrent> findByCriteria(String name, Set<TorrentClassifier.Type> categories, Double sizeFrom, Double sizeTo, int offset, int limit);
	
	public Collection<Torrent> findByCriteriaNoWiki(String name, Set<TorrentClassifier.Type> categories, Double sizeFrom, Double sizeTo, int offset, int limit);
	
	public Collection<Torrent> findAllDocuments(int offset, int limit);
	
	public Collection<Torrent> findByInfoHash(String infoHash);


}