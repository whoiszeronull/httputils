package com.hu.utils.httputils.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hu.utils.httputils.HttpUtils;

public class HttpUtilsTest {

	private HttpUtils hu = new HttpUtils();

//	String url = "http://www.desayopto.cn";
//	String url = "http://www.mrled.cn/en/index.html";
	String url = "http://www.absen.cn/";
	
	@Test
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
	
	

}
