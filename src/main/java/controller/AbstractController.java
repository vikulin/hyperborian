package controller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.Transaction;
import model.pojo.Torrent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Window;

import model.TorrentDAO;
import model.hibernate.HibernateUtil;
import model.hibernate.TorrentHibernateDAO;
import model.pojo.TorrentFileParent;
import model.pojo.User;

public abstract class AbstractController extends SelectorComposer<Window> {
	
	private TorrentDAO torrentDAO;
	
	public AbstractController() {
		torrentDAO = new TorrentHibernateDAO();
	}
	
	protected TorrentDAO getTorrentDAO() {
		return torrentDAO;
	}
	
	protected void updateDownloads(Torrent torrent) {
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
	
	public static Map<String, Long> downloadsByIp = new HashMap<String, Long>();
	
	private static Properties config = new Properties();
	
	protected final static String registrationEmailTemplate;

	static {
	    try { 
	        config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("server.properties"));
	        registrationEmailTemplate = readFromInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("registration-email-template.html"));
	    } catch (IOException e) {
	        throw new ExceptionInInitializerError("Cannot load properties file.");
	    }
	}
	
	public static String readFromInputStream(InputStream inputStream) throws IOException {
	    StringBuilder resultStringBuilder = new StringBuilder();
	    try (BufferedReader br
	      = new BufferedReader(new InputStreamReader(inputStream))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            resultStringBuilder.append(line).append("\n");
	        }
	    }
	    return resultStringBuilder.toString();
	}
	
	public static String getBtServiceUrl(){
		return config.getProperty("bt_service_url");
	}
	
	public static String getBtIndexUrl(){
		return config.getProperty("bt_index_url");
	}
	
	public static String getBasePath(){
		return config.getProperty("base_path");
	}
	
	public static String getGoogleCseKey(){
		return config.getProperty("google_cse_key");
	}
	
	public static String getGoogleCseCx(){
		return config.getProperty("google_cse_cx");
	}
	
	public static String getTorrentUrl(Torrent torrent) {
		if(torrent.getLocalPath().startsWith("http")) {
			return torrent.getLocalPath();
		} else {
			return getBasePath()+"/"+torrent.getTorrentFileLocalPath();
		}
	}
	
	public static InputStream getTorrentFileInputStream(Torrent torrent) throws MalformedURLException, IOException {
		if(torrent.getLocalPath().startsWith("http")) {
			return new URL(torrent.getLocalPath()).openStream();
		} else {
			return new FileInputStream(getBasePath()+"/"+torrent.getTorrentFileLocalPath());
		}
	}
	
	public String getBrowserLocale() {
		String header = Executions.getCurrent().getHeader("accept-language");
		if(header!=null && header.trim().length()>0) {
			int indexFirstLocale = header.indexOf(',');
			if(indexFirstLocale>0) {
				return header.substring(0, indexFirstLocale);
			} else {
				return header; 
			}
		} else {
			return null;
		}
	}
	
	public static String getMagnetLink(Torrent torrent) throws UnsupportedEncodingException {
		return "magnet:?xt=urn:btih:"+torrent.getInfoHash()+"&dn="+URLEncoder.encode(torrent.getName(), "UTF-8")+".torrent";
	}
	
	private static final long serialVersionUID = 1964345598793160341L;
	
	public static String RANDOM_STRING = "BPNN7HWYVRDLZQ2OXCFHQWVEARYG3UWS";

	private Object obj;
	private AbstractController parentController;
	private static final String selectedItem = "object";
	private static final String controller = "controller";
	
	private Map<String, Window> childWindow = new HashMap<String, Window>();
	
	private DateTimeFormatter dateTimeFormat;

	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		final Execution execution = Executions.getCurrent();
		obj = execution.getArg().get(selectedItem);
		parentController = (AbstractController) execution.getArg().get(controller);
		comp.setAttribute(controller, this);
		String locale = getBrowserLocale();
		if(locale!=null && locale.indexOf('-')>0) {
			String[] localeArray = locale.split("-");
			Locale loc = new Locale(localeArray[0], localeArray[1]);
			FormatStyle dateTimeStyle = FormatStyle.MEDIUM;
			dateTimeFormat = DateTimeFormatter.ofLocalizedDateTime(dateTimeStyle).withLocale(loc);
		} else {
			dateTimeFormat = DateTimeFormatter.RFC_1123_DATE_TIME;
		}
	}
	
	public static String getLanguage(String locale) {
		if(locale!=null && locale.indexOf('-')>0) {
			String[] localeArray = locale.split("-");
			return localeArray[0];
		}
		if(locale!=null && locale.indexOf('-')<0) {
			return locale;
		}
		return null;
	}
	
	protected DateTimeFormatter getDateTimeFormatter() {
		return dateTimeFormat;
	}
	
	public void openWindow(String page, Object obj) {
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put(selectedItem, obj);
		arguments.put(controller, this);
		try {
	        Window window = (Window)Executions.createComponents(page, null, arguments);
	        window.doModal();
		} catch (SuspendNotAllowedException e) {
			e.printStackTrace();
		} catch (UiException e) {
			e.printStackTrace();
			alert(e.getMessage());			
		} catch (Exception e) {
			e.printStackTrace();
			alert(e.getMessage());
		}
	}
	
	public void openWindow(String page, Object obj, String mode) {
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put(selectedItem, obj);
		arguments.put(controller, this);
		try {
	        Window window = (Window)Executions.createComponents(page, null, arguments);
	        if(mode==null) {
	        	window.doModal();
	        	return;
	        }
	        if(mode.equals("overlapped")) {
	        	window.doOverlapped();
	        	return;
	        }
	        if(mode.equals("highlighted")) {
	        	window.doHighlighted();
	        	return;
	        }
		} catch (SuspendNotAllowedException e) {
			e.printStackTrace();
		} catch (UiException e) {
			e.printStackTrace();
			alert(e.getMessage());			
		} catch (Exception e) {
			e.printStackTrace();
			alert(e.getMessage());
		}
	}
	
	public void openWindow(String page, Object obj, String mode, boolean refresh) {
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put(selectedItem, obj);
		arguments.put(controller, this);
		try {
	        

	        	Window child = childWindow.get(page);
	        	if(child==null) {
	        		Window window = (Window)Executions.createComponents(page, null, arguments);
	        		childWindow.put(page, window);
	        		if(mode==null) {
	    	        	window.doModal();
	    	        	return;
	    	        }
	    	        if(mode.equals("overlapped")) {
	    	        	window.doOverlapped();
	    	        	return;
	    	        }
	    	        if(mode.equals("highlighted")) {
	    	        	window.doHighlighted();
	    	        	return;
	    	        }
	        	} else {
	    	        if(refresh) {
	    	        	((AbstractController)child.getAttribute(controller)).reload(obj);
	    	        } else {
	    	        	Window window = (Window)Executions.createComponents(page, null, arguments);
	    	        	//Window oldTrack = childWindow.replace(page, window);
		        		if(mode==null) {
		    	        	window.doModal();
		    	        	return;
		    	        }
		    	        if(mode.equals("overlapped")) {
		    	        	window.doOverlapped();
		    	        	return;
		    	        }
		    	        if(mode.equals("highlighted")) {
		    	        	window.doHighlighted();
		    	        	return;
		    	        }
	    	        }
	        	}
	        
		} catch (SuspendNotAllowedException e) {
			e.printStackTrace();
		} catch (UiException e) {
			e.printStackTrace();
			alert(e.getMessage());			
		} catch (Exception e) {
			e.printStackTrace();
			alert(e.getMessage());
		}
	}
	
	public Object getObject() {
		return obj;
	}
	
	public void setObject(Object obj) {
		this.obj = obj;
	}
	
    @Listen("onClick = #closeButton")
    public void close() throws Exception {
    	Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    	Transaction tr = session.getTransaction();
    	try {
    		tr.commit();
    	} catch (Exception ex){
    		tr.rollback();
    		throw new Exception(ex);
    	}
    	getSelf().detach();
    }
	
	public void reload(Object obj){
		parentController.reload(obj);
	}
	
	public void loading(Object obj){
		parentController.loading(obj);
	}
	
	public void progressStatus(Object obj){
		parentController.progressStatus(obj);
	}
	
	public TorrentFileParent preloadingNext(Object obj){
		return parentController.preloadingNext(obj);
	}
	
	public void playThisTrack(Object obj) {
		parentController.playThisTrack(obj);
	}
	
	public void reset(Object obj){
		parentController.reset(obj);
	}
	
	public void delete(Object obj){
		parentController.delete(obj);
	}
	
	protected String getUserName() {
		Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(obj instanceof String) {
			return (String)obj;
		}
		if(obj instanceof User) {
			return ((User)obj).getUsername();
		}
		return null;
	}
}
