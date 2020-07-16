package controller;

import org.hyperborian.bt.listener.NewTorrentDHTListener;
import org.hyperborian.bt.pojo.classifier.TorrentClassifier;
import org.zkoss.json.JSONObject;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import metadata.ngram.NGramWords;
import model.TorrentDAO;
import model.hibernate.TorrentHibernateDAO;
import model.pojo.Torrent;
import the8472.mldht.cli.TorrentInfo;

public class UploadFileController extends AbstractController{
	
	private TorrentDAO torrentDAO;

	public UploadFileController() {
		torrentDAO = new TorrentHibernateDAO();
	}
	
	private static final long serialVersionUID = -2546318528735167971L;

	@Listen("onSendFile = #upload")
    public void selectFiles(Event event) {
		JSONObject jsonObject = (JSONObject) event.getData();
		String filename = jsonObject.get("name").toString();
    	this.fileName.setValue(filename);
    	this.submitContainer.setVisible(true);
    	this.dropArea.setVisible(false);
    	this.seedingWarning.setVisible(false);
    }
	
	@Listen("onUploadedFile = #upload")
    public void uploadedFiles(Event event) {
		this.submitContainer.setVisible(false);
		this.resultContainer.setVisible(true);
		JSONObject jsonObject = (JSONObject) event.getData();
		String location = jsonObject.get("location").toString();
		torrentLink.setHref(location);
    	//Messagebox.show(this.fileName.getValue()+" uploaded:"+location);
		String infoHash = jsonObject.get("infoHash").toString();
		Long size = Long.valueOf(jsonObject.get("size").toString());
		Torrent torrent = new Torrent();
		torrent.setInfoHash(infoHash);
		torrent.setLocalPath(location);
		torrent.setName(this.fileName.getValue());
		torrent.setSeeds(1);
		String ext = TorrentInfo.getExtension(torrent.getName());
		if(ext!=null && ext!="") {
			TorrentClassifier.setType(torrent, ext.toLowerCase());
		}
		torrent.setTotalSize(size);
		torrent.setNgram(NGramWords.get(torrent));
		newTorrentDHTListener.newTorrentReceived(torrent);
    }
	
	@Wire
	private Div dropArea;
	
	@Wire
	private Label fileName;
	
	@Wire
	private Div submitContainer;

	@Wire
	private Div resultContainer;
	
	@Wire
	private Div seedingWarning;
	
	@Wire
	private A torrentLink;
	
	NewTorrentDHTListener newTorrentDHTListener = new NewTorrentDHTListener() {

		@Override
		public void newTorrentReceived(org.hyperborian.bt.pojo.Torrent torrent) {
			System.err.println("info: "+ torrent.toString());
			System.out.println("local path:"+torrent.getInfoHash());
			torrentDAO.save(new Torrent(torrent));
			System.out.println("end info:"+torrent.getInfoHash());
		}
	};
	
}
