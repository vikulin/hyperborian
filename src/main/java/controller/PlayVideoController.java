package controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkmax.zul.Video;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;

import model.pojo.TorrentFileParent;
import zk.example.longoperations.LongOperation;

public class PlayVideoController extends AbstractController {

	private static final long serialVersionUID = 3864520092009505943L;
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		Object obj = getObject();
		if(obj==null || !(obj instanceof TorrentFileParent)) {
			throw new WrongValueException(comp,"Torrent item is missing");
		}
		torrentFile = (TorrentFileParent)obj;
		startTorrentDownload();
	}
	
	private TorrentFileParent torrentFile;
	
	protected String name;
	
	@Wire
	private Textbox url;
	
	@Wire
	private Video video;
	
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
	
    public void startTorrentDownload() {
    	
    	Gson gson = new Gson();
    	
    	LongOperation longOperation = new LongOperation() {
    		
            @Override
            protected void execute() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
            	//send REST request
            	//POST torrent
            	//Construct request body:
            	Set<String> pathSet = new HashSet<String>();
            	pathSet.add(torrentFile.getPath());
            	
            	
            	String json = gson.toJson(pathSet);
            	HttpClient httpClient = HttpClientBuilder.create().build();
            	
            	HttpPost request = new HttpPost("http://local.hyperborian.org/bt-streaming-service/rest/torrent/"+torrentFile.getTorrent().getInfoHash());
                StringEntity params =new StringEntity(json, "utf-8");
                //request.addHeader("content-type", "application/json; charset=utf-8");
                Header header = new BasicHeader(
                		  HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
				params.setContentType(header);
				params.setContentEncoding("UTF-8");
				request.setEntity(params);
                HttpResponse response = httpClient.execute(request);
                
                
                if(response.getStatusLine().getStatusCode()!=HttpStatus.CREATED.value() && response.getStatusLine().getStatusCode()!=HttpStatus.NOT_MODIFIED.value()) {
                	throw new WrongValueException(url, "Service returned error code:"+response.getStatusLine().getStatusCode());
                }
    		    while(getPiecesComplete(torrentFile)<30) {
    		    	Thread.sleep(1000);
    		    }
            }
 
            private int getPiecesComplete(TorrentFileParent torrentFile) throws ClientProtocolException, IOException, URISyntaxException {

            	String pathHash = Hashing.sha256().hashString(torrentFile.getPath(), Charsets.UTF_8 ).toString();
            	HttpClient httpClient = HttpClientBuilder.create().build();
            	String url = "http://local.hyperborian.org/bt-streaming-service/rest/chunk/"+torrentFile.getTorrent().getInfoHash()+"/"+pathHash;
            	System.err.println(url);
            	HttpGet request = new HttpGet(url);
                request.addHeader("content-type", "application/json");
                HttpResponse response = httpClient.execute(request);
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                System.err.print(responseString);
				return Integer.parseInt(responseString);
			}

			protected void onFinish() {
				String pathHash = Hashing.sha256().hashString(torrentFile.getPath(), Charsets.UTF_8 ).toString();
				String url = "http://local.hyperborian.org/bt-streaming-service/rest/stream/"+torrentFile.getTorrent().getInfoHash()+"/"+pathHash;
				video.setSrc(url);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
                //resultModel.addAll(result);
            	Clients.showNotification("Playing...", "info", null, null, 2000);
				//click Play button
            	video.play();
            	//Events.sendEvent(playButton, new Event("onClick"));
            	
            };
 
            @Override
            protected void onCleanup() {
                Clients.clearBusy();
            }

        };
 
        Clients.showBusy("Result coming in 3 seconds, please wait!");
        longOperation.start();
    }
 

}
