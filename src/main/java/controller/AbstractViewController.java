package controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.hyperborian.bt.pojo.TorrentDownloadStatus;
import org.hyperborian.bt.pojo.TorrentMetainfo;
import org.springframework.http.HttpStatus;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.google.gson.Gson;

import model.pojo.TorrentFileParent;
import model.pojo.TorrentFileParentStatus;

public abstract class AbstractViewController extends AbstractController {

	private static final long serialVersionUID = 8613800513243829045L;

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		if(existDownloadInQueue()) {
			comp.detach();
			Messagebox.show("It is busy. Please wait");
		}
		Object obj = getObject();
		if (obj == null || !(obj instanceof TorrentFileParent)) {
			throw new WrongValueException(comp, "Torrent item is missing");
		}
		setTitle(comp);
		download();
	}
	
	protected boolean existDownloadInQueue() {
		if (EventQueues.exists("longop")) {
			return true; // busy
		}
		return false;
	}

	protected void setTitle(Window comp) {
		comp.setTitle(((TorrentFileParent)getObject()) .getPath());
	}

	Gson gson = new Gson();

	private String url;

	protected String getSrc() {
		return url;
	}

	private TorrentDownloadStatus downloadProgress(TorrentFileParent torrentFile)
			throws ClientProtocolException, IOException, URISyntaxException {

		String path = URLEncoder.encode(torrentFile.getPath(),"UTF-8");
		HttpClient httpClient = HttpClientBuilder.create().build();
		String url = getBtServiceUrl() + "/status/" + torrentFile.getTorrent().getInfoHash() + "/" + path;
		System.err.println(url);
		HttpGet request = new HttpGet(url);
		request.addHeader("content-type", "application/json");
		HttpResponse response = httpClient.execute(request);
		HttpEntity entity = response.getEntity();
		String responseStatus = EntityUtils.toString(entity, "UTF-8");
		TorrentDownloadStatus status = gson.fromJson(responseStatus, TorrentDownloadStatus.class);

		return status;
	}

	private Object getTorrentMetadata(TorrentFileParent torrentFile)
			throws ClientProtocolException, IOException, URISyntaxException {

		String path = URLEncoder.encode(torrentFile.getPath(),"UTF-8");
		HttpClient httpClient = HttpClientBuilder.create().build();
		String url = getBtServiceUrl() + "/metainfo/" + torrentFile.getTorrent().getInfoHash() + "/" + path;
		System.err.println(url);
		HttpGet request = new HttpGet(url);
		request.addHeader("content-type", "application/json");
		HttpResponse response = httpClient.execute(request);
		if(response.getStatusLine().getStatusCode()!=HttpStatus.OK.value()) {
			return response;
		}
		HttpEntity entity = response.getEntity();
		String responseStatus = EntityUtils.toString(entity, "UTF-8");
		TorrentMetainfo metainfo = gson.fromJson(responseStatus, TorrentMetainfo.class);

		return metainfo;
	}
	
	private void setUrl(TorrentFileParent torrentFile) throws UnsupportedEncodingException {
		String path = URLEncoder.encode(torrentFile.getPath(),"UTF-8");
		url = getBtServiceUrl() + "/stream/" + torrentFile.getTorrent().getInfoHash() + "/" + path;
	}

	public void startTorrentDownload(TorrentFileParent torrentFile)
			throws ClientProtocolException, IOException, URISyntaxException, InterruptedException {
		// send REST request
		// POST torrent
		// Construct request body:
		Set<String> pathSet = new HashSet<String>();
		pathSet.add(torrentFile.getPath());

		String json = gson.toJson(pathSet);
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost request = new HttpPost(getBtServiceUrl() + "/torrent/" + torrentFile.getTorrent().getInfoHash());
		StringEntity params = new StringEntity(json, "utf-8");
		// request.addHeader("content-type", "application/json; charset=utf-8");
		Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
		params.setContentType(header);
		params.setContentEncoding("UTF-8");
		request.setEntity(params);
		HttpResponse response = httpClient.execute(request);
		if (response.getStatusLine().getStatusCode() != HttpStatus.CREATED.value()
				&& response.getStatusLine().getStatusCode() != HttpStatus.NOT_MODIFIED.value()) {
			Messagebox.show("Service returned error code:" + response.getStatusLine().getStatusCode());
			return;
		}
	}

	public void download() {
		EventQueue<Event> eq = EventQueues.lookup("download"); // create a queue
		// subscribe async listener to handle long operation
		eq.subscribe(new EventListener<Event>() {
			public void onEvent(Event evt) { // asynchronous
				if(evt.getName().equals("downloadTorrentEvent")) {
					TorrentFileParent torrentFile = ((TorrentFileParent)getObject());
					try {
						setUrl(torrentFile);
						startTorrentDownload(torrentFile);
						Object o = getTorrentMetadata(torrentFile);
						if(o instanceof HttpResponse) {
							((TorrentFileParent)getObject()).setStatus(TorrentFileParentStatus.ERROR);
							((TorrentFileParent)getObject()).setStatusCode(((HttpResponse) o).getStatusLine().getStatusCode());
							((TorrentFileParent)getObject()).setStatusMessage(((HttpResponse) o).getStatusLine().getReasonPhrase());
							return;
						}
						/**Fix
						 * Exception in thread "Thread-41" java.lang.ClassCastException: org.apache.http.impl.execchain.HttpResponseProxy cannot be cast to org.hyperborian.bt.pojo.TorrentMetainfo
							        at controller.AbstractViewController$1.onEvent(AbstractViewController.java:162)
							        at org.zkoss.zk.ui.event.impl.DesktopEventQueue$AsyncListenerThread.run(DesktopEventQueue.java:218)
						 */
						if(o instanceof TorrentMetainfo) {
							while (((TorrentMetainfo)o).getChunkSize() == 0) {
								Thread.sleep(1000);
								o = getTorrentMetadata(torrentFile);
							}
						} else {
							return;
						}
						TorrentMetainfo torrentMetadata = (TorrentMetainfo)getTorrentMetadata(torrentFile);
						boolean isComplete = false;
						String error = null;
						while (!isComplete) {
							TorrentDownloadStatus status = downloadProgress(torrentFile);
							isComplete = status.isComplete();
							error = status.getError();
							if(error!=null) {
								break;
							}
							int progress = (int)(100 * status.getChunkComplete()
									/ (Math.ceil(torrentFile.getSize() / torrentMetadata.getChunkSize())));
							if(progress>100) {
								progress = 100;
							}
							torrentFile.setProgress(progress);
							Thread.sleep(1000);
						}
						if(error!=null) {
							((TorrentFileParent)getObject()).setStatus(TorrentFileParentStatus.ERROR);
							((TorrentFileParent)getObject()).setStatusMessage(error);
						}
					} catch (IOException | URISyntaxException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}, new EventListener<Event>() { // callback
			public void onEvent(Event evt) throws ClientProtocolException, IOException {
				if(evt.getName().equals("downloadTorrentEvent")) {
					EventQueues.remove("download");
					
					/**
					 * reload parent controller (TorrentDetailsController)
					 */
					if(((TorrentFileParent)getObject()).getStatus()==TorrentFileParentStatus.ERROR) {
						AbstractViewController.super.reset(((TorrentFileParent)getObject()));
						Messagebox.show(((TorrentFileParent)getObject()).getStatusMessage(), "Error: code "+((TorrentFileParent)getObject()).getStatusCode(), Messagebox.OK, Messagebox.EXCLAMATION);
					} else {
						afterDownload();
						AbstractViewController.super.reload(((TorrentFileParent)getObject()));
					}
					
					HttpClient httpClient = HttpClientBuilder.create().build();
					String path = URLEncoder.encode(((TorrentFileParent)getObject()).getPath(),"UTF-8");
					HttpDelete request = new HttpDelete(
							getBtServiceUrl() + "/stream/" + ((TorrentFileParent)getObject()).getTorrent().getInfoHash() + "/" + path);
					// request.addHeader("content-type", "application/json; charset=utf-8");
					HttpResponse response = httpClient.execute(request);
					
				} 
				
				/*Not implemented individual progress: else {
					//TODO it doesn't have any other events except downloadTorrentEvent yet
					AbstractViewController.super.progressStatus(((TorrentFileParent)getObject()));
				}*/
			}
		});
		beforeDownload();
		eq.publish(new Event("downloadTorrentEvent")); // kick off the long operation
	}
	
	public void preload(TorrentFileParent torrentFile) {
		EventQueue<Event> eq = EventQueues.lookup("preload"); // create a queue
		// subscribe async listener to handle long operation
		eq.subscribe(new EventListener<Event>() {
			public void onEvent(Event evt) { // asynchronous
				if(evt.getName().equals("preloadTorrentEvent")) {
					try {
						startTorrentDownload(torrentFile);
						Object o = getTorrentMetadata(torrentFile);
						if(o instanceof HttpResponse) {
							((TorrentFileParent)getObject()).setStatus(TorrentFileParentStatus.ERROR);
							((TorrentFileParent)getObject()).setStatusMessage(((HttpResponse) o).getStatusLine().getReasonPhrase());
							return;
						}
						while (((TorrentMetainfo)o).getChunkSize() == 0) {
							Thread.sleep(1000);
							o = getTorrentMetadata(torrentFile);
						}
						TorrentMetainfo torrentMetadata = (TorrentMetainfo)getTorrentMetadata(torrentFile);
						boolean isComplete = false;
						String error = null;
						while (!isComplete) {
							TorrentDownloadStatus status = downloadProgress(torrentFile);
							isComplete = status.isComplete();
							error = status.getError();
							if(error!=null) {
								break;
							}
							int progress = (int)(100 * status.getChunkComplete()
									/ (Math.ceil(torrentFile.getSize() / torrentMetadata.getChunkSize())));
							if(progress>100) {
								progress = 100;
							}
							torrentFile.setProgress(progress);
							Thread.sleep(1000);
						}
						if(error!=null) {
							((TorrentFileParent)getObject()).setStatus(TorrentFileParentStatus.ERROR);
							((TorrentFileParent)getObject()).setStatusMessage(error);
						}
					} catch (IOException | URISyntaxException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}, new EventListener<Event>() { // callback
			public void onEvent(Event evt) throws ClientProtocolException, IOException {
				if(evt.getName().equals("preloadTorrentEvent")) {
					EventQueues.remove("preload");
					/**
					 * reload parent controller (TorrentDetailsController)
					 */
					AbstractViewController.super.reset(torrentFile);
					if(((TorrentFileParent)getObject()).getStatus()==TorrentFileParentStatus.ERROR) {
						Messagebox.show(((TorrentFileParent)getObject()).getStatusMessage(), "Error", Messagebox.OK, Messagebox.EXCLAMATION);
					}
					
					HttpClient httpClient = HttpClientBuilder.create().build();
					String path = URLEncoder.encode(torrentFile.getPath(),"UTF-8");
					HttpDelete request = new HttpDelete(
							getBtServiceUrl() + "/stream/" + (torrentFile).getTorrent().getInfoHash() + "/" + path);
					// request.addHeader("content-type", "application/json; charset=utf-8");
					HttpResponse response = httpClient.execute(request);
				}
			}
		});
		eq.publish(new Event("preloadTorrentEvent")); // kick off the long operation
	}
	
	protected void setStatus(TorrentFileParentStatus status) {
		((TorrentFileParent)getObject()).setStatus(status);
		AbstractViewController.super.loading(((TorrentFileParent)getObject()));
	}
	
	protected abstract void beforeDownload();
	
	protected abstract void afterDownload();


}