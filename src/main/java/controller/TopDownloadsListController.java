package controller;

import java.util.Collection;

import model.pojo.Torrent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;

import model.TorrentDAO;
import model.hibernate.TorrentHibernateDAO;

public class TopDownloadsListController extends PojoListboxController{

	private static final long serialVersionUID = -85210011269588451L;
	
	private TorrentDAO torrentDAO;
	
	public TopDownloadsListController() {
		torrentDAO = new TorrentHibernateDAO();
	}
	
	@Wire
	private Listbox topDownloads;

	@Override
	protected Listbox getListbox() {
		return topDownloads;
	}

	@Override
	public void search() {
		Collection<Torrent> result = torrentDAO.findTopDownloads(0, 10);
		topDownloads.setModel(new ListModelList<Torrent>(result));
	}	

}
