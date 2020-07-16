package controller;

import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Audio;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import model.pojo.TorrentFileParent;
import model.pojo.TorrentFileParentStatus;

public class PlayAudioController extends AbstractViewController {

	private static final long serialVersionUID = 3864520092009505943L;
	
	@Override
	protected void setTitle(Window comp) {
		
	}
	
	@Wire
	private Audio audio;
	
	@Wire
	private Label trackName;

	private TorrentFileParent nextTrack;
	
    @Listen("onEnded = #audio")
    public void finishedPlaying(Event event) {
    	//reset current track controls
    	PlayAudioController.super.reset(getObject());
    	playNextTrack(nextTrack);
    }
    
	protected void playNextTrack(TorrentFileParent nextTrack) {
    	if(nextTrack!=null && nextTrack.getExtension().equals("mp3")) {
    		try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		PlayAudioController.super.playThisTrack(nextTrack);
    	}
	}
    
	/*
	@Wire
	private Button playButton;
	
	@Wire
	private Button pauseButton;
	
	@Wire
	private Button stopButton;
	
	
	@Listen("onClick = #playButton")
	public void onClickPlayButton() throws InterruptedException {
		video.play();
	}
	
	@Listen("onClick = #pauseButton")
	public void onClickPauseButton() {
		video.pause();
	}
	
	@Listen("onClick = #stopButton")
	public void onClickStopButton() {
		video.stop();
	}
	*/

	@Override
    public void reload(Object obj) {
    	if(existDownloadInQueue()) {
    		Messagebox.show("It is busy. Please wait");
    		return;
    	}
		if(obj==null || !(obj instanceof TorrentFileParent)) {
			throw new WrongValueException("Torrent item is missing");
		}
		TorrentFileParent torrentFile = (TorrentFileParent)obj;
		setObject(torrentFile);
		download();
    }
 

	@Override
	protected void afterDownload() {
		audio.stop();
		audio.setSrc(getSrc());
		audio.setVisible(true);
		audio.setVisible(false);
		audio.setVisible(true);
		audio.play();
		trackName.setValue(((TorrentFileParent)getObject()).getPath());
		nextTrack = super.preloadingNext(getObject());
		if(nextTrack!=null && nextTrack.getExtension().equals("mp3")) {
			preload(nextTrack);
		}
	}

	@Override
	protected void beforeDownload() {
		   setStatus(TorrentFileParentStatus.LOADING);
		   audio.setVisible(true);
		   audio.setVisible(false);
		   audio.setVisible(true);
	}
    
}
