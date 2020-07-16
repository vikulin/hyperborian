package controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hyperborian.bt.pojo.classifier.TorrentClassifier;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkmax.zul.Tablelayout;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Window;

import model.pojo.Torrent;
import model.pojo.TorrentFileSearch;
import model.pojo.User;
import vmodel.TorrentGroupsModel;

public class TorrentListController extends AbstractTorrentFileSearchListController {
	
	private ListModelList<TorrentClassifier.Type> categoryModel;
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		categoryModel = new ListModelList<TorrentClassifier.Type>(Arrays.stream(TorrentClassifier.Type.values()).toArray(TorrentClassifier.Type[]::new));
		Execution exec = Executions.getCurrent();
		
		String p = exec.getParameter("page");
		if(p!=null) {
			try {
				Long page = Long.parseLong(p);
				pageNumber.setLabel(page+"");
			} catch(NumberFormatException ex) {
				pageNumber.setLabel("1");
			}
		} else {
			pageNumber.setLabel("1");
		}
		String n = exec.getParameter("name");
		if(n!=null) {
			if(n.length()>255) {
				n=n.substring(0, 255);
			}
			name.setText(n);
		}
		String sizeFrom = exec.getParameter("sizeFrom");
		if(sizeFrom!=null) {
			try {
				double sizeFromInt = Double.parseDouble(sizeFrom);
				if(sizeFromInt>9999) {
					sizeFromInt=9999;
				}
				if(sizeFromInt>=0) {
					this.sizeFrom.setValue(new BigDecimal(sizeFromInt));
				}
			} catch(NumberFormatException ex) {
				String value = null;
				this.sizeFrom.setValue(value);
			}
		}
		String sizeTo = exec.getParameter("sizeTo");
		if(sizeTo!=null) {
			try {
				double sizeToInt = Double.parseDouble(sizeTo);
				if(sizeToInt>9999) {
					sizeToInt=9999;
				}
				if(sizeToInt<0) {
					sizeToInt=0;
				}
				this.sizeTo.setValue(new BigDecimal(sizeToInt));
			} catch(NumberFormatException ex) {
				String value = null;
				this.sizeTo.setValue(value);
			}
		}

		Set<TorrentClassifier.Type> selectedCategory = new HashSet<TorrentClassifier.Type>();
		for(TorrentClassifier.Type category:TorrentClassifier.Type.values()) {
			String categoryParameter = exec.getParameter(category.name().toLowerCase());
			if(categoryParameter!=null) {
				selectedCategory.add(category);
			}
		}
		if(selectedCategory.size()>0) {
			((ListModelList<TorrentClassifier.Type>)categoryModel).setSelection(Arrays.asList(selectedCategory.stream().toArray(TorrentClassifier.Type[]::new)));
		} else {
			((ListModelList<TorrentClassifier.Type>)categoryModel).setSelection(Arrays.asList(new TorrentClassifier.Type[]{TorrentClassifier.Type.VIDEO}));
		}
		if(Integer.parseInt(pageNumber.getLabel())<2) {
			back.setHref(null);
		} else {
			back.setHref(getUrl(-1));
		}
		next.setHref(getUrl(1));
		categoryTab.setModel((ListModelList<TorrentClassifier.Type>)categoryModel);
		categoryListbox.setModel((ListModelList<TorrentClassifier.Type>)categoryModel);
		userName.setLabel(getUserName());
		search();
	}
	
    @Listen("onSelect = #categoryTab")
    public void selectTab(SelectEvent event) {
    	//TorrentClassifier.Type category = ((Tab)event.getTarget()).getValue();
    	onClickSearch();
    }
	
    @Listen("onSelect = #categoryListbox")
    public void selectCategory() {
    	onClickSearch();
    }
    @Listen("onOK = #name")
    public void onOkName() {
    	onClickSearch();
    }
    
    @Listen("onOK = #sizeFrom")
    public void onOkSizeFrom() {
    	onClickSearch();
    }
    
    @Listen("onOK = #sizeTo")
    public void onOkSizeTo() {
    	onClickSearch();
    }
    
	@Wire
	private Tablelayout logoutLayout;
	
	@Wire
	private Hbox loginLayout;
	
	@Wire
	private A userName;
	
    @Listen("onLoginOk = #torrents")
    public void onLoginOk() {
    	loginLayout.setVisible(false);
    	logoutLayout.setVisible(true);
    	userName.setLabel(getUserName());
    }
	
	private static final long serialVersionUID = 1L;
	
	@Wire
	private Button searchButton;
	
	@Wire
	private Tabbox categoryTab;
	
	@Wire
    private Listbox categoryListbox;

	@Wire
    private Listbox listbox;

	@Wire
    private Textbox name;

	@Wire
	private Tree categoriesTree;
	
	@Wire
	private A pageNumber;
	
	@Wire
	private Button back;
	
	@Wire
	private Button next;
	
	@Wire
	private Decimalbox sizeFrom;
	
	@Wire
	private Decimalbox sizeTo;
	
	@Listen("onChanging = #name")
	public void obChangingSearch(InputEvent event) {
		 
	}
	
		
	@Listen("onOpen = #name")
	public void onOpenSearchIcon(OpenEvent event) {
		 
	}
	
	@Listen("onClick = #searchButton")
	public void onClickSearch(){
		if(sizeFrom.getValue()!=null && sizeTo.getValue()!=null && sizeFrom.getValue().doubleValue()>sizeTo.getValue().doubleValue()) {
    		throw new WrongValueException(sizeFrom.getParent(), "Max value should be greater then Min value");
    	}
		pageNumber.setLabel("1");
		Executions.sendRedirect(getUrl(0));
	}
	
	private String getUrl(int pageOffset) {
		String categories = ((ListModelList<TorrentClassifier.Type>)categoryModel).getSelection().stream().map(c->"&"+c.name().toLowerCase()+"=1").collect(Collectors.joining(""));
		return "/index.zhtml?page="+(Integer.parseInt(pageNumber.getLabel())+pageOffset)+"&name="+name.getText()+"&sizeFrom="+this.sizeFrom.getText()+"&sizeTo="+this.sizeTo.getText()+categories;
	}
	
    public void search() throws MalformedURLException, IOException{
    	if(sizeFrom.getValue()!=null && sizeTo.getValue()!=null && sizeFrom.getValue().doubleValue()>sizeTo.getValue().doubleValue()) {
    		throw new WrongValueException(sizeFrom.getParent(), "Max value should be greater then Min value");
    	} else {
    		Clients.clearWrongValue(sizeFrom.getParent());
    	}
        String name = this.name.getValue();
        Double sizeFrom = this.sizeFrom.doubleValue();
        Double sizeTo = this.sizeTo.doubleValue();

		Set<TorrentClassifier.Type> categories = ((ListModelList<TorrentClassifier.Type>)categoryModel).getSelection();
		Collection<Torrent> result = getTorrentDAO().findByCriteria(name, categories, sizeFrom, sizeTo, getFirstResult(), 10);
		List<List<TorrentFileSearch>> torrentFiles = new ArrayList<>();
        List<Torrent> torrents = new ArrayList<Torrent>();
        List<Torrent> cleanUp = new ArrayList<Torrent>();
        Map<Long, Torrent> removeDuplicatedTorrents = new HashMap<Long, Torrent>();

        for(Torrent torrent:result) {
        	if(torrent.getTorrentWikiData().size()>0) {
        		Long wikiDataId = torrent.getTorrentWikiData().iterator().next().getWikiDataId();
        		if(removeDuplicatedTorrents.get(wikiDataId)!=null) {
        			continue;
        		} else {
        			removeDuplicatedTorrents.put(wikiDataId, torrent);
        			torrents.add(torrent);
        		}
        	} else {
        		torrents.add(torrent);
        	}
        	try {
	        	List<TorrentFileSearch> tsList = getTorrentFileSearchList(torrent, name);
	        	torrentFiles.add(tsList);
        	} catch (FileNotFoundException ex) {
        		ex.printStackTrace();
        		cleanUp.add(torrent);
        	}
        }
        torrents.removeAll(cleanUp);
        TorrentGroupsModel groupModel = new TorrentGroupsModel(torrentFiles, torrents);
        groupModel.setMultiple(true);
        listbox.setModel(groupModel);
    }
    
	private int getFirstResult() {
		return (Integer.parseInt(pageNumber.getLabel())-1)*10;
	}
	
	protected Listbox getListbox() {
		return listbox;
	}
	
	@Override
	public void reload(Object obj) {
		if(obj instanceof User) {
			
		}
	}
	
}
