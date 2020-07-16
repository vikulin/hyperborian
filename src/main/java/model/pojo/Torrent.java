package model.pojo;

import java.io.Serializable;
import java.util.Collection;

public class Torrent extends org.hyperborian.bt.pojo.Torrent implements Serializable {
	
	private static final long serialVersionUID = -8040381235495435986L;
	
	public Torrent() {
		
	}
	
	public Torrent(org.hyperborian.bt.pojo.Torrent torrent) {
		this.setId(torrent.getId());
		this.setArchive(torrent.isArchive());
		this.setAudio(torrent.isAudio());
		this.setCreationDate(torrent.getCreationDate());
		this.setDiskImage(torrent.isDiskImage());
		this.setDocument(torrent.isDocument());
		this.setDownloads(torrent.getDownloads());
		this.setImage(torrent.isImage());
		this.setInfoHash(torrent.getInfoHash());
		this.setLocalPath(torrent.getLocalPath());
		this.setName(torrent.getName());
		this.setNgram(torrent.getNgram());
		this.setSeeds(torrent.getSeeds());
		this.setSoftware(torrent.isSoftware());
		this.setTorrentWikiData(null);
		this.setTotalSize(torrent.getTotalSize());
		this.setVideo(torrent.isVideo());
	}
	
	/**
	 * Collection of TorrentWikiData
	 */
	private Collection<TorrentWikiData> torrentWikiData = null;
	
	private TorrentWikiData showTorrentWikiData;

	public Collection<TorrentWikiData> getTorrentWikiData() {
		return torrentWikiData;
	}

	public void setTorrentWikiData(Collection<TorrentWikiData> torrentWikiData) {
		this.torrentWikiData = torrentWikiData;
	}

	public TorrentWikiData getShowTorrentWikiData() {
		return showTorrentWikiData;
	}

	public void setShowTorrentWikiData(TorrentWikiData enWikiData) {
		this.showTorrentWikiData = enWikiData;
	}

}
