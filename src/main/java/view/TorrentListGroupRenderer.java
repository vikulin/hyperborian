package view;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

import org.hibernate.Transaction;
import model.pojo.Torrent;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zkmax.zul.Filedownload;
import org.zkoss.zul.A;
import org.zkoss.zul.Html;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listgroup;
import org.zkoss.zul.Listgroupfoot;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Vlayout;

import controller.AbstractController;
import converter.HumanReadableByteCount;
import model.TorrentDAO;
import model.hibernate.HibernateUtil;
import model.hibernate.TorrentHibernateDAO;
import model.pojo.TorrentFileSearch;
import model.pojo.TorrentWikiData;
import vmodel.TorrentGroupsModel;
 

public class TorrentListGroupRenderer implements ListitemRenderer<Object> {
	
	private TorrentDAO torrentDAO;
	
	public TorrentListGroupRenderer() {
		torrentDAO = new TorrentHibernateDAO();
	}
	
	private void updateDownloads(Torrent torrent) {
		torrent.setDownloads(torrent.getDownloads()+1);
		Transaction tx = HibernateUtil.getSessionFactory().getCurrentSession().getTransaction();
		try {
			torrentDAO.update(torrent);
			tx.commit();
		} catch (Exception ex) {
			tx.rollback();
			ex.printStackTrace();
		}
	}
	
    @Override
    public void render(Listitem listitem, Object obj, int index) throws Exception {
 
        if (listitem instanceof Listgroup) {
        	Torrent groupInfo = (Torrent) obj;
            String torrentName = groupInfo.getName();
            A tb = new A();
            TorrentWikiData wikiData = groupInfo.getShowTorrentWikiData();
            if(wikiData!=null) {
				String title = wikiData.getWikiTitle() + " - " + torrentName;
				tb.setLabel(title);
    		} else {
    			tb.setLabel(torrentName);
    		}
             
            tb.setHref("/torrent_details.zhtml?id="+groupInfo.getId()+"&name="+URLEncoder.encode(torrentName, "UTF-8"));
            
            //tb.setHref("/torrent_list.zul?id="+groupInfo.getId()+"&name="+groupInfo.getName()+"&page="+page+"&redirect=true");
            Listcell groupName = new Listcell();
            tb.setParent(groupName);
            listitem.appendChild(groupName);
            listitem.appendChild(new Listcell(HumanReadableByteCount.humanReadableByteCount(groupInfo.getTotalSize())));
            
            A magnet = new A();
            magnet.setImage("/image/magnet-icon-main-24.png");
            magnet.setHeight("24px");
            magnet.setWidth("24px");
            magnet.setHoverImage("/image/magnet-icon-fix-24.png");
            final String magnetLink = AbstractController.getMagnetLink(groupInfo);
            magnet.setTooltiptext(magnetLink);
            magnet.addEventListener("onClick", new EventListener<Event>() {

				public void onEvent(Event event) throws Exception {
					Executions.getCurrent().sendRedirect(magnetLink, "hiddenIframe");
					String remoteIp = Executions.getCurrent().getRemoteAddr();
					if(AbstractController.downloadsByIp.get(remoteIp)==null) {
						try {
							updateDownloads(groupInfo);
						} finally {
							AbstractController.downloadsByIp.put(remoteIp+"|"+groupInfo.getId(), groupInfo.getId());
						}
					}
				}
            });
            
            /**
             * TODO add downloads for magnet button
             */
            A downloadIcon = new A();
            downloadIcon.setImage("/image/arrow-down-main-24.png");
            downloadIcon.setHeight("24px");
            downloadIcon.setWidth("24px");
            downloadIcon.setHoverImage("/image/arrow-down-fix-24.png");
            downloadIcon.addEventListener("onClick", new EventListener<Event>() {

				public void onEvent(Event event) throws Exception {
					String remoteIp = Executions.getCurrent().getRemoteAddr();
					final InputStream content = 
					        new DataInputStream(AbstractController.getTorrentFileInputStream(groupInfo));
					Filedownload.save(content, "application/x-bittorrent", groupInfo.getName()+".torrent");
					if(AbstractController.downloadsByIp.get(remoteIp)==null) {
						try {
							updateDownloads(groupInfo);
						} finally {
							AbstractController.downloadsByIp.put(remoteIp, groupInfo.getId());
						}
					}
				}
            	
            });
            Vlayout vLayoutM = new Vlayout();
            vLayoutM.appendChild(magnet);
            Listcell downloadButtonCellM = new Listcell();
            downloadButtonCellM.appendChild(vLayoutM);
            listitem.appendChild(downloadButtonCellM);
            Vlayout vLayout = new Vlayout();
            vLayout.appendChild(downloadIcon);
            Listcell downloadButtonCell = new Listcell();
            downloadButtonCell.appendChild(vLayout);
            listitem.appendChild(downloadButtonCell);
            listitem.setValue(obj);
            listitem.addEventListener("onClick", new EventListener<Event>() {

				public void onEvent(Event event) throws Exception {
					TorrentGroupsModel model = ((TorrentGroupsModel)((Listbox)listitem.getParent()).getGroupsModel());
					if(((Listgroup) listitem).isOpen()) {
						model.setOpenGroup0(model.searchGroupIndex(groupInfo), false);
					} else {
						model.setOpenGroup0(model.searchGroupIndex(groupInfo), true);
					}
				}
            	
            });
        } 
        /**
         * No footer
         */
        else if (listitem instanceof Listgroupfoot) {
        	
        	listitem.setVisible(false);
            /*Listcell cell = new Listcell();
            cell.setSclass("foodFooter");
            cell.setSpan(6);
            cell.appendChild(new Label("Total " + obj + " Items"));
            listitem.appendChild(cell);
            */
        } 
        else {
        	TorrentFileSearch data = (TorrentFileSearch) obj;
        	String wikiTitle = data.getWikiTitle();
        	Html container = new Html();
        	if(wikiTitle!=null) {
        		container.setContent(data.getHtmlWikiTorrentFileList());
        	} else {
        		container.setContent(data.getHtmlTorrentFileList());
        	}
        	
        	
        	Listcell listCell = new Listcell();
        	listCell.setSpan(3);
        	container.setParent(listCell);
            /**
             * TODO read metadata
             */
            listitem.appendChild(listCell);
            listitem.setValue(data);
        }
 
    }
 
}