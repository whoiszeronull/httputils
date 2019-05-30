package com.hu.utils.httputils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GoogleSpider<T> implements Runnable {

	private String html; // the google search result webpage html content.
	private int totalPageNum; // the total page number need to be processed from the search result pages.

	private static HtmlProcessor processor;

	public final static int MAX_TOTAL_PAGE = 500;

	private static List<String> results = Collections.synchronizedList(new ArrayList<>());

	// for multi threading.
	private static ThreadPoolExecutor exe;

	private String previous; // the url link for the "previous page" on the bottom of google search result
								// pages
	private Set<String> navLinks = new HashSet<>(); // the navigation for pages [1,2,3....]
	private String next; // // the url link for the "next page" on the bottom of google search result
							// pages

	// the pages that already have been searched and processed from the google
	// search result pages.
	private static Set<String> donePages = Collections.synchronizedSet(new HashSet<String>());

	/**
	 * if using this constractor, then it will call another constractor by using
	 * some default settings. After processing all the webpage search result based
	 * on given keyWord. The result will be populated in the filed List<String>
	 * results which containing all the nonAdLinks, then client can get the results
	 * for using.
	 * 
	 * @param keyWord  the keyword to search for by google.com
	 * @param pageNums the total page numbers to run thru from the search result
	 *                 pages.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public GoogleSpider(String keyWord, int pageNums) throws ClientProtocolException, IOException {
		this(GoogleSpider.getGoogleFirstPage(keyWord), pageNums, new GoogleHtmlNonAdLinksProcessor(), (ThreadPoolExecutor) Executors.newCachedThreadPool());
	}

	// use inner class as default procssor for the html content.
	private static class GoogleHtmlNonAdLinksProcessor implements HtmlProcessor {

		@Override
		public void process(String htmlContent) {
			List<String> googleNonAdLinks = GoogleSpider.getGoogleNonAdLinks(htmlContent);
			results.addAll(convertToHosts(googleNonAdLinks));
		}
	}

	private static Set<String> convertToHosts(List<String> links) {
		Set<String> hosts = new HashSet();
		for (String link : links) {
			try {
				hosts.add(new URL(link).getHost());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return hosts;
	}

	public GoogleSpider(String keyWord, int pageNums, HtmlProcessor processor) throws ClientProtocolException, IOException {
		this(GoogleSpider.getGoogleFirstPage(keyWord), pageNums, processor, (ThreadPoolExecutor) Executors.newCachedThreadPool());
	}
	
	/**
	 * 
	 * @param html
	 * @param totalPageNum
	 * @param processor
	 * @param exe
	 */
	public GoogleSpider(String html, int totalPageNum, HtmlProcessor processor, ThreadPoolExecutor exe) {
		this.html = html;
		// if the input total page Num is less than 0 or greater than MAX_TOTAL_PAGE,
		// then set the totalPage Num to be MAX_TOTAL_PAGE.
		if (totalPageNum < 1 || totalPageNum > MAX_TOTAL_PAGE) {
			this.totalPageNum = MAX_TOTAL_PAGE;
		}

		this.totalPageNum = totalPageNum;
		GoogleSpider.processor = processor;
		GoogleSpider.exe = exe;
	}

	/**
	 * Use google engine to get the search result content based on the given ke
	 * word.
	 * 
	 * @param keyWord
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String getGoogleFirstPage(String keyWord) throws ClientProtocolException, IOException {
		String googleUrl = "https://www.google.com/search?q=";
		String url = googleUrl + URLEncoder.encode(keyWord, "utf-8");
		return HttpUtils.getHtml(url);
	}

	/**
	 * from google search result, get the non Ad links
	 * 
	 * @param googleContent the search result page content of google.
	 * @return a set that contains the non-Ad links, or null if nothing found
	 */
	public static List<String> getGoogleNonAdLinks(String googleContent) {

		if (StringUtils.isBlank(googleContent)) {
			return null;
		}

		List<String> links = new ArrayList<>();
		Elements divs = Jsoup.parse(googleContent).select("div.srg>div.g");
		if (divs != null && divs.size() > 0) {
			for (Element e : divs) {
				String link = e.select("div > div.rc > div.r > a[href]").attr("href");
				links.add(link);
			}
		}
		if (links.size() > 0) {
			return links;
		}
		return null;
	}

	@Override
	public void run() {
		
		System.out.println(Thread.currentThread().getName() + "started");

		if (StringUtils.isBlank(html)) {
			return;
		}

		// parse and extra the necessary info from html content.
		processor.process(html);

		// check the links in the bottom navigation bars, and start new thread for each
		// page if not been processed yet.
		try {
			spawn();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void spawn() throws ClientProtocolException, IOException, InterruptedException {
		// get the google search result page bottom navigation links, and populate them
		// into the filed navLinks
		populateNavigationLinks();

		for (String url : navLinks) {
			if (!donePages.contains(url) && getPageNum(url) < this.totalPageNum) {

				// delaying thread to reduce server burden
				Thread.sleep(1200);

				exe.submit(new GoogleSpider<String>(HttpUtils.getHtml(url), this.totalPageNum, GoogleSpider.processor,
						GoogleSpider.exe));
				donePages.add(url);
			}
		}
	}

	// 得到当前页面的URL， 可以通过获取 next 页面的URL后推算出来当前页面的URL。
	private String getCurPageUrlAfterUniform() {

		String nextPageUrl = getNavBarNextUrl(html);

		int curNum = getPageNum(nextPageUrl);

		int curNumConvert = (curNum - 1) * 10;

		if (curNumConvert < 0) {
			curNumConvert = 0;
		}

		nextPageUrl = uniformGoogleLink(nextPageUrl);

		String curPageUrl = nextPageUrl.replace("&start=" + curNum * 10, "&start=" + curNumConvert);

		return curPageUrl;
	}

	// get the google search result page bottom navigation links and populate them
	// into navLinks set.
	private void populateNavigationLinks() {
		Elements elements = Jsoup.parse(html).select("table#nav tr td a.fl");
		for (Element e : elements) {
			String link = e.attr("href");
			link = uniformGoogleLink(link);
			navLinks.add(link);
		}
	}

	// normal the href link as :
	// /search?q=led+display&ei=YJrsXJjXKq6m_QaTiYSYDA&start=20&sa=N&ved=0ahUKEwiYh_i0lL3iAhUuU98KHZMEAcMQ8tMDCO0B
	// need to conver it to : https://www.google.com/search?q=led+display&start=20
	// "led display" are search keyword example
	private String uniformGoogleLink(String link) {
		return "https://www.google.com" + link.split("&")[0] + "&start=" + link.split("&start=")[1].split("&")[0];
	}

	// based on the google serach result page link to get the page number indicator
	// on it. Typical url link as bellow:
	// https://www.google.com/search?q=led%E6%98%BE%E7%A4%BA%E5%B1%8F&biw=1745&bih=321&ei=dofsXMX0E-vF_Qbg_YO4CQ&start=50&sa=N&ved=0ahUKEwjFh_Wvgr3iAhXrYt8KHeD-AJc4KBDw0wMIgwE
	private int getPageNum(String pnnextUrl) {
		return (Integer.parseInt(pnnextUrl.split("&start=")[1].split("&")[0])) % 10;
	}

	/**
	 * based on the given google search result html content to get the navigation
	 * bar's "next page" url
	 * 
	 * @param html
	 * @return
	 */
	public static String getNavBarNextUrl(String html) {
		return Jsoup.parse(html).select("table#nav tr td.b.navend a#pnnext").attr("href");
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public int getTotalPageNum() {
		return totalPageNum;
	}

	public void setTotalPageNum(int totalPageNum) {
		this.totalPageNum = totalPageNum;
	}

	public static HtmlProcessor getProcessor() {
		return processor;
	}

	public static void setProcessor(HtmlProcessor processor) {
		GoogleSpider.processor = processor;
	}

	public static List<String> getResults() {
		return results;
	}

	public static void setResults(List<String> results) {
		GoogleSpider.results = results;
	}

	public static ThreadPoolExecutor getExe() {
		return exe;
	}

	public static void setExe(ThreadPoolExecutor exe) {
		GoogleSpider.exe = exe;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

	public Set<String> getNavLinks() {
		return navLinks;
	}

	public void setNavLinks(Set<String> navLinks) {
		this.navLinks = navLinks;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public static Set<String> getDonePages() {
		return donePages;
	}

	public static void setDonePages(Set<String> donePages) {
		GoogleSpider.donePages = donePages;
	}

	public static int getMaxTotalPage() {
		return MAX_TOTAL_PAGE;
	}

	@Override
	public String toString() {
		return "GoogleSpider [" + " totalPageNum=" + totalPageNum + ", previous=" + previous + ", navLinks=" + navLinks
				+ ", next=" + next + "]";
	}

}
