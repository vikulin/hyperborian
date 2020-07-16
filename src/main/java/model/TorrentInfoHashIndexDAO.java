package model;

import java.util.Collection;

import org.hyperborian.bt.pojo.TorrentInfoHashIndex;

public interface TorrentInfoHashIndexDAO extends GenericDAO<TorrentInfoHashIndex,Long> {
	
	public Collection<TorrentInfoHashIndex> findAllMostSeeding(int offset, int limit);
	
	public Collection<TorrentInfoHashIndex> findAllLeastSeeding(int offset, int limit);

}
