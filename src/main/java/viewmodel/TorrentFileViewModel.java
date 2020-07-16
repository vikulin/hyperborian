package viewmodel;

import java.util.Comparator;

import org.hyperborian.bt.pojo.TorrentFile;
import org.zkoss.bind.Converter;

import converter.HumanReadableByteCount;

public class TorrentFileViewModel {
	
	private Comparator<TorrentFile> pathAsc;
	private Comparator<TorrentFile> pathDsc;
	private Comparator<TorrentFile> sizeAsc;
	private Comparator<TorrentFile> sizeDsc;
	private Converter<?, ?, ?> humanReadableByteCount;
	
	public TorrentFileViewModel() {
		pathAsc = new comparator.TorrentFileComparator(true, 1);
		pathDsc = new comparator.TorrentFileComparator(false, 1);
		sizeAsc = new comparator.TorrentFileComparator(true, 2);
		sizeDsc = new comparator.TorrentFileComparator(false, 2);
		humanReadableByteCount = new HumanReadableByteCount();
	}
 
	public Comparator<TorrentFile> getPathAsc() {
		return pathAsc;
	}
	public Comparator<TorrentFile> getPathDsc() {
		return pathDsc;
	}
	public Comparator<TorrentFile> getSizeAsc() {
		return sizeAsc;
	}
	public Comparator<TorrentFile> getSizeDsc() {
		return sizeDsc;
	}
	
	public Converter<?, ?, ?> getHumanReadableByteCount() {
		return humanReadableByteCount;
	}
}
