package controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.hyperborian.bt.index.GoogleCustomSearch;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import model.hibernate.TorrentWikiDataHibernateDAO;
import model.pojo.Torrent;
import model.pojo.TorrentWikiData;
import model.pojo.wikidata.Entities;
import model.pojo.wikidata.Labels;
import model.pojo.wikidata.P31;
import model.pojo.wikidata.Sitelinks;
import model.pojo.wikidata.WikiData;

public class AttachWikiDataController extends AbstractController{
	
	private TorrentWikiDataHibernateDAO torrentWikiDataDAO;

	public AttachWikiDataController() {
		torrentWikiDataDAO = new TorrentWikiDataHibernateDAO();
	}
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		Torrent torrent = (Torrent) getObject();
		Iterator<TorrentWikiData> iterator = torrent.getTorrentWikiData().iterator();
		if(iterator.hasNext()) {
			TorrentWikiData torrentWikiData = torrent.getTorrentWikiData().iterator().next();
			wikiDataIdText.setValue("Q"+torrentWikiData.getWikiDataId());
		}
	}
	
	@Wire
	private Textbox wikiDataIdText;
	
    @Listen("onClick = #saveButton")
    public void save() throws Exception {
    	Long wikiDataId = Long.valueOf(wikiDataIdText.getValue().trim().toUpperCase().substring(1));
    	Torrent torrent = (Torrent) getObject();
    	Collection<Torrent> similarTorrents = getTorrentDAO().findSimilarTorrent(wikiDataId, 0, 1);
    	if(similarTorrents.size()==0) {
			String wikiDataLink = "https://www.wikidata.org/wiki/Special:EntityData/Q"+wikiDataId;
			/**
			 * json suffix added within getWikiData method
			 */
			WikiData wikiData = GoogleCustomSearch.getWikiData(wikiDataLink);
			if(wikiData==null) {
				throw new WrongValueException(wikiDataIdText, "Wiki data: "+wikiDataId+" not found.\nWiki data link:"+wikiDataLink);
			}
			Entities entry = wikiData.getEntities().entrySet().iterator().next().getValue();
			List<P31> p31 = entry.getClaims().get("P31");
			if(p31==null || p31.size()==0) {
				throw new WrongValueException(wikiDataIdText, "Wiki attribute: P31 is missing");
			}
			Set<TorrentWikiData> torrentWikiDataSet = new HashSet<TorrentWikiData>();
			for(Entry<String, Labels> label:entry.getLabels().entrySet()) {
				String languageCode = label.getKey();
				Sitelinks sitelinks = entry.getSitelinks().get(languageCode+"wiki");
				if(sitelinks==null) {
					/**
					 * no such language code found in sitelinks object
					 */
					continue;
				}
				TorrentWikiData torrentWikiData = new TorrentWikiData();
				torrentWikiData.setTorrent(torrent);
				
				String wikiTitle = sitelinks.getTitle();
				torrentWikiData.setWikiDataId(wikiDataId);
				torrentWikiData.setLanguageCode(languageCode);
				torrentWikiData.setWikiTitle(wikiTitle);
		    	torrentWikiDataSet.add(torrentWikiData);

			}
			torrent.setTorrentWikiData(torrentWikiDataSet);
			for(TorrentWikiData torrentWikiData:torrentWikiDataSet) {
				torrentWikiDataDAO.save(torrentWikiData);
			}
    	} else {
    		torrent.getTorrentWikiData().forEach(f->{
    			torrentWikiDataDAO.delete(f);
    		});
    		
    		similarTorrents.iterator().next().getTorrentWikiData().
    		forEach(f -> {
	    		TorrentWikiData twd = new TorrentWikiData();
	    		twd.setLanguageCode(f.getLanguageCode());
	    		twd.setTorrent(torrent);
	    		twd.setWikiDataId(wikiDataId);
	    		twd.setWikiTitle(f.getWikiTitle());
	    		torrentWikiDataDAO.save(twd);
	    	});
    	}
    	Messagebox.show("Wiki данные для "+torrent.getName()+" успешно изменены");
    }

}
