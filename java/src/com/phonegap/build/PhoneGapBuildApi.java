package com.phonegap.build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class PhoneGapBuildApi {

    protected String auth_token = null;
    
    protected DefaultHttpClient http_client;
    
    protected String host = "build.phonegap.com";
    protected int port = 443;
    protected String api_version = "v1";
    
    final String default_title = "PGBuild Project";
    final String default_package_name = "com.example.app";
    final String default_version = "1.0.0";
    final String default_description = "PhoneGap Application";
    final Boolean default_debug = Boolean.FALSE;
    final Integer default_ios_key = null;
    final Integer default_android_key = null;
    final Integer default_blackberry_key = null;
    
    public static void main(String[] args) throws Exception {
    	//PhoneGapBuildApi api = new PhoneGapBuildApi();
    	// set the required parameters
    	//api.auth_token = "someAuthToken";
    	
    	// authenticate a user using basic auth
    	// this will store an internal auth token that can be saved and
    	// re-used
    	//api.authenticate("test@email.com", "password");
    	
    	// validating your authentication token
    	//System.out.println(api.isAuthenticated());
    	
    }
    
    public PhoneGapBuildApi() {
        this.http_client = new DefaultHttpClient();
    }

    protected Url apiUrl()
    		throws UnsupportedEncodingException {
    	
        return this.apiUrl("");
    }

    protected Url apiUrl(String path)
    		throws UnsupportedEncodingException {
    	
        Url url = new Url();
        url.host = this.host;
        url.path =  "api/" + this.api_version + "/" + path;
        url.protocol = "https";
        
        return url;
    }
    
    public boolean isAuthenticated()
        throws Exception {
    	
    	Url url = this.apiUrl();
    	url.addQueryString("auth_token", this.auth_token);

        HttpGet get = new HttpGet(url.toString());

        HttpResponse response = this.http_client.execute( get );
        StatusLine status = response.getStatusLine();
        
        EntityUtils.consume(response.getEntity());
        
        if (status.getStatusCode() == 200) {
        	return true;
        } else {
        	return false;
        }
    }

    public void authenticate(final String username, final String password)
    		throws Exception {
    	
        UsernamePasswordCredentials creds =
        			new UsernamePasswordCredentials(username, password);
        
        this.http_client.getCredentialsProvider().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), 
                creds
            );
        
        Url url = this.apiUrl();
        url.path = "token";
        
        HttpPost post = new HttpPost(url.toString());
        HttpResponse status = this.http_client.execute( post );
        HttpEntity entity = status.getEntity();
        String json = EntityUtils.toString(entity);
        EntityUtils.consume(entity);
        
        System.out.println(json.toString());
        
        JSONObject jsonObject = new JSONObject(json);
        
        if (jsonObject.has("token")) {
        	this.auth_token = jsonObject.get("token").toString();
        } else if (jsonObject.has("error")) {
        	throw new AuthenticationException((String) jsonObject.get("error"));
        } else {
        	throw new Exception("failed to authenticate");
        }
    }
    
    public String getAuthToken() {
    	return this.auth_token;
    }
    
    protected String getJson(HttpGet get) throws Exception {
        HttpResponse response = this.http_client.execute( get );
        String json = EntityUtils.toString(response.getEntity());
        return json;
    }

    protected boolean isNull(Object object) {
        return (object == null)?true:false;
    }
    
    public String buildJson(HashMap json_data) {
        String current_string = "";
        
        Set set = json_data.entrySet();
        Iterator i = set.iterator();
        String comma = ",";
        
        while(i.hasNext())
        {
            Map.Entry entry = (Map.Entry)i.next(); 
            if (entry.getValue() instanceof HashMap)
            {
                current_string += "\""+ entry.getKey() +"\":" + buildJson((HashMap)entry.getValue());
                current_string += (i.hasNext())?",":"";
            }
            else
            {
                current_string += "\""+ entry.getKey() + "\": " + returnCorrectType(entry.getValue());
                current_string += (i.hasNext())?",":"";
            }
        }
        
        return "{"+current_string+"}";
    }
    
    protected Object returnCorrectType(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof Boolean)
        {
            return Boolean.toString(((Boolean) obj).booleanValue());
        } 
        else if (obj instanceof Integer)
        {
            return Integer.toString(((Integer)obj).intValue());
        }
        else
        {
            return "\""+obj.toString()+"\"";
        }
    }
}
