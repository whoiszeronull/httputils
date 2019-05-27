package com.hu.utils.httputils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HttpUtils {

	public static final String CHARSET_UTF8 = "utf-8";
	public static final String CHARSET_DEFAULT = CHARSET_UTF8;

	/**
	 * Based on the given url string, get the content from the url which belong to
	 * content type: text/html, text/plain. And ignor other types.
	 * 
	 * @param url
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String getHtml(String url) throws ClientProtocolException, IOException {

		try (CloseableHttpClient httpclient = HttpClients.createDefault();) {
			HttpGet httpget = new HttpGet(url);
			httpRequestDisguise(httpget);
			ResponseHandler<String> responseHandler = new ResponseHandlerString();
			return httpclient.execute(httpget, responseHandler);
		}
	}

//request headers examples as bellow:(20190527): 
//	:authority:www.google.com
//	:method:GET
//	:path:/search?q=LED+display&ei=AunrXJSXHK62gge37JfgDg&start=10&sa=N&ved=0ahUKEwiUoMeh67viAhUum-AKHTf2BewQ8tMDCNoC&biw=1745&bih=536
//	:scheme:https
//	accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8
//	accept-encoding:gzip, deflate, br
//	accept-language:zh-CN,zh;q=0.9
//	cookie:CGIC=IlV0ZXh0L2h0bWwsYXBwbGljYXRpb24veGh0bWwreG1sLGFwcGxpY2F0aW9uL3htbDtxPTAuOSxpbWFnZS93ZWJwLGltYWdlL2FwbmcsKi8qO3E9MC44; CONSENT=YES+HK.zh-CN+; HSID=A9bTO2JrUSEYujboZ; SSID=AxArkapH1-3oIflUB; APISID=uZ2lNHtGYZT-aaMj/A_ey4-YiRoC-A8wjy; SAPISID=bcKxwfikEpVAdf01/ARTHiEosJ4D6BrdJB; SID=Uwd4TZFe98NCtG4g572F2mCdJR3D_g7dF57jpS1bYhfmUHSa58bhoelbAuK0dfuJ2-da0A.; SIDCC=AN0-TYsTA6Fx-T_j7i_9TgsrFqheNNkSRItA3MCNnJEfUcWTgH1riTLKh_RJE8_Heapl8N34GjQ; ANID=AHWqTUnusBmQvSx6HrJRuSEnjnj_4NL8w7XNCtlPURAJPXVU5QFK-R4ECvPx18sT; NID=184=anFEaf4SMDYXk9e_YJd9fjydriQM9DeFuEUYZM1PalQ_7yk07sZuASaPNeXisn__MQCkOUzql033wkBvWi2zR5J7N2N5Ck8TlnV0TS0i9X4KUid7eCq3ryZuLnXP3yJ4ANaS1UE0NiZq4gMo7QVLbIhb6zPZMZAvYpSjiT7YFsabdJKhKdPsowboDyWSMiWGBV-cqjkndzcx6Nnu9dbr5NqzommSRQXKcyKjMs_Dq_vqf0Sj; DV=w4Hqv3Q6vcot8D1GDcTKiA9zWmOYr1Z_Q3x9eM9cFwIAAAA; 1P_JAR=2019-05-27-13; UULE=a+cm9sZToxIHByb2R1Y2VyOjEyIHByb3ZlbmFuY2U6NiB0aW1lc3RhbXA6MTU1ODk2NDQ4NDEyMzAwMCBsYXRsbmd7bGF0aXR1ZGVfZTc6NDA3NjQ0MzUyIGxvbmdpdHVkZV9lNzotNzM5ODAwODM4fSByYWRpdXM6NTI3MDA=
//	referer:https://www.google.com/
//	upgrade-insecure-requests:1
//	user-agent:Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE

	// to disguise the request , reduce the chance for server to block the requests.
	private static void httpRequestDisguise(HttpGet httpget) {

		// 增加个 user-agent, 对于使用google 有帮助
		httpget.addHeader("user-agent",
				"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE");
	}

	/**
	 * Based on the domain given by the proper url, extra all the non-repeated links
	 * belong to this domain, and remove other links.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static Set<String> getAllDomainLinks(String urlStr) throws ClientProtocolException, IOException {

		// form a url based on the given urlStr, to ensure the urlStr is proper given
		// with protocol and host name.
		URL url = new URL(urlStr);

		// a treeset to contain non-repeated links and stored in order
		TreeSet<String> links = new TreeSet<>();

		// form a basedurl string from the given urlStrl, such as
		String baseUrl = url.getProtocol() + "://" + url.getHost();

		List<String> allLinks = HttpUtils.getAllLinksFromUrl(urlStr);

		for (String link : allLinks) {
			if (StringUtils.isNotEmpty(link)) {
				// if the link start with "/", then complete the link with baseUrl as a complete
				// proper form link;
				if (link.startsWith("/")) {
					links.add(baseUrl + link);
				}
				if (link.startsWith(baseUrl)) {
					links.add(link);
				}
			}
		}
		return links;
	}

	/**
	 * From the given host, filter all the links out from the html string which
	 * belong to the host.
	 * 
	 * @param host the given host name or the link that belongs to the host
	 * @param html the html content to filter
	 * @return a set that contains the links which belong to the host within
	 *         complete correctly formed url strings.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Set<String> filterDomainLinks(String host, String content)
			throws ClientProtocolException, IOException {

		// form a url based on the given urlStr, to ensure the urlStr is proper given
		// with protocol and host name.
		URL url = new URL(host);

		// a treeset to contain non-repeated links and stored in order
		TreeSet<String> links = new TreeSet<>();

		// form a basedurl string from the given urlStrl, such as
		String baseUrl = url.getProtocol() + "://" + url.getHost();

		List<String> allLinks = HttpUtils.getAllLinksFromHtml(content);

		for (String link : allLinks) {
			if (StringUtils.isNotEmpty(link)) {
				// if the link start with "/", then complete the link with baseUrl as a complete
				// proper form link;
				if (link.startsWith("/")) {
					links.add(baseUrl + link);
				}
				if (link.startsWith(baseUrl)) {
					links.add(link);
				}
			}
		}
		return links;
	}

	// get a list that contains all the hrefs contained in the given url;
	public static List<String> getAllLinksFromUrl(String url) throws ClientProtocolException, IOException {
		ArrayList<String> links = new ArrayList<String>();
		String html = HttpUtils.getHtml(url);
		if (StringUtils.isNotEmpty(html)) {
			Document doc = Jsoup.parse(html);
			Elements linksElements = doc.select("a[href]");
			for (Element e : linksElements) {
				// 读取href属性值的内容
				links.add(e.attr("href"));

				// 不能用这个。 这个是读取href的属性值，但是属性值必须是绝对路径值，如果不是的话就会返回空字符串，比如有的href值 为 "/index.htm"
				// 的话，那么这个不会给读取。
				// links.add(e.attr("abs:href"));
			}
		}
		return links;
	}

	// get a list that contains all the hrefs contained in the given url;
	public static List<String> getAllLinksFromHtml(String html) throws ClientProtocolException, IOException {
		ArrayList<String> links = new ArrayList<String>();
		if (StringUtils.isNotEmpty(html)) {
			Document doc = Jsoup.parse(html);
			Elements linksElements = doc.select("a[href]");
			for (Element e : linksElements) {
				// 读取href属性值的内容
				links.add(e.attr("href"));
			}
		}
		return links;
	}

	// hadler the HttpEntity only convert the content belongs to "text/html" or
	// "text/plain" with "utf-8" encoding which set in HttpUtils.CHARSET_DEFAULT =
	// "utf-8".
	private static class ResponseHandlerString implements ResponseHandler<String> {
		@Override
		public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					ContentType contentType = ContentType.get(entity);
					if (contentType != null) {
						String mimeType = contentType.getMimeType();
						// only translate the contentype is the text_html or text_plain
						if ("text/html".equalsIgnoreCase(mimeType) || "text/plain".equalsIgnoreCase(mimeType)) {
							Charset charset = contentType.getCharset();
							if (charset != null) {
								return new String(EntityUtils.toString(entity, charset).getBytes(),
										HttpUtils.CHARSET_DEFAULT);
							} else {
								return EntityUtils.toString(entity, "utf-8");
							}
						}
					}
				}
				return null;
			} else {
				throw new ClientProtocolException("Unexpected response status: " + status);
			}
		}

	}

	// if there is contentEncoding in the header, then return the string by using
	// the contentEncoding, otherwise get the charset from contentType, if no
	// charset in contentType
	// then return the string by using given charsetName setting, such as "utf-8"
	private static String getEntityString(HttpEntity entity, String charsetName) throws ParseException, IOException {

		ContentType contentType = ContentType.get(entity);
		if (contentType != null) {
			// only translate the contentype is the text_html or text_plain
			if (contentType.equals(ContentType.TEXT_HTML) || contentType.equals(ContentType.TEXT_PLAIN)) {
				Charset charset = contentType.getCharset();
				if (charset != null) {
					return new String(EntityUtils.toString(entity, charset).getBytes(), charsetName);
				}
			}
		}

		/*
		 * Header contentEncoding = entity.getContentEncoding(); if (contentEncoding !=
		 * null) { return new String(EntityUtils.toString(entity).getBytes(),
		 * charsetName); }
		 */
		return null;
	}

	/**
	 * Get the first page of search result by using www.baidu.com with givien key
	 * word
	 * 
	 * @param keyWord the key word to search
	 * @return the first page content in html format.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String getBaiduFirstPage(String keyWord) throws ClientProtocolException, IOException {
		String baiduBaseUrl = "https://www.baidu.com/s?wd=";
		String url = baiduBaseUrl + URLEncoder.encode(keyWord, "utf-8");
		return getHtml(url);
	}

	/**
	 * Form baidu searched result webpage, filter out the advertisement links and
	 * keep the other result links
	 * 
	 * @param baiduPage the search result webpage content from baidu searching.
	 * @return a list containing the non-ad links and null if nothing found.
	 */
	public static Set<String> getBaiduNonAdEncryptedLinks(String baiduPage) {
		Set<String> list = new HashSet<>();
		Elements divs = Jsoup.parse(baiduPage).select("div.result.c-container");
		if (divs != null && divs.size() > 0) {
			for (Element e : divs) {
				String link = e.select("h3>a").attr("href");
				list.add(link);
			}
		}
		if (list.size() > 0) {
			return list;
		}
		return null;
	}

	/**
	 * based on the given encrypted links (for example extract from baidu search
	 * webpage) to get the original actual links from server.
	 * 
	 * @param encryptedLink the encrypted link which will be redirected when
	 *                      accessing. For example the links from baidu search
	 *                      result websites links..
	 * @return the original decrpted website link.
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws URISyntaxException
	 */
	public static String getDecryptedLink(String encryptedLink)
			throws ClientProtocolException, IOException, URISyntaxException {
		String redir;
		HttpClientContext context = HttpClientContext.create();
		HttpGet httpget = new HttpGet(encryptedLink);

		try (CloseableHttpClient httpclient = HttpClients.createDefault();
				CloseableHttpResponse response = httpclient.execute(httpget, context);) {
			HttpHost target = context.getTargetHost();
			List<URI> redirectLocations = context.getRedirectLocations();
			URI location = URIUtils.resolve(httpget.getURI(), target, redirectLocations);
			redir = location.toASCIIString();
			// Expected to be an absolute URI
		}
		return redir;
	}

	/**
	 * 
	 * @param keyWord
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static Set<String> getBaiduSearchResultActualLinks(String keyWord)
			throws ClientProtocolException, IOException, URISyntaxException {
		Set<String> links = new HashSet<String>();
		Set<String> encryptedLinks = HttpUtils.getBaiduNonAdEncryptedLinks(HttpUtils.getBaiduFirstPage(keyWord));
		for (String link : encryptedLinks) {
			String decryptedLink = HttpUtils.getDecryptedLink(link);
			links.add(decryptedLink);
		}

		if (links.size() > 0) {
			return links;
		}

		return null;
	}

	
}
