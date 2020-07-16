package org.hyperborian.bt.index;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Collection;

import org.hibernate.Session;
import org.hibernate.Transaction;
import model.pojo.Torrent;

import model.TorrentDAO;
import model.hibernate.HibernateUtil;
import model.hibernate.TorrentHibernateDAO;
import model.pojo.interfaces.Pojo;

public class TopDownloadIndexer implements Runnable, Pojo, Indexer{
	
		private Integer limit;
	
		private boolean stop = false;
	
		private Timestamp createTime;
	
		private TorrentDAO torrentDAO;
		
		private String name;
		
		private int progress = 0;

		private double maxSize;
		
		private int processed = 0;

		private int part;

		private Long id;
		
		public Timestamp getCreateTime() {
			return createTime;
		}
		
		public Integer getLimit() {
			return limit;
		}

		public void setLimit(Integer limit) {
			this.limit = limit;
		}

		public boolean isStopped() {
			return stop;
		}

		public void stop() {
			this.stop = true;
		}

		public void setCreateTime(Timestamp createTime) {
			this.createTime = createTime;
		}

		public int getPart() {
			return part;
		}
		
		public int getProcessed() {
			return processed;
		}
		
		public int getProgress() {
			return progress;
		}

		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}

		private static long getSecureIndex() {
		    SecureRandom random = new SecureRandom();
		    while (true) {
		        long nextLong = random.nextLong();
		        if (nextLong > 0) {
		            return nextLong;
		        }
		    }
		}
	
		public TopDownloadIndexer() {
			this.id = getSecureIndex();
			torrentDAO = new TorrentHibernateDAO();
		}

		@Override
		public void run() {
			//reset counter
			processed = 0;

			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
			Transaction tx = session.beginTransaction();
			Collection<Torrent> topDownloads = torrentDAO.findTopDownloads(0, limit);
			maxSize = topDownloads.size();
			for(Torrent torrent:topDownloads) {
				updateTopDownloads(torrent);
				processed++;
				progress = (int)(100.0*(processed/maxSize));
			}
			tx.commit();
		}
	
		private void updateTopDownloads(Torrent torrent){
			Long downloads = torrent.getDownloads();
			if(downloads>0) {
				downloads--;
				torrent.setDownloads(downloads);
				torrentDAO.update(torrent);
			}
		}
		
		@Override
		public Long getId() {
			return id;
		}

		@Override
		public void setId(Long id) {
			this.id=id;
		}
		
	}