package org.hyperborian.bt;

import java.io.InputStream;
import java.net.SocketException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hyperborian.bt.listener.NewTorrentDHTListener;
import org.hyperborian.bt.listener.TorrentPeersStatisticListener;
import org.hyperborian.bt.map.MaxSizeHashMap;
import org.hyperborian.bt.pojo.classifier.TorrentClassifier;
import org.zkoss.zul.ListModelList;

import lbms.plugins.mldht.kad.DHT;
import lbms.plugins.mldht.kad.DHT.DHTtype;
import lbms.plugins.mldht.kad.DHT.IncomingMessageListener;
import lbms.plugins.mldht.kad.DHTStats;
import lbms.plugins.mldht.kad.DHTStatsListener;
import lbms.plugins.mldht.kad.Key;
import lbms.plugins.mldht.kad.RPCServer;
import lbms.plugins.mldht.kad.ScrapeResponseHandler;
import lbms.plugins.mldht.kad.messages.MessageBase;
import model.TorrentDAO;
import model.hibernate.HibernateUtil;
import model.hibernate.TorrentHibernateDAO;
import model.pojo.Torrent;
import the8472.mldht.Launcher;
import the8472.mldht.indexing.TorrentDumper;
import the8472.utils.ConfigReader;
import vmodel.MaxSizeListModelList;

public class Start {
	
	public static ListModelList<Object> fifoTorrentsQueue = new MaxSizeListModelList(10);
	public static ListModelList<Object> fifoTorrentsQueueSoftware = new MaxSizeListModelList(10);
	public static ListModelList<Object> fifoTorrentsQueueVideo = new MaxSizeListModelList(10);
	public static ListModelList<Object> fifoTorrentsQueueAudio = new MaxSizeListModelList(10);
	public static ListModelList<Object> fifoTorrentsQueueImages = new MaxSizeListModelList(10);
	public static ListModelList<Object> fifoTorrentsQueueArchives = new MaxSizeListModelList(10);
	public static ListModelList<Object> fifoTorrentsQueueDocuments = new MaxSizeListModelList(10);
	public static ListModelList<Object> fifoTorrentsQueueDiskImages = new MaxSizeListModelList(10);
	
	static ConfigReader configReader;
	
	static TorrentDAO torrentDAO = new TorrentHibernateDAO();
	
	public static Map<String, Torrent> INFO_HASHES = new MaxSizeHashMap<String, Torrent>(20);
	
	public static void main(String[] args) throws SocketException, InterruptedException, ExecutionException {
		
		Supplier<InputStream> configSchema = () -> Launcher.class.getResourceAsStream("config.xsd");
		
		Supplier<InputStream> configDefaults = () -> Launcher.class.getResourceAsStream("config-defaults.xml");

		configReader = new ConfigReader(Paths.get(".", "config.xml"), configDefaults, configSchema);
		configReader.read();
		
		XmlConfig config = new XmlConfig(configReader);
		config.update();
		configReader.addChangeCallback(config::update);
		
		DHT dht = new DHT(DHTtype.IPV4_DHT);
		dht.addDHTNode("67.215.246.10", 6881);
		dht.start(config);
		
		CompletableFuture<RPCServer> service = dht.getServerManager().awaitActiveServer();
		RPCServer rpc = service.get();
		
		DHTStatsListener listener = new DHTStatsListener(){

			@Override
			public void statsUpdated(DHTStats stats) {
				System.out.println(stats);
				System.out.println("******************************");
				System.out.println(dht.getStatus());
			}
		};
		//dht.addStatsListener(listener);
		IncomingMessageListener l =  new IncomingMessageListener() {

			@Override
			public void received(DHT dht, MessageBase msg) {
				//System.out.println(msg);
			}
			
		};
		//dht.addIncomingMessageListener(l);
		dht.bootstrap();
		List<DHT> dhts = new ArrayList<DHT>();
		dhts.add(dht);
		NewTorrentDHTListener newTorrentDHTListener = new NewTorrentDHTListener() {

			@Override
			public void newTorrentReceived(org.hyperborian.bt.pojo.Torrent torrent) {
				System.err.println("info: "+ torrent.toString());
				System.out.println("local path:"+torrent.getInfoHash());
				Transaction tx = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
				try {
					Torrent torrentToSave = new Torrent(torrent);
					torrentDAO.save(torrentToSave);
					tx.commit();
					INFO_HASHES.put(torrent.getInfoHash(), torrentToSave);
				} catch (Exception ex) {
					tx.rollback();
					ex.printStackTrace();
				}
				System.out.println("end info:"+torrent.getInfoHash());
			}
		};
		TorrentPeersStatisticListener torrentPeersStatisticListener = new TorrentPeersStatisticListener() {
			
			private org.hyperborian.bt.pojo.Torrent updateSeedsForIh(Session session, String ih, int seeds){
				Torrent torrent = INFO_HASHES.get(ih);
				 if(torrent!=null) {
					Transaction tx = session.beginTransaction();
					try {
						torrent.setSeeds(seeds);
						torrentDAO.update(torrent);
						tx.commit();
						return INFO_HASHES.remove(torrent.getInfoHash());
					} catch (Exception ex) {
						tx.rollback();
						ex.printStackTrace();
					}
				}
				return null;
			}

			@Override
			public void torrentStatisticReceived(Key ih, ScrapeResponseHandler handler) {
				if(handler.getScrapedSeeds()>0) {
					org.hyperborian.bt.pojo.Torrent torrent = updateSeedsForIh(HibernateUtil.getSessionFactory().getCurrentSession(), ih.toString(false), handler.getScrapedSeeds());
					System.err.println(String.format("Scrape Result for %s: seeds[scrape]:%d peers[scrape]:%d direct results:%d ; %d/%d of nodes returning values supported scrape",
							ih.toString(false), handler.getScrapedSeeds(), handler.getScrapedPeers(), handler.getDirectResultCount(), handler.numResponsesSupportingScrape(), handler.numResponses()));
					TorrentClassifier.Type type = TorrentClassifier.getType(torrent);
					if(type!=null) {
						switch (type) {
							case SOFTWARE:
								fifoTorrentsQueueSoftware.add(torrent);
								break;
							case VIDEO:
								fifoTorrentsQueueVideo.add(torrent);
								break;
							case AUDIO:
								fifoTorrentsQueueAudio.add(torrent);
								break;
							case IMAGES:
								fifoTorrentsQueueImages.add(torrent);
								break;
							case ARCHIVES:
								fifoTorrentsQueueArchives.add(torrent);
								break;
							case DOCUMENTS:
								fifoTorrentsQueueDocuments.add(torrent);
								break;
							case DISK_IMAGES:
								fifoTorrentsQueueDiskImages.add(torrent);
								break;
							default:
								break;
						}
					}
					fifoTorrentsQueue.add(torrent);
				} else {
					INFO_HASHES.remove(ih.toString(false));
					System.out.println(String.format("Scrape Result for %s: seeds[scrape]:%d peers[scrape]:%d direct results:%d ; %d/%d of nodes returning values supported scrape",
							ih.toString(false), handler.getScrapedSeeds(), handler.getScrapedPeers(), handler.getDirectResultCount(), handler.numResponsesSupportingScrape(), handler.numResponses()));
				}
			}
			
		};
		
		TorrentDumper td = new TorrentDumper(newTorrentDHTListener, torrentPeersStatisticListener);
		td.start(dhts, configReader);

	}
	

}
