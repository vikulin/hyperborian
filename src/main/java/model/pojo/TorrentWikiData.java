package model.pojo;

import model.pojo.interfaces.Pojo;

public class TorrentWikiData implements Pojo  {
	
	
	private Long id;
	
	//this is wikidata id without Q prefix
	private Long wikiDataId;
	
	private Torrent torrent;
	
	private String languageCode;
	
	private String wikiTitle;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getWikiDataId() {
		return wikiDataId;
	}

	public void setWikiDataId(Long wikiDataId) {
		this.wikiDataId = wikiDataId;
	}

	public Torrent getTorrent() {
		return torrent;
	}

	public void setTorrent(Torrent torrent) {
		this.torrent = torrent;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getWikiTitle() {
		return wikiTitle;
	}

	public void setWikiTitle(String wikiTitle) {
		this.wikiTitle = wikiTitle;
	}

}
