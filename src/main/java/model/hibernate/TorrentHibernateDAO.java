package model.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import model.pojo.Torrent;
import model.pojo.TorrentWikiData;

import org.hyperborian.bt.pojo.classifier.TorrentClassifier;
import org.hyperborian.bt.pojo.classifier.TorrentClassifier.Type;

import model.TorrentDAO;

/**
 * <p>Hibernate DAO layer for Sells</p>
 * <p>Generated at Mon Sep 19 10:34:20 EEST 2016</p>
 *
 * @author Salto-db Generator v1.0.16 / Pojos + Hibernate mapping + Generic DAO
 * @see http://www.hibernate.org/328.html
 */
public class TorrentHibernateDAO extends
		GenericHibernateDAO<Torrent, Long> implements
		TorrentDAO {
	

	/**
	 * Find Torrent by name
	 */
	public Collection<Torrent> findByName(String name, int offset, int limit) {
		Criteria c = createCriteria(offset, limit);
		c.add(Restrictions.ilike("name", "%"+name+"%"));
		Collection<Torrent> torrents = findByCriteriaWith(c);
		return torrents;
	}
	
	/**
	 * Find Torrent by totalSize
	 */
	public Collection<Torrent> findByTotalSize(Long from, Long to, int offset, int limit) {
		Criteria c = createCriteria(offset, limit);
		c.add(Restrictions.between("totalSize", from, to));
		Collection<Torrent> torrents = findByCriteriaWith(c);
		return torrents;
	}
	
	/**
	 * Find to Torrent downloads
	 */
	public Collection<Torrent> findTopDownloads(int offset, int limit) {
		Criteria c = createCriteria(offset, limit);
		c.addOrder(Order.desc("downloads"));
		c.addOrder(Order.desc("id"));
		Collection<Torrent> torrents = findByCriteriaWith(c);
		return torrents;
	}
	
	/**
	 * Find similar Torrents
	 */
	public Collection<Torrent> findSimilarTorrent(Torrent torrent) {
		if(torrent.getTorrentWikiData().size()==0) {
			return new ArrayList<Torrent>();
		}
		TorrentWikiData torrentWikiData = torrent.getTorrentWikiData().iterator().next();
		Criteria c = createCriteria();
		c.createAlias("torrentWikiData", "wikiData");
		//c.createAlias("wikiData.wikiDataId", "wikiDataId");
		c.add(Restrictions.eq("wikiData.wikiDataId", torrentWikiData.getWikiDataId()));
		c.addOrder(Order.desc("seeds"));
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		Collection<Torrent> torrents = findByCriteriaWith(c);
		return torrents;
	}
	
	/**
	 * Find similar Torrents
	 */
	public Collection<Torrent> findSimilarTorrent(Long wikiDataId, int offset, int limit) {

		Criteria c = createCriteria(offset, limit);
		c.createAlias("torrentWikiData", "wikiData");
		//c.createAlias("wikiData.wikiDataId", "wikiDataId");
		c.add(Restrictions.eq("wikiData.wikiDataId", wikiDataId));
		c.addOrder(Order.desc("seeds"));
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		Collection<Torrent> torrents = findByCriteriaWith(c);
		return torrents;
	}
	
	/**
	 * Find Torrent by name and category
	 */
	public Collection<Torrent> findByCriteria(String name, Set<TorrentClassifier.Type> categories, Double sizeFrom, Double sizeTo, int offset, int limit) {
		Criteria c = createCriteria(offset, limit);
		if (name!=null && !"".equals(name)){
			c.add(Restrictions.ilike("ngram", "%"+name+"%"));
		}
		
		if(sizeFrom!=null && sizeFrom>0.0) {
			c.add(Restrictions.ge("totalSize", new Double(sizeFrom*1073741824.0).longValue()));
		}
		if(sizeTo!=null && sizeTo>0.0) {
			c.add(Restrictions.le("totalSize", new Double(sizeTo*1073741824.0).longValue()));
		}
		c.addOrder(Order.desc("seeds"));
		c.addOrder(Order.desc("id"));
		Collection<Torrent> torrents = new ArrayList<Torrent>();
		if(categories!=null && !categories.isEmpty()) {
			for(Type type:categories) {
				switch (type) {
				case SOFTWARE:
					c.add(Restrictions.eq ("software", true));
					c.add(Restrictions.eq ("video", false));
					c.add(Restrictions.eq ("audio", false));
					break;
				case VIDEO:
					c.add(Restrictions.eq ("video", true));
					c.add(Restrictions.eq ("software", false));
					c.add(Restrictions.eq ("archive", false));
					c.add(Restrictions.eq ("diskImage", false));
					break;
				case AUDIO:
					c.add(Restrictions.eq ("audio", true));
					c.add(Restrictions.eq ("software", false));
					c.add(Restrictions.eq ("archive", false));
					c.add(Restrictions.eq ("diskImage", false));
					break;
				case IMAGES:
					c.add(Restrictions.eq ("image", true));
					c.add(Restrictions.eq ("software", false));
					c.add(Restrictions.eq ("video", false));
					c.add(Restrictions.eq ("audio", false));
					c.add(Restrictions.eq ("archive", false));
					c.add(Restrictions.eq ("diskImage", false));
					break;
				case DOCUMENTS:
					c.add(Restrictions.eq ("document", true));
					c.add(Restrictions.eq ("software", false));
					c.add(Restrictions.eq ("video", false));
					c.add(Restrictions.eq ("audio", false));
					c.add(Restrictions.eq ("archive", false));
					c.add(Restrictions.eq ("diskImage", false));
					break;
				case ARCHIVES:
					c.add(Restrictions.eq ("archive", true));
					c.add(Restrictions.eq ("software", false));
					c.add(Restrictions.eq ("video", false));
					c.add(Restrictions.eq ("audio", false));
					break;
				case DISK_IMAGES:
					c.add(Restrictions.eq ("diskImage", true));
					c.add(Restrictions.eq ("software", false));
					c.add(Restrictions.eq ("video", false));
					c.add(Restrictions.eq ("audio", false));
					break;				
				case ALL:
					c.add(Restrictions.eq ("archive", false));
					break;	
				default:
					break;
				}
				Collection<Torrent> t = findByCriteriaWith(c);
				torrents.addAll(t);
			}

		} else {
			Collection<Torrent> t = findByCriteriaWith(c);
			torrents.addAll(t);
		}

		return torrents;
	}
	
	/**
	 * Find Torrent by name and category and no wiki data
	 */
	public Collection<Torrent> findByCriteriaNoWiki(String name, Set<TorrentClassifier.Type> categories, Double sizeFrom, Double sizeTo, int offset, int limit) {
		Criteria c = createCriteria(offset, limit);
		if (name!=null && !"".equals(name)){
			c.add(Restrictions.ilike("ngram", "%"+name+"%"));
		}
		
		if(sizeFrom!=null && sizeFrom>0.0) {
			c.add(Restrictions.ge("totalSize", new Double(sizeFrom*1073741824.0).longValue()));
		}
		if(sizeTo!=null && sizeTo>0.0) {
			c.add(Restrictions.le("totalSize", new Double(sizeTo*1073741824.0).longValue()));
		}
		c.add(Restrictions.isEmpty("torrentWikiData"));
		c.addOrder(Order.desc("seeds"));
		c.addOrder(Order.desc("id"));
		Collection<Torrent> torrents = new ArrayList<Torrent>();
		if(categories!=null && !categories.isEmpty()) {
			for(Type type:categories) {
				switch (type) {
				case SOFTWARE:
					c.add(Restrictions.eq ("software", true));
					c.add(Restrictions.eq ("video", false));
					c.add(Restrictions.eq ("audio", false));
					break;
				case VIDEO:
					c.add(Restrictions.eq ("video", true));
					c.add(Restrictions.eq ("software", false));
					c.add(Restrictions.eq ("archive", false));
					c.add(Restrictions.eq ("diskImage", false));
					break;
				case AUDIO:
					c.add(Restrictions.eq ("audio", true));
					c.add(Restrictions.eq ("software", false));
					c.add(Restrictions.eq ("archive", false));
					c.add(Restrictions.eq ("diskImage", false));
					break;
				case IMAGES:
					c.add(Restrictions.eq ("image", true));
					c.add(Restrictions.eq ("software", false));
					c.add(Restrictions.eq ("video", false));
					c.add(Restrictions.eq ("audio", false));
					c.add(Restrictions.eq ("archive", false));
					c.add(Restrictions.eq ("diskImage", false));
					break;
				case DOCUMENTS:
					c.add(Restrictions.eq ("document", true));
					c.add(Restrictions.eq ("software", false));
					c.add(Restrictions.eq ("video", false));
					c.add(Restrictions.eq ("audio", false));
					c.add(Restrictions.eq ("archive", false));
					c.add(Restrictions.eq ("diskImage", false));
					break;
				case ARCHIVES:
					c.add(Restrictions.eq ("archive", true));
					c.add(Restrictions.eq ("software", false));
					c.add(Restrictions.eq ("video", false));
					c.add(Restrictions.eq ("audio", false));
					break;
				case DISK_IMAGES:
					c.add(Restrictions.eq ("diskImage", true));
					c.add(Restrictions.eq ("software", false));
					c.add(Restrictions.eq ("video", false));
					c.add(Restrictions.eq ("audio", false));
					break;				
				case ALL:
					c.add(Restrictions.eq ("archive", false));
					break;	
				default:
					break;
				}
				Collection<Torrent> t = findByCriteriaWith(c);
				torrents.addAll(t);
			}

		} else {
			Collection<Torrent> t = findByCriteriaWith(c);
			torrents.addAll(t);
		}

		return torrents;
	}
	
	
	/**
	 * Find documents
	 */
	public Collection<Torrent> findAllDocuments(int offset, int limit) {
		Criteria c = createCriteria(offset, limit);
		c.add(Restrictions.eq ("document", true));
		Collection<Torrent> torrents = findByCriteriaWith(c);
		return torrents;
	}
	
	/**
	 * Find Torrent by an info hash
	 */
	public Collection<Torrent> findByInfoHash(String infoHash) {
		Criteria c = createCriteria();
		c.add(Restrictions.eq ("infoHash", infoHash));
		Collection<Torrent> torrents = findByCriteriaWith(c);
		return torrents;
	}
}
