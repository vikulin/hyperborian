package model.pojo;

import org.hyperborian.bt.pojo.Torrent;
import org.hyperborian.bt.pojo.TorrentFile;
import org.hyperborian.bt.pojo.classifier.TorrentClassifier;

public class TorrentFileParent extends TorrentFile {
	
	public TorrentFileParent(Torrent torrent, TorrentFile torrentFile) {
		this.torrent = torrent;
		setPath(torrentFile.getPath());
		setSize(torrentFile.getSize());
		setContentType(TorrentClassifier.getContentType(getExtension()));
	}
	
	private TorrentFileParentStatus status = TorrentFileParentStatus.LISTING;
	
	private String statusMessage;
	
	private Integer statusCode;
	
	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	private int progress=0;
	
	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getProgress() {
		return progress;
	}

	public TorrentFileParentStatus getStatus() {
		return status;
	}
	
	public String getStatusName() {
		return status.name().toString();
	}

	public void setStatus(TorrentFileParentStatus status) {
		this.status = status;
	}

	private Torrent torrent;

	public Torrent getTorrent() {
		return torrent;
	}
	
	@Override
	public String getExtension() {
		String path = getPath();
		if(path==null) {
			return null;
		}
		int i = path.lastIndexOf('.');
		if (i > 0) {
		    return new String(path.substring(i+1));
		} else {
			return "null";
		}
	}

}
