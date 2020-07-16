package comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.hyperborian.bt.pojo.TorrentFile;
 

public class TorrentFileComparator implements Comparator<TorrentFile>, Serializable {
    
	private static final long serialVersionUID = 1L;
	
	private boolean asc;
	
    private int type;
    
    public TorrentFileComparator() {
        this.asc = true;
        this.type = 1;
    }
    
    public TorrentFileComparator(boolean asc, int type) {
        this.asc = asc;
        this.type = type;
    }
    
    public int getType() {
        return type;
    }
 
    public void setType(int type) {
        this.type = type;
    }
 
    public int compare(TorrentFile o1, TorrentFile o2) {
    
	    TorrentFile torrentFile1 = (TorrentFile) o1;
	    TorrentFile torrentFile2 = (TorrentFile) o2;
	    switch (type) {
	    case 1: // Compare Path
	    	if(o1.getPath()==null) {
	    		return 1;
	    	}
	    	if(o2.getPath()==null) {
	    		return -1;
	    	}
	    	if(o1.getPath().contains("/") && !o2.getPath().contains("/")) {
	    		return -1;
	    	}
	    	if(!o1.getPath().contains("/") && o2.getPath().contains("/")) {
	    		return 1;
	    	}
	        return o1.getPath().toLowerCase().compareTo(o2.getPath().toLowerCase())* (asc ? 1 : -1);
	    case 2: // Compare Size
	        return torrentFile1.getSize().compareTo(torrentFile2.getSize()) * (asc ? 1 : -1);
	    default: // Compare Path
	    	if(o1.getPath().contains("/") && !o2.getPath().contains("/")) {
	    		return -1;
	    	}
	    	if(!o1.getPath().contains("/") && o2.getPath().contains("/")) {
	    		return 1;
	    	}
	        return o1.getPath().toLowerCase().compareTo(o2.getPath().toLowerCase())* (asc ? 1 : -1);
	    }

    }
}