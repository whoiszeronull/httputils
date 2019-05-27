package com.hu.utils.httputils;

/**
 * this is a interface need to be implemented for dealing with the webpage 
 * @author shunn
 *
 */
public interface HtmlProcessor {
	
	/**
	 * process the given html content. Such as extracting the data from html content and saving them inito data base.
	 * @param htmlContent
	 */
	public void process(String htmlContent);

}
