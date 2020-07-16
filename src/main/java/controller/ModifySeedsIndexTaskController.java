package controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.hyperborian.bt.index.SeedIndexer;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkmax.zul.Tablechildren;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;

public class ModifySeedsIndexTaskController extends AbstractController {

	private static final long serialVersionUID = 1L;

	@Wire
	private Textbox name;
	
	@Wire
	private Intbox limit;
	
	@Wire
	private Intbox schedulePeriod;
	
	@Wire
	private Checkbox shouldDeleteZeroSeeding;
	
	@Wire
	private Intbox zeroSeedingThreshold;
	
	@Wire
	private Checkbox startFromMostSeeding;
	
	@Wire
	private Checkbox schedule;
	
	@Wire
	private Radio minutes;
	
	@Wire
	private Radio hours;
	
	@Wire
	private Tablechildren scheduleContainer;
	
	@Listen("onClick = #shouldDeleteZeroSeeding")
    public void showZeroSeedingThreshold(){
		zeroSeedingThreshold.setVisible(shouldDeleteZeroSeeding.isChecked());
    }
	
	@Listen("onClick = #schedule")
    public void showHideSchedule(){
		scheduleContainer.setVisible(schedule.isChecked());
    }
	

	private SeedIndexer indexer;
    
    @Listen("onClick = #saveButton")
    public void save() throws Exception {
    	indexer = (SeedIndexer)super.getObject();
    	indexer.setCreateTime(new Timestamp(new Date().getTime()));
    	indexer.setName(this.name.getValue());
    	indexer.setDeleteNotSeeding(shouldDeleteZeroSeeding.isChecked());
    	if(zeroSeedingThreshold.getValue()!=null && zeroSeedingThreshold.getValue()>=0) {
    		indexer.setZeroSeedingThreshold(zeroSeedingThreshold.getValue());
    	}
    	indexer.setStartFromMostSeeding(startFromMostSeeding.isChecked());
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
