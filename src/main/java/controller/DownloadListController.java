package controller;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.hyperborian.bt.pojo.Torrent;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;

import model.TorrentDAO;
import model.hibernate.TorrentHibernateDAO;
import model.pojo.TorrentsByIP;
import the8472.mldht.cli.TorrentInfo;

public class DownloadListController extends PojoListboxController {
	
	private TorrentDAO torrentDAO;

	public DownloadListController() {
		torrentDAO = new TorrentHibernateDAO();
	}

	private static final long serialVersionUID = 1372479664187844836L;
	
	@Wire
	private Listbox downloads;
	
	@Wire
	private Textbox ip;

	@Override
	protected Listbox getListbox() {
		return downloads;
	}


	
    @Listen("onClick = #searchButton")
    public void search(){
        String ip = this.ip.getValue();
        Collection<TorrentsByIP> result = new LinkedHashSet<TorrentsByIP>();
        if (ip==null || "".equals(ip)){
        	for(Entry<String, Long> downloads: downloadsByIp.entrySet()) {
        		TorrentsByIP download = new TorrentsByIP();
        		download.setIp(downloads.getKey());
        		download.setId(downloads.getValue()+"");
        		//String torrentFile = torrentDAO.loadById(downloads.getValue()).getTorrentFileLocalPath();
				//Torrent ti = TorrentInfo.get(new File(getBasePath()+"/"+torrentFile).toPath());
        		//download.setName(ti.getName());
        		Torrent torrentFile = torrentDAO.loadById(downloads.getValue());
        		download.setName(torrentFile.getName());
        		result.add(download);
        	}
		} else {
			for(Entry<String, Long> downloads: downloadsByIp.entrySet()) {
        		
	        	if(downloads.getKey().equals(ip)) {
	        		TorrentsByIP download = new TorrentsByIP();
	        		download.setIp(downloads.getKey());
	        		download.setId(downloads.getValue()+"");
	        		//String torrentFile = torrentDAO.loadById(downloads.getValue()).getTorrentFileLocalPath();
					//Torrent ti = TorrentInfo.get(new File(getBasePath()+"/"+torrentFile).toPath());
	        		//download.setName(ti.getName());
	        		Torrent torrentFile = torrentDAO.loadById(downloads.getValue());
	        		download.setName(torrentFile.getName());
	        		result.add(download);
	        		break;
        		}
        	}
		}
        downloads.setModel(new ListModelList<TorrentsByIP>(result));
	}
}
