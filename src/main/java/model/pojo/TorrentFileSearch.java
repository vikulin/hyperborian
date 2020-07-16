package model.pojo;

import java.util.ArrayList;
import java.util.List;

public class TorrentFileSearch {
	
	private String url;

	public TorrentFileSearch(String url) {
		torrentFileListText = new ArrayList<TaggedText>();
		this.url = url;
	}
	
	public class EmTaggedText implements TaggedText{
		
		public EmTaggedText(String text) {
			this.text = text;
		}
		
		private String text;

		public String getText() {
			return text;
		}
	}
	
	public class NotTaggedText implements TaggedText{
		
		public NotTaggedText(String text) {
			this.text = text;
		}
		
		private String text;

		public String getText() {
			return text;
		}
	}
	
	private List<TaggedText> torrentFileListText;
	
	private String browserLanguage;
	
	public String getBrowserLanguage() {
		return browserLanguage;
	}

	public void setBrowserLanguage(String browserLanguage) {
		this.browserLanguage = browserLanguage;
	}

	private String wikiTitle;

	public String getWikiTitle() {
		return wikiTitle;
	}

	public void setWikiTitle(String wikiTitle) {
		this.wikiTitle = wikiTitle;
	}

	public String getHtmlTorrentFileList() {
		String result="";
		for(TaggedText text:torrentFileListText) {
			if(text instanceof EmTaggedText) {
				result+="<em>"+text.getText()+"</em>";
			} else {
				if(text instanceof NotTaggedText) {
					result+=text.getText();
				}
			}
		}
		return result;
	}
	
	public String getHtmlWikiTorrentFileList() {
		String result="<div lang=\""+this.browserLanguage+"\" style=\"padding-right: 15px;float: left;\"><a class=\"z-a wiki_title\" href=\""+this.url+"\">"+wikiTitle+"</a></div>";
		result+="<div class=\"wiki_text\">";
		for(TaggedText text:torrentFileListText) {
			if(text instanceof EmTaggedText) {
				result+="<em>"+text.getText()+"</em>";
			} else {
				if(text instanceof NotTaggedText) {
					result+=text.getText();
				}
			}
		}
		result+="</div>";
		return result;
	}
	
	public void addEmTorrentFileListText(String textData) {
		this.torrentFileListText.add(new EmTaggedText(textData));
	}
	
	public void addTorrentFileListText(String textData) {
		this.torrentFileListText.add(new NotTaggedText(textData));
	}
	
	public int size() {
		return this.torrentFileListText.size();
	}

}
