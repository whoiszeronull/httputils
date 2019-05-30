package com.hu.utils.httputils.test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;

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
		List<String> links = GoogleSpider.getGoogleNonAdLinks(googleFirstPage);
		for (String link : links) {
			System.out.println(link);
		}
	}

//	@Test
	public void runTest() throws ClientProtocolException, IOException {
		GoogleSpider<String> googleSpider = new GoogleSpider("led", 50);
		googleSpider.run();

//		String html = HttpUtils.getHtml("https://www.google.com/search?q=led+%E6%98%BE%E7%A4%BA%E5%B1%8F&start=60");
//		System.out.println(html);
	}

	public static void main(String[] args) throws ClientProtocolException, IOException, InterruptedException {

		String keyword = "LED dancing floor";
		System.out.println("GoogleSpiderTest.main() search for keyword: " + keyword);
		GoogleSpider<String> spider = new GoogleSpider<String>(keyword, 80);
		new Thread(spider).start();

		System.out.println("spiders started, main thread sleeps for 5 seconds..");
		Thread.sleep(10000);

		ThreadPoolExecutor exe = spider.getExe();

		System.out.println("current total active spider threads count: " + exe.getActiveCount());

		StringBuilder sb = new StringBuilder();
		while (true) {
			if (exe.getActiveCount() == 0) {
				Thread.sleep(3500);
				System.out.println("Google search for keyword: " + keyword);
				System.out.println("all threads now finished jobs=======================");
				System.out.println(GoogleSpider.getResults().size());
				for (String host : GoogleSpider.getResults()) {
					sb.append(host + System.lineSeparator());
				}
				break;
			}
		}

		try {

			FileUtils.writeStringToFile( new File("D:\\Google search results\\Google-" + keyword + ".txt"), sb.toString(), "utf-8", false);
		} catch (Exception e) {
			FileUtils.writeStringToFile(new File("D:\\Google search results\\Google-" + new SimpleDateFormat("yyMMdd-HHmmss") + ".txt"),
					sb.toString(), "utf-8", false);
		}

	}
}
