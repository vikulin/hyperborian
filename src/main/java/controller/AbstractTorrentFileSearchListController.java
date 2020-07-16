package controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hyperborian.bt.pojo.TorrentFile;

import model.pojo.Torrent;
import model.pojo.TorrentFileSearch;
import model.pojo.TorrentWikiData;
import the8472.mldht.cli.TorrentInfo;

public abstract class AbstractTorrentFileSearchListController extends AbstractController {
	
	
    protected List<TorrentFileSearch> getTorrentFileSearchList(Torrent torrent, String name) throws MalformedURLException, IOException {
    	String url = "/torrent_details.zhtml?id="+torrent.getId()+"&name="+URLEncoder.encode(torrent.getName(), "UTF-8");
		TorrentFileSearch ts = new TorrentFileSearch(url);
		if(torrent.getTorrentWikiData().size()>0) {
			String language = getLanguage(getBrowserLocale());
			boolean foundLanguage = false;
			TorrentWikiData enWikiData = null;
			if(language!=null) {
				for(TorrentWikiData torrentWikiData:torrent.getTorrentWikiData()) {
					if(torrentWikiData.getLanguageCode().equals("en")) {
						enWikiData = torrentWikiData;
					}
					if(torrentWikiData.getLanguageCode().equals(language.toLowerCase())){
						/**
						 * https://en.wikipedia.org/w/api.php?action=parse&page=T-34_(film)&prop=wikitext&section=0&format=json
						 */
						ts.setBrowserLanguage(language);
						ts.setWikiTitle(torrentWikiData.getWikiTitle());
						torrent.setShowTorrentWikiData(torrentWikiData);
						foundLanguage = true;
						break;
					}
				}
			}
			if(!foundLanguage && enWikiData!=null) {
				ts.setBrowserLanguage("en");
				ts.setWikiTitle(enWikiData.getWikiTitle());
				torrent.setShowTorrentWikiData(enWikiData);
			}
		}
		if(name!=null) {
			org.hyperborian.bt.pojo.Torrent ti = TorrentInfo.get(getTorrentFileInputStream(torrent));
			Iterator<TorrentFile> items = ti.getFiles().iterator();
			while(items.hasNext()) {
				TorrentFile tf = items.next();
				String path = tf.getPath().toLowerCase();
				if(path.contains(name.toLowerCase())) {
					int nameIndex = path.indexOf(name.toLowerCase());
					String left = tf.getPath().substring(0, nameIndex);
					String right = tf.getPath().substring(nameIndex+name.length(), path.length());
					ts.addTorrentFileListText(left);
					ts.addEmTorrentFileListText(name);
					ts.addTorrentFileListText(right);
					if(ti.getFiles().size()>1) {
						ts.addTorrentFileListText(",");
					}
					if(items.hasNext()) {
						tf = items.next();
						ts.addTorrentFileListText("&nbsp;");
						ts.addTorrentFileListText(tf.getPath());
						ts.addTorrentFileListText("&nbsp;");
					}
				};
				if(ts.size()>10) {
					break;
				}
			}
		}
		List<TorrentFileSearch> tsList = new ArrayList<TorrentFileSearch>();
		tsList.add(ts);
		
		return tsList;
    }

}
