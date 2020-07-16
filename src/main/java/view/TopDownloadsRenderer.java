package view;

import java.net.URLEncoder;

import org.hyperborian.bt.pojo.Torrent;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.A;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
 

public class TopDownloadsRenderer implements ListitemRenderer<Object> {
	
    @Override
    public void render(Listitem listitem, Object obj, int index) throws Exception {
 
        if (listitem instanceof Listitem ) {
        	listitem.setHeight("64px");
        	Torrent groupInfo = (Torrent) obj;
            String torrentName = groupInfo.getName();
            A tb = new A(torrentName); 
            tb.setHref("/torrent_details.zhtml?id="+groupInfo.getId()+"&name="+URLEncoder.encode(groupInfo.getName(), "UTF-8"));
            Listcell groupName = new Listcell();
            tb.setParent(groupName);
            listitem.appendChild(groupName);
            listitem.addEventListener("onClick", new EventListener<Event>() {

				public void onEvent(Event event) throws Exception {
					Executions.getCurrent().sendRedirect("/torrent_details.zhtml?id="+groupInfo.getId()+"&name="+URLEncoder.encode(groupInfo.getName(), "UTF-8"));
				}
            	
            });
        } 
    }
}