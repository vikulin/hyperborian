package controller;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hyperborian.bt.Start;
import org.hyperborian.bt.pojo.TorrentFile;
import org.hyperborian.bt.pojo.classifier.TorrentClassifier;
import org.springframework.security.acls.model.NotFoundException;
import org.zkoss.json.JSONObject;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkmax.zul.Filedownload;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

import comparator.TorrentFileComparator;
import converter.HumanReadableByteCount;
import model.pojo.Torrent;
import model.pojo.TorrentFileParent;
import model.pojo.TorrentFileParentStatus;
import model.pojo.TorrentFileSearch;
import model.pojo.TorrentWikiData;
import the8472.mldht.cli.TorrentInfo;

public class ModifyTorrentDetailsController extends AbstractController {
	
	private static final long serialVersionUID = 2193437275779994276L;
	
	private long torrentId;

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		Execution exec = Executions.getCurrent();
		String id = exec.getParameter("id");
		if(id==null) {
			throw new NotFoundException("Id parameter is missing");
		} else {
			torrentId=Long.parseLong(id);
			search();
		}	
	}
		
	
	@Listen("onScroll = #myScrollDiv")
	public void  scrollDiv(Event e) {
		JSONObject jsonObject = (JSONObject) e.getData();
		double scrollPercentageY = Double.parseDouble(String.valueOf(jsonObject.get("scrollPercentageY")));
		if(scrollPercentageY>90) {
			int counter=0;
			while(torrentFilesCashe.size()>0 && counter<20) {
				((ListModelList)files.getModel()).add(torrentFilesCashe.remove(0));
				counter++;
			}
		}
	}
	
	@Wire
    private Listbox files;
	
	@Wire
    private Listbox latestTorrents;
	
	@Wire
	private Listbox similarTorrentsList;
	
	@Wire
	private Groupbox panelTitle;
	
	@Wire
	private Label createDate;
	
	@Wire
	private Label totalSize;
	
	@Wire
	private Label downloads;
	
	@Wire
	private Label seeds;
	
	@Listen("onClick = #magnetLink")
	public void onMagnetLinkClick() throws UnsupportedEncodingException {
		if(getObject()!=null) {
			Torrent torrent = (Torrent)getObject();
			Executions.getCurrent().sendRedirect(getMagnetLink(torrent), "hiddenIframe");
			String remoteIp = Executions.getCurrent().getRemoteAddr();
			if(downloadsByIp.get(remoteIp)==null) {
				try {
					updateDownloads(torrent);
				} finally {
					downloadsByIp.put(remoteIp+"|"+torrent.getId(), torrent.getId());
				}
			}
		} else {
			throw new NotFoundException("Torrent object is missing");
		}
	}
	
	@Listen("onClick = #torrentFile")
	public void onTorrentDownloadClick() throws MalformedURLException, IOException {
		if(getObject()!=null) {
			Torrent torrent = (Torrent)getObject();
			//Executions.getCurrent().sendRedirect(getMagnetLink(torrent), "hiddenIframe");
			String remoteIp = Executions.getCurrent().getRemoteAddr();
			final InputStream content = 
			        new DataInputStream(getTorrentFileInputStream(torrent));
			Filedownload.save(content, "application/x-bittorrent", torrent.getName()+".torrent");
			if(AbstractController.downloadsByIp.get(remoteIp)==null) {
				try {
					updateDownloads(torrent);
				} finally {
					downloadsByIp.put(remoteIp, torrent.getId());
				}
			}
		} else {
			throw new NotFoundException("Torrent object is missing");
		}
	}

	private List<TorrentFileParent> torrentFilesCashe;
	
	public void search() throws MalformedURLException, IOException {
		Torrent torrent = getTorrentDAO().loadById(torrentId);
		setObject(torrent);
		if(torrent==null) {
			throw new NotFoundException("The torrent your are looking for is missing.");
		}
		
		org.hyperborian.bt.pojo.Torrent ti = TorrentInfo.get(getTorrentFileInputStream(torrent));
		ti.getFiles().sort(new TorrentFileComparator());
		if(ti.getFiles().size()<=21) {
			files.setModel(new ListModelList<TorrentFileParent>(ti.getFiles().stream().map(e->new TorrentFileParent(ti, e)).collect(Collectors.toList())));
			torrentFilesCashe = new ArrayList<TorrentFileParent>();
		} else {
			files.setModel(new ListModelList<TorrentFileParent>(ti.getFiles().subList(0, 20).stream().map(e->new TorrentFileParent(ti, e)).collect(Collectors.toList())));
			torrentFilesCashe = ti.getFiles().subList(20, ti.getFiles().size()).stream().map(e->new TorrentFileParent(ti, e)).collect(Collectors.toList());
		}
		panelTitle.setTitle(torrent.getName());
		if(ti.getCreationDate()==null) {
			String torrentFile = torrent.getTorrentFileLocalPath();
			if(!torrentFile.startsWith("http")) {
				File torrentLocalFile = new File(getBasePath()+"/"+torrentFile);
				BasicFileAttributes attr;
				try {
					attr = Files.readAttributes(torrentLocalFile.toPath(), BasicFileAttributes.class);
					createDate.setValue(ZonedDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault()).format(getDateTimeFormatter()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			createDate.setValue(ZonedDateTime.ofInstant(Instant.ofEpochMilli(ti.getCreationDate()), ZoneId.systemDefault()).format(getDateTimeFormatter()));
		}
		totalSize.setValue(HumanReadableByteCount.humanReadableByteCount(torrent.getTotalSize()));
		downloads.setValue(torrent.getDownloads()+"");
		seeds.setValue(torrent.getSeeds()+"");
		Collection<Torrent> result = getTorrentDAO().findSimilarTorrent(torrent);
		List<Torrent> similarTorrents = result.stream().collect(Collectors.toList());
		List<List<TorrentFileSearch>> torrentFiles = new ArrayList<>();
		List<Torrent> cleanUp = new ArrayList<Torrent>();
		similarTorrents.remove(torrent);
        for(Torrent t:similarTorrents) {
        	try {
	        	List<TorrentFileSearch> tsList = getTorrentFileSearchList(t, torrent.getName());
	        	torrentFiles.add(tsList);
        	} catch (FileNotFoundException ex) {
        		ex.printStackTrace();
        		cleanUp.add(torrent);
        	}
        }
        similarTorrents.removeAll(cleanUp);
        similarTorrentsList.setModel(new ListModelList<Torrent>(similarTorrents));
	}
	
    private List<TorrentFileSearch> getTorrentFileSearchList(Torrent torrent, String name) throws MalformedURLException, IOException {
    	org.hyperborian.bt.pojo.Torrent ti = TorrentInfo.get(getTorrentFileInputStream(torrent));
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
						foundLanguage = true;
						break;
					}
				}
			}
			if(!foundLanguage && enWikiData!=null) {
				ts.setBrowserLanguage("en");
				ts.setWikiTitle(enWikiData.getWikiTitle());
			}
		}
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
		List<TorrentFileSearch> tsList = new ArrayList<TorrentFileSearch>();
		tsList.add(ts);
		return tsList;
    }
	
	public void attachWikiData() {
		Torrent torrent = (Torrent) getObject();
		openWindow("./attach_wiki_data.zul", torrent);
	}

}