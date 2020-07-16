package controller;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hyperborian.bt.Start;
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
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import comparator.TorrentFileComparator;
import converter.HumanReadableByteCount;
import model.pojo.Torrent;
import model.pojo.TorrentFileParent;
import model.pojo.TorrentFileParentStatus;
import model.pojo.TorrentFileSearch;
import the8472.mldht.cli.TorrentInfo;

public class TorrentDetailsController extends AbstractTorrentFileSearchListController {
	
	private static final long serialVersionUID = 2193437275779994276L;
	
	private long torrentId;

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		Execution exec = Executions.getCurrent();
		String id = exec.getParameter("id");
		if(id==null) {
			Messagebox.show("Id parameter is missing", "Error", Messagebox.OK, Messagebox.ERROR);
			return;
		} else {
			try {
				torrentId=Long.parseLong(id);
			} catch (NumberFormatException e) {
				Messagebox.show("Incorrect torrent id format", "Error", Messagebox.OK, Messagebox.ERROR);
				return;
			}
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
	private Html wikiContent;
	
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
	
	public void showLatestTorrents() {
		TorrentClassifier.Type type = TorrentClassifier.getType((Torrent) getObject());
		if(type!=null) {
				switch (type) {
					case SOFTWARE:
						latestTorrents.setModel(new ListModelList<>(Start.fifoTorrentsQueueSoftware));
						break;
					case VIDEO:
						latestTorrents.setModel(new ListModelList<>(Start.fifoTorrentsQueueVideo));
						
						break;
					case AUDIO:
						latestTorrents.setModel(new ListModelList<>(Start.fifoTorrentsQueueAudio));
						
						break;
					case IMAGES:
						latestTorrents.setModel(new ListModelList<>(Start.fifoTorrentsQueueImages));
						
						break;
					case ARCHIVES:
						latestTorrents.setModel(new ListModelList<>(Start.fifoTorrentsQueueArchives));
						
						break;
					case DOCUMENTS:
						latestTorrents.setModel(new ListModelList<>(Start.fifoTorrentsQueueDocuments));
						
						break;
					case DISK_IMAGES:
						latestTorrents.setModel(new ListModelList<>(Start.fifoTorrentsQueueDiskImages));
						
						break;
					default:
						latestTorrents.setModel(new ListModelList<>(Start.fifoTorrentsQueue));
						break;
				}
			} else {
				latestTorrents.setModel(new ListModelList<>(Start.fifoTorrentsQueue));
			}
	}
		

	public void search() throws MalformedURLException, IOException {
		Torrent torrent = getTorrentDAO().getById(torrentId);
		if(torrent==null) {
			Messagebox.show("The torrent your are looking for is missing.", "Error", Messagebox.OK, Messagebox.ERROR);
			return;
		}
		setObject(torrent);
		showLatestTorrents();
		showWikiData();
		
		org.hyperborian.bt.pojo.Torrent ti = TorrentInfo.get(getTorrentFileInputStream(torrent));
		ti.getFiles().sort(new TorrentFileComparator());
		if(ti.getFiles().size()<=21) {
			files.setModel(new ListModelList<TorrentFileParent>(ti.getFiles().stream().map(e->new TorrentFileParent(ti, e)).collect(Collectors.toList())));
			torrentFilesCashe = new ArrayList<TorrentFileParent>();
		} else {
			files.setModel(new ListModelList<TorrentFileParent>(ti.getFiles().subList(0, 20).stream().map(e->new TorrentFileParent(ti, e)).collect(Collectors.toList())));
			torrentFilesCashe = ti.getFiles().subList(20, ti.getFiles().size()).stream().map(e->new TorrentFileParent(ti, e)).collect(Collectors.toList());
		}
		
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
        if(similarTorrents.size()>0) {
        	similarTorrentsList.setVisible(true);
        }
	}
	
	private void showWikiData() throws MalformedURLException, IOException {
		Torrent torrent = (Torrent) getObject();
		if(torrent.getTorrentWikiData().size()>0) {
			List<TorrentFileSearch> tsList = getTorrentFileSearchList(torrent, null);
			if(tsList.size()>0) {
				TorrentFileSearch ts = tsList.get(0);
				String title = ts.getWikiTitle();
				panelTitle.setTitle(title);
				String wikiContent = ts.getHtmlWikiTorrentFileList();
				this.wikiContent.setContent(wikiContent);
			}
		} else {
			panelTitle.setTitle(torrent.getName());
		}
	}


	@Override
	public void reload(Object obj) {
		ListModelList<TorrentFileParent> listModel = (ListModelList)files.getModel();
		Set<TorrentFileParent> selectedItems = listModel.getSelection();
		listModel.stream().filter(track->track.getStatus()==TorrentFileParentStatus.PLAYING).forEach(track->{track.setStatus(TorrentFileParentStatus.LISTING);listModel.notifyChange(track);});
		TorrentFileParent playing = (TorrentFileParent) obj;
		playing.setStatus(TorrentFileParentStatus.PLAYING);
		listModel.setSelection(Arrays.asList(playing));
		listModel.notifyChange(playing);
		Clients.evalJavaScript("jq('.audio').on('ended',function(){\r\n" + 
				"      var widget = zk.Widget.$('$audio');\r\n" + 
				"	   zAu.send(new zk.Event(widget, \"onEnded\", {prop1 : \"Test\" }, {toServer:true}));\r\n" + 
				"    })");
	}
	
	public void loading(Object obj) {
		ListModelList<TorrentFileParent> listModel = (ListModelList)files.getModel();
		TorrentFileParent playing = (TorrentFileParent) obj;
		playing.setStatus(TorrentFileParentStatus.LOADING);
		listModel.notifyChange(playing);
	}
	
	public void progressStatus(Object obj) {
		TorrentFileParent playing = (TorrentFileParent) obj;
		ListModelList<TorrentFileParent> listModel = (ListModelList)files.getModel();
		listModel.notifyChange(playing);
	}
	
	public TorrentFileParent preloadingNext(Object obj) {
		TorrentFileParent playing = (TorrentFileParent) obj;
		ListModelList<TorrentFileParent> listModel = (ListModelList)files.getModel();
		int index = listModel.indexOf(playing);
		if(index<listModel.size()-1) {
			TorrentFileParent next = listModel.get(index+1);
			loading(next);
			return next;
		}
		return null;
	}
	
	public void playThisTrack(Object obj) {
		this.openWindow("./play_audio.zul", obj, "overlapped", true);
	}
	
	@Override
	public void reset(Object obj) {
		ListModelList<TorrentFileParent> listModel = (ListModelList)files.getModel();
		listModel.clearSelection();
		TorrentFileParent playing = (TorrentFileParent) obj;
		playing.setStatus(TorrentFileParentStatus.LISTING);
		listModel.notifyChange(playing);
	}
	
	public void attachWikiData() {
		Torrent torrent = (Torrent) getObject();
		openWindow("./attach_wiki_data.zul", torrent);
	}

}