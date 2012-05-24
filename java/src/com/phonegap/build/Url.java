package com.phonegap.build;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Url {
	HashMap<String, String> queryStrings;
	String host = "";
	String port = "";
	String protocol = "http";
	String username = "";
	String password = "";
	String path = "";
	
	public Url() {
		this.queryStrings = new HashMap<String, String>();
	}
	
	public void addQueryString(String key, String value) {
		this.queryStrings.put(key, value);
	}
	
	public String toString() {
		String credentials;
		try {
			credentials = (!this.username.isEmpty() && !this.password.isEmpty()) ?
					URLEncoder.encode(this.username, "UTF-8") + ':' +
						URLEncoder.encode(this.password, "UTF-8") + "@":
					"";
		} catch (UnsupportedEncodingException e) {
			return "";
		}
			
		// generate the query string from the HashMap
		
		// if we don't have any query strings then we dont need the ? in the
		// address
		String query = (this.queryStrings.size() > 0) ? "?" : "";
		
		// iterate and append
		Iterator<Entry<String, String>> it =
					this.queryStrings.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, String> i = (Map.Entry<String, String>)it.next();
			query += "&" + i.getKey() + "=" + i.getValue();
		}
		
		// end query string generation
		
		String port = (!this.port.isEmpty()) ? ":" + this.port : "";
		
		String url = this.protocol + "://" + credentials + this.host + port 
				+ "/" + this.path +  query;
		
		return url;
	}
	
	public static void main(String[] args) {
		Url url = new Url();
		url.protocol = "https";
		url.host = "build.phonegap.com";
		url.username = "hardeep.shoker@nitobi.com";
		url.password = "password123";
		url.path = "api/v1/apps";
		url.addQueryString("auth_token", "");
		System.out.println(url);
	}
}
