package viewmodel;

import java.util.LinkedHashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;

import vmodel.NavigationPage;

public class NavigationViewModel {
	
	NavigationPage currentPage;
	
	private Map<String, Map<String, NavigationPage>> pageMap;

	@Init
	public void init() {
		initPageMap();
		currentPage = pageMap.get("Share torrents instantly!").get("Demo");
	}

	@Command
	public void navigatePage(@BindingParam("target") NavigationPage targetPage) {
		BindUtils.postNotifyChange(null, null, currentPage, "selected");
		currentPage = targetPage;
		BindUtils.postNotifyChange(null, null, this, "currentPage");
	}
	
	public NavigationPage getCurrentPage() {
		return currentPage;
	}

	public Map<String, Map<String, NavigationPage>> getPageMap() {
		return pageMap;
	}

	private void initPageMap() {
		pageMap = new LinkedHashMap<String, Map<String, NavigationPage>>();

		addPage("Share torrents instantly!", "Demo", "/template/upload_file.zul");
		
		addPage("Easy to search", "How it works", "/template/search-demo.zul", "software");
		
		addPage("Enjoy your music", "Demo", "/template/music-demo.zul", "/demo/video/music.mp4");
		addPage("Read books", "Demo", "/template/document-demo.zul", "/demo/video/document.mp4");
		
		//addPage("Help", "FAQ", "/template/faq.zul", "active");
		addPage("Help", "About Us", "/template/about_us.zul", "inactive");
		/*
		addPage("Orders", "Ready for Shipping", "/orders/orders.zul", "ready");
		addPage("Orders", "Shipping", "/orders/orders.zul", "shipping");
		addPage("Orders", "Specified for Later", "/orders/orders.zul", "later");

		addPage("Fan Service", "Events", "/fan_service/events.zul");
		addPage("Fan Service", "Promotion", "/fan_service/promotion.zul");*/
	}

	private void addPage(String title, String subTitle, String includeUri) {
		addPage(title, subTitle, includeUri, null);
	}

	private void addPage(String title, String subTitle, String includeUri, String data) {
		String folder = "";
		Map<String, NavigationPage> subPageMap = pageMap.get(title);
		if(subPageMap == null) {
			subPageMap = new LinkedHashMap<String, NavigationPage>();
			pageMap.put(title, subPageMap);
		}
		/*
		NavigationPage navigationPage = new NavigationPage(title, subTitle,
		folder + includeUri + "?random=" + Math.random(), data) {
		@Override
		public boolean isSelected() {
			return currentPage == this;
			}
		};*/
		NavigationPage navigationPage = new NavigationPage(title, subTitle,
				folder + includeUri, data) {
				@Override
				public boolean isSelected() {
					return currentPage == this;
					}
				};
		
		subPageMap.put(subTitle, navigationPage);
	}
}