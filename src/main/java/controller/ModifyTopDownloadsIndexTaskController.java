package controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.hyperborian.bt.index.TopDownloadIndexer;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkmax.zul.Tablechildren;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;

public class ModifyTopDownloadsIndexTaskController extends AbstractController {

	private static final long serialVersionUID = 1L;

	@Wire
	private Textbox name;
	
	@Wire
	private Intbox limit;
	
	@Wire
	private Intbox schedulePeriod;
	
	@Wire
	private Checkbox schedule;
	
	@Wire
	private Radio minutes;
	
	@Wire
	private Radio hours;
	
	@Wire
	private Tablechildren scheduleContainer;
	
	@Listen("onClick = #schedule")
    public void showHideSchedule(){
		scheduleContainer.setVisible(schedule.isChecked());
    }
	

	private TopDownloadIndexer indexer;
    
    @Listen("onClick = #saveButton")
    public void save() throws Exception {
    	indexer = (TopDownloadIndexer)super.getObject();
    	indexer.setCreateTime(new Timestamp(new Date().getTime()));
    	indexer.setName(this.name.getValue());
    	
    	if(limit.getValue()!=null && limit.getValue()>0) {
    		indexer.setLimit(limit.getValue());
    	}
    	if(!schedule.isChecked()) {
			Thread thread = new Thread(indexer);
			thread.setDaemon(true);
			thread.start();
    	} else {
    		if(schedulePeriod.getValue()==null || schedulePeriod.getValue()==0) {
    			throw new WrongValueException(schedulePeriod, "Schedule period is missing");
    		}
    		if(minutes.isChecked()) {
	    		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	    		scheduler.scheduleAtFixedRate(indexer, 0, schedulePeriod.getValue(), TimeUnit.MINUTES);
    		} else {
    			if(hours.isChecked()) {
	    			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		    		scheduler.scheduleAtFixedRate(indexer, 0, schedulePeriod.getValue(), TimeUnit.HOURS);
    			} else {
    				ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		    		scheduler.scheduleAtFixedRate(indexer, 0, schedulePeriod.getValue(), TimeUnit.DAYS);
    			}
    		}
    	}
    	
    	close();
    	reload(indexer);
    	Messagebox.show("Задача "+indexer.getName()+" успешно создана");
    }
}
