package controller;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.http.WebManager;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Progressmeter;

import model.pojo.TorrentFileParent;

public class ViewImageController extends AbstractViewController {

	private static final long serialVersionUID = 8613800513243829045L;

	@Wire
	private Image report;
	
	@Wire
	private Div progress;
	
	@Wire
	private Progressmeter loadingIcon;

	private Timer timer;
	
	@Override
	protected void afterDownload() {
		progress.setVisible(false);
        report.setSrc(getSrc());
        report.setVisible(true);
        timer.cancel();
	}

	@Override
	protected void beforeDownload() {
		startProgressmeter();
	}
	
	
	public void startProgressmeter() {
		TimerTask timerTask = new ProgressTimerTask();
        //running timer task as daemon thread
        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 1*1000);
        
		EventQueue<Event> eq = EventQueues.lookup("timerEvent",  WebManager.getWebApp(WebApps.getCurrent().getServletContext()), true);
		// subscribe async listener to handle long operation
		eq.subscribe(new EventListener<Event>() {
			public void onEvent(Event evt) { // asynchronous
				
			}
		}, new EventListener<Event>() { // callback
			public void onEvent(Event evt) throws ClientProtocolException, IOException {
				if(evt.getName().equals("timerEvent")) {
					loadingIcon.setValue(((TorrentFileParent)getObject()).getProgress());
				}
			}
		});
	}
	
	class ProgressTimerTask extends TimerTask{

		@Override
		public void run() {
			try {
				EventQueues.lookup("timerEvent", WebManager.getWebApp(WebApps.getCurrent().getServletContext()), true).publish(new Event("timerEvent"));// kick off the long operation
			} catch (IllegalStateException ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
}