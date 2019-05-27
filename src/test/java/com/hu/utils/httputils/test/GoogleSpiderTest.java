package com.hu.utils.httputils.test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hu.utils.httputils.GoogleSpider;

public class GoogleSpiderTest {
	
	
//	@Test
	public void getGoogleFirstPageTest() throws ClientProtocolException, IOException {
		String googleFirstPage = GoogleSpider.getGoogleFirstPage("led display");
		FileUtils.writeStringToFile(new File("D:\\tesgoogle.html"), googleFirstPage, "utf-8");
	}
	
//	@Test
	public void getGoogleNonAdLinks() throws IOException {
		String googleFirstPage = GoogleSpider.getGoogleFirstPage("led显示屏");
		Set<String> links = GoogleSpider.getGoogleNonAdLinks(googleFirstPage);
		for (String link : links) {
			System.out.println(link);
		}
	}
	
	@Test
	public void getNavBarNextUrlTest() throws ClientProtocolException, IOException {
		String pnnext = GoogleSpider.getNavBarNextUrl(GoogleSpider.getGoogleFirstPage("led display"));
		System.out.println(pnnext);
	}

}
