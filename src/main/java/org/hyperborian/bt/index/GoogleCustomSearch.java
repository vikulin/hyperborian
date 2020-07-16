package org.hyperborian.bt.index;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.hibernate.Transaction;
import org.hyperborian.bt.pojo.classifier.TorrentClassifier;
import org.hyperborian.bt.pojo.classifier.TorrentClassifier.Type;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import com.google.api.services.customsearch.model.Search.Spelling;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import controller.AbstractController;
import model.TorrentDAO;
import model.hibernate.HibernateUtil;
import model.hibernate.TorrentHibernateDAO;
import model.hibernate.TorrentWikiDataHibernateDAO;
import model.pojo.Torrent;
import model.pojo.TorrentWikiData;
import model.pojo.interfaces.Pojo;
import model.pojo.search.SearchResult;
import model.pojo.wikidata.Entities;
import model.pojo.wikidata.Labels;
import model.pojo.wikidata.P31;
import model.pojo.wikidata.Sitelinks;
import model.pojo.wikidata.WikiData;

public class GoogleCustomSearch implements Runnable, Pojo, Indexer {
	
		private static String[] video = {"Q11424", "Q202866", "Q5398426", "Q3464665", "Q15416", "Q18011172", "Q24869", "Q12912091", "Q29168811"};
	
		private static String[] software = {"Q7889", "Q7397", "Q1373429", "Q7058673", "Q758870", "Q2351962", "Q1155404", "Q229371", "Q23442338", "Q9135", "Q218616", "Q166142", "Q1639024"};
		
		private static String[] audio = {"Q482994", "Q4176708", "Q208569", "Q215380"};
		
		private Integer limit;
	
		private boolean stop = false;
	
		private Timestamp createTime;
	
		private TorrentDAO torrentDAO;
		
		private TorrentWikiDataHibernateDAO torrentWikiDataDAO;
		
		private String name;
		
		private int progress = 0;

		private double maxSize;
		
		private int processed = 0;

		private int part;

		private Long id;

		private boolean update;

		private String category;

		public Timestamp getCreateTime() {
			return createTime;
		}
		
		public Integer getLimit() {
			return limit;
		}

		public void setLimit(Integer limit) {
			this.limit = limit;
		}

		public boolean isStopped() {
			return stop;
		}

		public void stop() {
			this.stop = true;
		}

		public void setCreateTime(Timestamp createTime) {
			this.createTime = createTime;
		}

		public int getPart() {
			return part;
		}
		
		public int getProcessed() {
			return processed;
		}
		
		public int getProgress() {
			return progress;
		}

		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}

		private static long getSecureIndex() {
		    SecureRandom random = new SecureRandom();
		    while (true) {
		        long nextLong = random.nextLong();
		        if (nextLong > 0) {
		            return nextLong;
		        }
		    }
		}
	
		public GoogleCustomSearch() {
			this.id = getSecureIndex();
			torrentDAO = new TorrentHibernateDAO();
			torrentWikiDataDAO = new TorrentWikiDataHibernateDAO();
		}

		@Override
		public void run() {
			try {
				//reset counter
				processed = 0;
	
				Transaction tx2 = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
				Set<TorrentClassifier.Type> categories = new HashSet<TorrentClassifier.Type>();
				String[] wikiInstanceOf = {};
				if(category.equals("Video")) {
					wikiInstanceOf = video;
					categories.add(TorrentClassifier.Type.VIDEO);
				}
				if(category.equals("Software")) {
					wikiInstanceOf = software;
					categories.add(TorrentClassifier.Type.SOFTWARE);
					categories.add(TorrentClassifier.Type.DISK_IMAGES);
				}
				if(category.equals("Audio")) {
					wikiInstanceOf = audio;
					categories.add(TorrentClassifier.Type.AUDIO);
				}
				Collection<Torrent> top;
				if(!update) {
					top = torrentDAO.findByCriteriaNoWiki(null, categories, null, null, 0, limit);
				} else {
					top = torrentDAO.findByCriteria(null, categories, null, null, 0, limit);
				}
				tx2.commit();
				maxSize = top.size();
				for(Torrent torrent:top) {
					String name = torrent.getName();
					if(name.contains("XXX")) {
						continue;
					}
					try {
						String query = getSearchString(name);
						String wikiDataLink = getWikiDataLink(query, categories, wikiInstanceOf);
						if(wikiDataLink==null) {
							continue;
						}
						String wikiDataId = parseWikiDataId(wikiDataLink);
						WikiData wikiData = getWikiData(wikiDataLink);
						if(wikiData==null) {
							continue;
						}
						Entities entry = wikiData.getEntities().entrySet().iterator().next().getValue();
						List<P31> p31 = entry.getClaims().get("P31");
						if(p31==null || p31.size()==0) {
							continue;
						}
						boolean foundCategory = false;
						for(P31 o:p31) {
							String instanceOf = ((Map<?,?>)o.getMainsnak().getDatavalue().getValue()).get("id").toString();
							/**
							 * film, animated film or television series
							 */
							Optional<String> optional = Arrays.stream(wikiInstanceOf).filter(x -> instanceOf.equals(x)).findFirst();
							if(!(optional.isPresent())) {
								continue;
							} else {
								foundCategory = true;
								break;
							}
						}
						
						if(!foundCategory) {
							continue;
						}
						
						Set<TorrentWikiData> torrentWikiDataSet = new HashSet<TorrentWikiData>();
						Long wikiDataIdLong = Long.valueOf(wikiDataId.substring(1));
						for(Entry<String, Labels> label:entry.getLabels().entrySet()) {
							String languageCode = label.getKey();
							Sitelinks sitelinks = entry.getSitelinks().get(languageCode+"wiki");
							if(sitelinks==null) {
								/**
								 * no such language code found in sitelinks object
								 */
								continue;
							}
							TorrentWikiData torrentWikiData = new TorrentWikiData();
							torrentWikiData.setTorrent(torrent);
							
							String wikiTitle = sitelinks.getTitle();
							torrentWikiData.setWikiDataId(wikiDataIdLong);
							torrentWikiData.setLanguageCode(languageCode);
							torrentWikiData.setWikiTitle(wikiTitle);
					    	torrentWikiDataSet.add(torrentWikiData);
	
						}
						
						Transaction tx = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
						Collection<TorrentWikiData> existingTorrentWikiDataSet = null;
						if(torrent.getTorrentWikiData()!=null && torrent.getTorrentWikiData().size()>0) {
							existingTorrentWikiDataSet = new HashSet<TorrentWikiData>(torrent.getTorrentWikiData());
						}
						torrent.setTorrentWikiData(torrentWikiDataSet);
						torrentDAO.update(torrent);					
						if(existingTorrentWikiDataSet==null) {
							for(TorrentWikiData torrentWikiData:torrentWikiDataSet) {
								torrentWikiDataDAO.save(torrentWikiData);
							}
						} else {
							for(TorrentWikiData torrentWikiData:torrentWikiDataSet) {
								boolean updated = false;
								for(TorrentWikiData existingTorrentWikiData:existingTorrentWikiDataSet) {
									if(torrentWikiData.getLanguageCode().equals(existingTorrentWikiData.getLanguageCode())) {
										torrentWikiData.setId(existingTorrentWikiData.getId());
										torrentWikiDataDAO.update(torrentWikiData);
										updated = true;
										break;
									}
								}
								if(!updated) {
									torrentWikiDataDAO.save(torrentWikiData);
								}
							}
						}
						tx.commit();
			    		
					} catch (IOException e) {
						e.printStackTrace();
					}
					processed++;
					progress = (int)(100.0*(processed/maxSize));
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		public static WikiData getWikiData(String wikiDataLink) throws ClientProtocolException, IOException {
			String url = wikiDataLink.replace("EntityPage", "EntityData")+".json";
			HttpClient httpClient = HttpClientBuilder.create().build();
			System.err.println(url);
			HttpGet request = new HttpGet(url);
			request.addHeader("content-type", "application/json");
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String wikiDataresponse = EntityUtils.toString(entity, "UTF-8");
			Gson gson = new Gson();
			WikiData wikiData = null;
			try {
				wikiData = gson.fromJson(wikiDataresponse, WikiData.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				System.err.println(wikiDataLink);
				System.err.println(wikiDataresponse);
			}
			return wikiData;
		}
		
		private static String getWikiDataLink(String query, Set<Type> categories, String[] wikiInstanceOf) throws IOException {
			SearchResult searchResult = getWebSearchResult(query, categories, wikiInstanceOf);
			if(searchResult!=null && searchResult.getWikiDataLink()!=null) {
				return searchResult.getWikiDataLink();
			}
			List<Result> wikiSearchResults = null;
			
			if(searchResult==null) {
				
				/**
				 * additional words to search
				 */
				Type category = categories.iterator().next();
				switch (category) {
				case VIDEO:
					wikiSearchResults = searchWiki(query+" film", 10);
					break;
				default:
					wikiSearchResults = searchWiki(query, 10);
					break;
				}
				
			} else {
				String searchWebResult = searchResult.getTitle();
				wikiSearchResults = searchWiki(searchWebResult, 10);
			}
			if(wikiSearchResults!=null && wikiSearchResults.size()>0) {
				Result result = wikiSearchResults.get(0);
				if(wikiSearchResults.size()>1) {
					Result toCompare = wikiSearchResults.get(1);
					/**fixed incorrect google index for Nuts! film
					 * https://www.hyperborian.org/torrent_details.zhtml?id=303432&name=Nuts%21
					 */
					if(!result.getTitle().startsWith(query) && toCompare.getTitle().startsWith(query)) {
						String wikiLink = toCompare.getLink();
						if(wikiLink!=null) {
							String wikiDataLink = parseWikiDataLink(wikiLink);
							return wikiDataLink;
						}
					}
				}
				
				String wikiLink = result.getLink();
				if(wikiLink!=null) {
					String wikiDataLink = parseWikiDataLink(wikiLink);
					return wikiDataLink;
				}
			}
			return null;
		}
		
		/**
		 * Search Google CSE for query
		 * @param query to search for
		 * @param numOfResults of results to return
		 * @return List of results
		 * @throws IOException
		 */
		public static Search searchWeb(String query, int numOfResults) throws IOException {
		    Customsearch.Builder customSearch = new Customsearch.Builder(new NetHttpTransport(), new JacksonFactory(), null);
		    customSearch.setApplicationName("Search");
		    Customsearch.Cse.List list = customSearch.build().cse().list(query);
		    // ID
		    list.setKey(AbstractController.getGoogleCseKey());
		    list.setCx(AbstractController.getGoogleCseCx());
		    //Exact terms
		    Search items = null;
		    try {
		    	items = list.execute();
		    } catch (GoogleJsonResponseException ex) {
		    	ex.printStackTrace();
		    }
		    return items;
		}
		
		/**
		 * Search Google CSE for query
		 * @param query to search for
		 * @param numOfResults of results to return
		 * @return List of results
		 * @throws IOException
		 */
		public static List<Result> searchWiki(String query, int numOfResults) throws IOException {
		    List<Result> results = new ArrayList<Result>();
		    Customsearch.Builder customSearch = new Customsearch.Builder(new NetHttpTransport(), new JacksonFactory(), null);
		    customSearch.setApplicationName("Search");
		    Customsearch.Cse.Siterestrict.List list = customSearch.build().cse().siterestrict().list(query);
		    // ID
		    list.setKey(AbstractController.getGoogleCseKey());
		    list.setCx(AbstractController.getGoogleCseCx());
		    //Exact terms
		    List<Result> items = list.execute().getItems();
		    if(items!=null) {
		    	results.addAll(items);
		    }
		    return results;
		}
		
		public static SearchResult getWebSearchResult(String query, Set<Type> categories, String[] wikiInstanceOf) throws IOException {
			
			System.out.println(query);
			Search results = searchWeb(query,10);
			if(results==null) {
				return null;
			}
			Spelling spelling = results.getSpelling();
			if(results.getSearchInformation().getTotalResults()<3 && spelling!=null) {
				String corretedQuery = results.getSpelling().getCorrectedQuery();
				if(corretedQuery==null) {
					return null;
				}
				results = searchWeb(corretedQuery,10);
				if(results==null || results.getItems()==null) {
					return null;
				}
				System.out.println(results.size());
				Iterator<Result> result = results.getItems().iterator();
				while(result.hasNext()) {
					Result r = result.next();
					Map<String, List<Map<String, Object>>> pageMap = r.getPagemap();
					if(pageMap==null) {
						continue;
					}
					List<Map<String, Object>> page = pageMap.get("movie");
					if(page!=null && page.size()>0) {
						Map<String, Object> movie = page.get(0);
						Object name = movie.get("name");
						Object genre = movie.get("genre");
						if(name!=null && genre!=null) {
							SearchResult sr = new SearchResult();
							sr.setTitle(name.toString());
							return sr;
						}
					}
				}
			} else {
				if(results==null || results.getItems()==null) {
					return null;
				}
				Type category = categories.iterator().next();
				Iterator<Result> result = results.getItems().iterator();
				while(result.hasNext()) {
					Result r = result.next();
					String displayLink = r.getDisplayLink();
					if(displayLink.endsWith(".wikipedia.org")) {
						String wikiLink = r.getLink();
						if(wikiLink!=null) {
							
							String wikiDataLink = parseWikiDataLink(wikiLink);
							if(wikiDataLink==null) {
								continue;
							}
							SearchResult sr = parseWikiData(wikiDataLink, wikiInstanceOf);
							if(sr==null) {
								continue;
							} else {
								return sr;
							}
						}						
					}
					if(displayLink.endsWith(".imdb.com")) {
						SearchResult sr = new SearchResult();
						sr.setTitle(r.getTitle().replaceAll(" - IMDb", ""));
						return sr;
					}
					
					if(category.equals(Type.VIDEO)) {
						Map<String, List<Map<String, Object>>> pageMap = r.getPagemap();
						if(pageMap==null) {
							continue;
						}
						List<Map<String, Object>> page = pageMap.get("movie");
						if(page!=null && page.size()>0) {
							Map<String, Object> movie = page.get(0);
							Object name = movie.get("name");
							Object genre = movie.get("genre");
							if(name!=null && genre!=null) {
								SearchResult sr = new SearchResult();
								sr.setTitle(name.toString());
								return sr;
							}
						}
					} else {
						return null;
					}
				}
			}
			
			return null;
		}
		
		private static SearchResult parseWikiData(String wikiDataLink, String[] wikiInstanceOf) throws ClientProtocolException, IOException {
			WikiData wikiData = getWikiData(wikiDataLink);
			if(wikiData!=null) {
				Entities entry = wikiData.getEntities().entrySet().iterator().next().getValue();
				List<P31> p31 = entry.getClaims().get("P31");
				if(p31==null || p31.size()==0) {
					return null;
				}
				for(P31 o:p31) {
					String instanceOf = ((Map<?,?>)o.getMainsnak().getDatavalue().getValue()).get("id").toString();
					/**
					 * film, animated film or television series
					 */
					Optional<String> optional = Arrays.stream(wikiInstanceOf).filter(x -> instanceOf.equals(x)).findFirst();
					if(!(optional.isPresent())) {
						return null;
					} else {
						SearchResult sr = new SearchResult();
						sr.setWikiDataLink(wikiDataLink);
						return sr;
					}
				}
			}
			return null;
		}
		
		final static String regexFirstNumber = "^[^\\d]*(\\d+)";
		
		final static Pattern patternFirstNumber = Pattern.compile(regexFirstNumber);
		
		final static String wikiDataCss = "a[href*=\"Special:EntityPage\"]";
		
		private static String getSearchString(String name) {
			name = name.replaceAll("www\\..*?\\....", "").replaceAll("\\[.*?\\]", "").replaceAll("\\{.*\\}", "").replaceAll("2O1", "201");
			name = FilenameUtils.removeExtension(name).
					replaceAll("(?i)HDRip", "").
					replaceAll("(?i)BDRip", "").
					replaceAll("(?i)DLRip", "").
					replaceAll("(?i)WEBDLRip", "").
					replaceAll("(?i)SATRip", "").
					replaceAll("(?i)HDTVRip", "");
			Matcher matcher = patternFirstNumber.matcher(name);
			if (matcher.find()) {
			    //System.out.println("Full match: " + matcher.group(0));
				if(matcher.groupCount()>=1) {
					String firstNumber = matcher.group(1);
					int index = name.indexOf(firstNumber);
					if(index==0) {
						matcher = patternFirstNumber.matcher(name.substring(firstNumber.length()));
						if(matcher.find()) {
							firstNumber = matcher.group(1);
							index = name.indexOf(firstNumber);
						}
					}
					return name.substring(0,index+firstNumber.length()).replaceAll("\\[|\\]|_|\\.|-|\\(|\\)|\\{|\\}|\\$|\\?|\\+|\\*|\\^|@|\"|\'|:|,|;|~|=|&|#|<|>|/|／|！|\\?|：|♪|�"," ");
				}
			}
			return name.replaceAll("\\[|\\]|_|\\.|-|\\(|\\)|\\{|\\}|\\$|\\?|\\+|\\*|\\^|@|\"|\'|:|,|;|~|=|&|#|<|>|/|／|！|\\?|：|♪|�"," ");
		}
		
		private static String parseWikiDataLink(String url) throws IOException {

		    Document doc1 = Jsoup.connect(url).get();
		    Elements elements = doc1.select(wikiDataCss);
		    if(elements.size()>0) {
		    	String wikiDataUrl = elements.get(0).attr("href");
		    	return wikiDataUrl;
		    }
			return null;
		}
		
		private static String parseWikiDataId(String wikiDataLink) {
			int index = wikiDataLink.indexOf("/Q");
			return wikiDataLink.substring(index+1);
		}
		
		public static void main(String[] args) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
			System.out.println("test".contains(""));
		}
	
		@Override
		public Long getId() {
			return id;
		}

		@Override
		public void setId(Long id) {
			this.id=id;
		}

		public void setUpdate(boolean update) {
			this.update = update;
		}

		public void setCategory(String category) {
			this.category= category;
		}
		
	}