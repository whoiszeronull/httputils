package com.hu.utils.httputils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GoogleSpider<T> implements Runnable {
	private String html; // the google search result webpage html content.
	private int totalPageNum; // the total page number need to be processed from the search result pages.

	private static HtmlProcessor parser;

	public final static int MAX_TOTAL_PAGE = 500;

	private String previous; // the url link for the "previous page" on the bottom of google search result
								// pages
	private List<String> navLinks; // the navigation for pages [1,2,3....]
	private String next; // // the url link for the "next page" on the bottom of google search result
							// pages

	// for multi threading.
	ThreadPoolExecutor exe;

	public GoogleSpider(String html, int totalPageNum, HtmlProcessor parser, ThreadPoolExecutor exe) {
		super();
		this.html = html;

		// if the input total page Num is less than 0 or greater than MAX_TOTAL_PAGE,
		// then set the totalPage Num to be MAX_TOTAL_PAGE.
		if (totalPageNum < 1 || totalPageNum > MAX_TOTAL_PAGE) {
			this.totalPageNum = MAX_TOTAL_PAGE;
		}

		this.totalPageNum = totalPageNum;
		this.parser = parser;
		this.exe = exe;
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
	public static Set<String> getGoogleNonAdLinks(String googleContent) {

		if (StringUtils.isBlank(googleContent)) {
			return null;
		}

		Set<String> links = new HashSet<>();
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

	/**
	 * search the keyword by google and get all the nonAD hosts from page 0-pageNum.
	 * It is using multi threading for each page parshing.
	 * 
	 * @param keyWord the key word to search for thru google.com
	 * @param pageNum the total page number to parse from googl search result.
	 * @return a set that containing all the nonAd hosts from search results of
	 *         page0 to pageNum;
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static <T> Set<T> getGoogleResults(String keyWord, int totalPageNum)
			throws ClientProtocolException, IOException {

		Set<T> hosts = new HashSet<T>();

		String googleFirstPage = getGoogleFirstPage(keyWord);

		if (hosts.size() > 0) {
			return hosts;
		}

		return null;
	}

	@Override
	public void run() {
		if (StringUtils.isBlank(html)) {
			return;
		}

		// get the google search result page bottom navigation links
		getNavigationLinks();

		parser.process(html);
		spawn();
	}

	// get the google search result page bottom navigation links
	private void getNavigationLinks() {
		previous = getNavBarPreUrl(html);
		next = getNavBarNextUrl(html);
		navLinks = getNavLinks(html);
	}

	public static String getNavBarPreUrl(String html) {
		return null;
	}

	public static String getNavBarNextUrl(String html) {
//		System.out.println("GoogleSpider.getNavBarNextUrl()");
//		String cssQuerry = "table#nav tr td.b.navend";
//		System.out.println(Jsoup.parse(html).select(cssQuerry));

		String pnnext = Jsoup.parse(html).select("table#nav tr td.b.navend a#pnnext")
				.attr("href");
		return pnnext;
	}

	public static List<String> getNavLinks(String html) {
		return null;
	}

	private void spawn() {
	}

}
