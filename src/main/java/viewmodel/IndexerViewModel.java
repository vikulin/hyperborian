package viewmodel;

import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

public class IndexerViewModel {
	
	private ListModelList<String> indexerList;
	private String selected;

	@Init
	public void init(){
		this.indexerList = new ListModelList<String>();
		this.indexerList.add("Index seeders");
		this.indexerList.add("Index top downloads");
		this.indexerList.add("Delete duplicated records");
		this.indexerList.add("Google Custom Search Index");
	}
	
	public ListModel<String> getIndexerList(){
		return indexerList;
	}
	
	public void setSelected(String selected) {
		this.selected = selected;
	}

	public String getSelected() {
		return selected;
	}

}
