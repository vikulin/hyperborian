package vmodel;

import java.util.List;

import model.pojo.Torrent;
import org.zkoss.zul.SimpleGroupsModel;

import model.pojo.TorrentFileSearch;

public class TorrentGroupsModel extends SimpleGroupsModel{
    private static final long serialVersionUID = 1L;
 
    public TorrentGroupsModel(List<List<TorrentFileSearch>> torrentFiles, List<Torrent> torrent, List<Object> foots, boolean[] closed) {
        super(torrentFiles, torrent, foots, closed);
    }
    
    public TorrentGroupsModel(List<List<TorrentFileSearch>> torrentFiles, List<Torrent> torrent) {
        super(torrentFiles, torrent);
    }
    
    
    public void updateAllChildsInGroup(int groupIndex, List<TorrentFileSearch> data) {
    	_data.set(groupIndex, data);
    }
    
    public Integer searchGroupIndex(Torrent torrent) {
    	for(Object t:_heads) {
    		if(torrent.getId()==((Torrent)t).getId()) {
    			return _heads.indexOf(t);
    		}
    	}
    	return null;
    }
    
}