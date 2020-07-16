package viewmodel;

import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

public class WikiDataViewModel {
	
	private ListModelList<String> indexerList;
	private String selected;

	@Init
	public void init(){
		this.indexerList = new ListModelList<String>();
		this.indexerList.add("Video");
		this.indexerList.add("Audio");
		this.indexerList.add("Software");
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
