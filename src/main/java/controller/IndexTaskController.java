package controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.hyperborian.bt.index.DeleteDuplicatedTorrents;
import org.hyperborian.bt.index.GoogleCustomSearch;
import org.hyperborian.bt.index.Indexer;
import org.hyperborian.bt.index.SeedIndexer;
import org.hyperborian.bt.index.TopDownloadIndexer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;


public class IndexTaskController extends PojoListboxController{
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp, false);
	}
	
	public static List<Indexer> indexingTaskList = new ArrayList<Indexer>();

	private static final long serialVersionUID = 1372479664187844836L;
	
	@Wire
	private Listbox tasks;
	
	@Wire
	private Combobox name;

	protected Listbox getListbox() {
		return tasks;
	}
	
    /**
     * Modify part
     */
    @Listen("onClick = #createButton")
    public void create(){
    	if(name.getValue().equals("Index seeders")) {
	    	SeedIndexer indexer = new SeedIndexer();
	    	String page = "./seed_index.zhtml";
	    	openWindow(page, indexer);
	    	return;
    	}
    	if(name.getValue().equals("Index top downloads")) {
    		TopDownloadIndexer indexer = new TopDownloadIndexer();
	    	String page = "./top_downloads_index.zhtml";
	    	openWindow(page, indexer);
	    	return;
    	}
    	if(name.getValue().equals("Delete duplicated records")) {
    		DeleteDuplicatedTorrents indexer = new DeleteDuplicatedTorrents();
	    	String page = "./delete_duplicated_torrents.zhtml";
	    	openWindow(page, indexer);
	    	return;
    	}
    	if(name.getValue().equals("Google Custom Search Index")) {
    		GoogleCustomSearch indexer = new GoogleCustomSearch();
	    	String page = "./google_custom_search_index.zhtml";
	    	openWindow(page, indexer);
	    	return;
    	}
    	
    }

    @Listen("onClick = #searchButton")
    public void search(){
        String name = this.name.getValue();
        Collection<Indexer> result = new LinkedHashSet<Indexer>();
        if (name==null || "".equals(name)){
        	result.addAll(indexingTaskList);
		} else {
			for(Indexer task: indexingTaskList) {
	        	if(task.getName().equals(name)) {
	        		result.add(task);
        		}
        	}
		}
        tasks.setModel(new ListModelList<Indexer>(result));
    	/**'110661'id:71953**/
    	/*
    	for(long id=1;id<=175246;id++) {
    		
    		System.err.println("id:"+id);
			Torrent torrent = torrentDAO.getById(id);
			if(torrent==null) {
				continue;
			}
			String torrentFile = torrent.getTorrentFileLocalPath();
			Torrent ti = null;
			try {
				ti = TorrentInfo.get(new File(getBasePath()+"/"+torrentFile).toPath());
			} catch (java.lang.IllegalStateException ex) {
				torrent.setNgram(new File(getBasePath()+"/"+torrentFile).toPath().toString());
				continue;
			}
			torrent.setSoftware(ti.isSoftware());
			torrent.setVideo(ti.isVideo());
			torrent.setAudio(ti.isAudio());
			torrent.setDocument(ti.isDocument());
			torrent.setImage(ti.isImage());
			torrent.setArchive(ti.isArchive());
			torrent.setDiskImage(ti.isDiskImage());
			torrentDAO.update(torrent);
    	}*/
        /*
        Collection<Torrent> torrents = getTorrentDAO().findByName("??",0,100000000);
        System.err.println("found torrents:"+torrents.size());
    	for(Torrent torrent:torrents) {
    		
    		String torrentFile = torrent.getTorrentFileLocalPath();
    		try {
				Torrent ti = TorrentInfo.get(new File(getBasePath()+"/"+torrentFile).toPath());
				torrent.setName(ti.getName());
				getTorrentDAO().update(torrent);
    		} catch (java.io.UncheckedIOException ex) {
    			getTorrentDAO().delete(torrent);
    		}
    	}*/
    	
    	/*
    	Torrent torrent = torrentDAO.getById(100696l);
    	Torrent ti = TorrentInfo.get(new File("C:/Users/Vadym/Downloads/eclipse/dump-storage/torrents"+"/"+"06/66/0666a2e6353efbcafe0e6d2b870f41acb41345e1.torrent").toPath());
    	torrent.setSoftware(ti.isSoftware());
		torrent.setVideo(ti.isVideo());
		torrent.setAudio(ti.isAudio());
		torrent.setDocument(ti.isDocument());
		torrent.setImage(ti.isImage());
		torrent.setArchive(ti.isArchive());
		torrent.setDiskImage(ti.isDiskImage());
		torrentDAO.update(torrent);
		*/
	}
    
    @Override
    public void reload(Object obj) {
    	indexingTaskList.add((Indexer) obj);
    }
    
    @Override
    public void delete(Object obj) {
    	Indexer indexer = (Indexer)obj;
    	indexer.stop();
    	indexingTaskList.remove(obj);
    	super.delete(obj);
    }
}
