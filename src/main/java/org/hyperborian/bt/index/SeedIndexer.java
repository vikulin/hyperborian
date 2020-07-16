package org.hyperborian.bt.index;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Collection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hyperborian.bt.pojo.InfoHashPeerStat;
import model.pojo.Torrent;
import org.hyperborian.bt.pojo.TorrentInfoHashIndex;

import com.google.gson.Gson;

import controller.AbstractController;
import model.TorrentDAO;
import model.TorrentInfoHashIndexDAO;
import model.hibernate.HibernateUtil;
import model.hibernate.TorrentHibernateDAO;
import model.hibernate.TorrentInfoHashIndexHibernateDAO;
import model.pojo.interfaces.Pojo;

public class SeedIndexer implements Runnable, Pojo, Indexer{
	
		private Integer limit;
	
		private boolean startFromMostSeeding = true;
	
		private boolean shouldDeleteZeroSeeding;
		
		private boolean stop = false;
	
		private Timestamp createTime;
	
		private TorrentInfoHashIndexDAO torrentInfoHashIndexDAO;
		
		private TorrentDAO torrentDAO;
		
		private String name;
		
		private int progress = 0;

		private double maxSize;
		
		private int processed = 0;

		private int part;

		private Gson gson;

		private Long id;
		
		private int zeroSeedingThreshold = 0;
		
		public Timestamp getCreateTime() {
			return createTime;
		}
		
		public Integer getLimit() {
			return limit;
		}

		public void setLimit(Integer limit) {
			this.limit = limit;
		}

		public boolean getStartFromMostSeeding() {
			return startFromMostSeeding;
		}

		public void setStartFromMostSeeding(boolean startFromMostSeeding) {
			this.startFromMostSeeding = startFromMostSeeding;
		}

		public boolean getDeleteNotSeeding() {
			return shouldDeleteZeroSeeding;
		}

		public void setDeleteNotSeeding(boolean shouldDeleteZeroSeeding) {
			this.shouldDeleteZeroSeeding = shouldDeleteZeroSeeding;
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
	
		public SeedIndexer() {
			this.id = getSecureIndex();
			this.gson = new Gson();
			torrentDAO = new TorrentHibernateDAO();
			torrentInfoHashIndexDAO = new TorrentInfoHashIndexHibernateDAO();
		}

		@Override
		public void run() {
			//reset counter
			processed = 0;

			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
			Transaction tx1 = session.beginTransaction();
			maxSize = (Long) session.createQuery("select count(*) from Torrent").uniqueResult();
			int partSize = 100;
			if(limit!=null && limit<maxSize) {
				maxSize = limit;
				if(limit<partSize) {
					partSize = limit;
				}
			}
			tx1.commit();
			int parts = (int)(maxSize/(partSize*1.0));
			for(int part=0;part<=parts;part++) {
				this.part=part;
				Transaction tx2 = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
				try {
					Collection<TorrentInfoHashIndex> torrents;
					if(startFromMostSeeding) {
						torrents = torrentInfoHashIndexDAO.findAllMostSeeding(part*partSize, partSize);
					} else {
						torrents = torrentInfoHashIndexDAO.findAllLeastSeeding(part*partSize, partSize);
					}
					for(TorrentInfoHashIndex ti: torrents) {
						InfoHashPeerStat stat = getInfoHashStat(ti.getInfoHash());
						torrentStatisticReceived(ti, stat);
						if(stop) {
							tx2.commit();
							return;
						}
						if(limit!=null) {
							if(limit<=processed) {
								tx2.commit();
								return;
							}
						}
					}
					tx2.commit();
					
				} catch (Exception ex) {
					tx2.rollback();
					ex.printStackTrace();
				}
			}
		}
	
		private void updateSeedsForIh(Session session, Long id, String ih, Long seeds){
			 if(seeds>zeroSeedingThreshold) {
				Torrent torrent = torrentDAO.loadById(id);
				torrent.setSeeds(seeds.intValue());
				torrentDAO.update(torrent);
				System.err.println(String.format("Indexed %s: peers[scrape]:%d",
						ih, seeds));
			 } else {
				 if(shouldDeleteZeroSeeding) {
				 	Torrent torrent = torrentDAO.loadById(id);
					torrent.setSeeds(seeds.intValue());
					torrentDAO.delete(torrent);
					String torrentFile = AbstractController.getTorrentUrl(torrent);
					if(!torrentFile.startsWith("http")) {
						File file = new File(torrentFile);
						if(file.exists()) {
							file.delete();
						} else {
							System.err.println("File "+file.getAbsolutePath()+" is missing");
						}
						System.err.println(String.format("Deleted %s: peers[scrape]:%d",
								ih, seeds));
					} else {
						// TODO implement
					}
				 }
			 }
		}
		
		private void torrentStatisticReceived(TorrentInfoHashIndex ih, InfoHashPeerStat handler) {
				String infoHash = ih.getInfoHash();
				updateSeedsForIh(HibernateUtil.getSessionFactory().getCurrentSession(), ih.getId(), infoHash, handler.getPeers());
				processed++;
				progress = (int)(100.0*(processed/maxSize));
		}
		
		private InfoHashPeerStat getInfoHashStat(String infoHash)
				throws ClientProtocolException, IOException, URISyntaxException {

			HttpClient httpClient = HttpClientBuilder.create().build();
			String url = AbstractController.getBtIndexUrl() + "/peers/" + infoHash;
			HttpGet request = new HttpGet(url);
			request.addHeader("content-type", "application/json");
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String responseStatus = EntityUtils.toString(entity, "UTF-8");
			InfoHashPeerStat stat = gson.fromJson(responseStatus, InfoHashPeerStat.class);

			return stat;
		}

		@Override
		public Long getId() {
			return id;
		}

		@Override
		public void setId(Long id) {
			this.id=id;
		}

		public int getZeroSeedingThreshold() {
			return zeroSeedingThreshold;
		}

		public void setZeroSeedingThreshold(int zeroSeedingThreshold) {
			this.zeroSeedingThreshold = zeroSeedingThreshold;
		}
		
	}