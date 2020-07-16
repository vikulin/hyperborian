package org.hyperborian.bt.index;

import java.sql.Timestamp;

public interface Indexer {
	
	public Timestamp getCreateTime();
	
	public boolean isStopped();

	public int getPart();
	
	public int getProcessed();
	
	public int getProgress();

	public String getName();
	
	public void stop();
	
}
