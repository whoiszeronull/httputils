package com.hu.utils.httputils.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hu.utils.httputils.HttpUtils;

public class HttpUtilsTest {

	private HttpUtils hu = new HttpUtils();

//	Number n;
//	Integer i;
//	ArrayList al;
//	HashMap m;
//	StringCoding sc;
//	String url = "http://www.desayopto.cn";
//	String url = "http://www.mrled.cn/en/index.html";
	String url = "http://www.absen.cn/";

//	@Test
	public void testGetAllDomainLinks() throws ClientProtocolException, IOException {

		Set<String> allLinks = HttpUtils.getAllDomainLinks(url);
		for (String string : allLinks) {
			System.out.println(string);
		}

		System.out.println("===========================");
		Set<String> links = hu.getAllDomainLinks(url);
		System.out.println("total size: " + links.size());
		for (String link : links) {
			System.out.println(link);
		}

	}

//	 @Test
	public void testGetHtml() throws IOException, Exception {
		String html = HttpUtils.getHtml(url);
		System.out.println(html);
	}

//	@Test
	public void testGetAllHrefs() throws ClientProtocolException, IOException {

		System.out.println("Getting links from url: " + url);

		Set<String> links = HttpUtils.getAllDomainLinks(url);
		System.out.println("total links: " + links.size());
		for (String link : links) {
			System.out.println(link);

			try {
				System.out.println(new URL(link));
			} catch (Exception e) {
				System.out.println(e.getMessage());
				continue;
			}

		}
	}

//	@Test
	public void testURL() throws MalformedURLException {
		URL url2 = new URL(url);
		System.out.println(url2.toString());
		System.out.println(url2.toExternalForm());

		System.out.println(url2.getProtocol());
		System.out.println(url2.getHost());
		System.out.println(url2.getPort());
		System.out.println(url2.getFile());

	}

//	@Test
	public void getBaiduNonAdLinksTest() throws IOException {
		String content = FileUtils.readFileToString(new File("D:\\temp\\LED_百度搜索.html"), "utf-8");
		Set<String> links = HttpUtils.getBaiduNonAdEncryptedLinks(content);
		for (String link : links) {
			System.out.println(link);
		}
	}

//	http://www.baidu.com/link?url=CM4X6aA6zKagUu3ghQk8Op0IXbt2PG5LU-JLyJkKpJrfTxh_ec1iNLHKjCLQBYl2tXrw42NmjCI0TUiWUCbgmM1PNlUGlRbUGA8_XyixoKC
//		http://www.baidu.com/link?url=uKWn4YkwjaPoMgXNxH48tjtIn30nKZ17n8iaI9IM9QNCwHixhJ_Nmlc-HIabpNgt
//		http://www.baidu.com/link?url=ugM9FXaAbtYnW3NqqXeQVYUM9hQwVyyqxrK0vw0oyX4zSgNJkyW7nwzshAeK_G45
//		http://www.baidu.com/link?url=COZSJiNh84eMhYF18bZ_mSpzjDY4p0qmoCwsKnE5NP1jProwFPs9E9Rop57tSsoW
//		http://www.baidu.com/link?url=UJdo44BwMKdYq_olHV8_8jc47VXzyCR5irz6ebTYrUi
//		http://www.baidu.com/link?url=g2auXQj2esKFQwHtLUtreKV4gvVgmj3zPgiVH0tiUBu

//	@Test
	public void getBaiduDecryptedLinkTest() throws ClientProtocolException, IOException, URISyntaxException {

//		String link = "http://www.baidu.com/link?url=CM4X6aA6zKagUu3ghQk8Op0IXbt2PG5LU-JLyJkKpJrfTxh_ec1iNLHKjCLQBYl2tXrw42NmjCI0TUiWUCbgmM1PNlUGlRbUGA8_XyixoKC";
		String link = "http://www.baidu.com/link?url=uKWn4YkwjaPoMgXNxH48tjtIn30nKZ17n8iaI9IM9QNCwHixhJ_Nmlc-HIabpNgt";
//		String link = "http://www.baidu.com/link?url=UJdo44BwMKdYq_olHV8_8jc47VXzyCR5irz6ebTYrUi";

		String finalRedirec = HttpUtils.getDecryptedLink(link);
		System.out.println("final redirected links: " + finalRedirec); // result is : https://www.cnledw.com/
	}

	@Test
	public void getBaiduSearchResultActualLinks() throws ClientProtocolException, IOException, URISyntaxException {
		Set<String> links = HttpUtils.getBaiduSearchResultActualLinks("LED");
		
		System.out.println(links.size());
		
		for (String string : links) {
			System.out.println(string);
		}
	}
}
