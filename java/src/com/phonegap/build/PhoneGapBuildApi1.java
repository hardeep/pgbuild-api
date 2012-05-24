package com.phonegap.build;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class PhoneGapBuildApi1 extends PhoneGapBuildApi 
{
    protected String api_version = "api/v1";
    
    public static void main(String[] args) throws Exception {
    	//PhoneGapBuildApi1 api = new PhoneGapBuildApi1();
    	// set the required parameters
    	//api.auth_token = "someAuthToken";
    	
    	//System.out.println(api.getKeys());
    	
    	//System.out.println(api.getKeys("ios"));
    	
    	//System.out.println(api.getApps());
    	
    	//System.out.println(api.getApp(13958));
    	
    	//System.out.println(api.getApp(13958));
    	
    	/*System.out.println(api.createAppFromZip(
    			true, "magical app", "com.example.www", "1.0.0",
    			"Magical Unicorns", 
    			true, null, null, null,
    			"/Users/hardeep/Desktop/www.zip",
    			null));*/
    }
    
    public PhoneGapBuildApi1() {
    }
    
    /**
     * @brief   Gets all the keys from the build server
     * @return  JSONObject : Contains information on all the keys
     * @throws 	Exception 
     */
    public JSONObject getKeys() throws Exception {
    	Url url = apiUrl("keys");
    	url.addQueryString("auth_token", this.auth_token);
        HttpGet getKeys = new HttpGet(url.toString());
        return new JSONObject(getJson(getKeys));
    }
    
    /**
     * @brief   Get keys for a specific platform
     * @param   platform : Which platform to query keys for
     * @return  JSONObject : contains information relevant to a
     *              specific platform's keys
     * @throws  JSONException
     */
    public JSONObject getKeys(String platform) throws Exception {
    	Url url = apiUrl("keys/" + platform);
    	url.addQueryString("auth_token", this.auth_token);
        HttpGet getKeys = new HttpGet(url.toString());
        return new JSONObject(getJson(getKeys));
    }
    
    /**
     * @brief   Get information regarding all user applications
     * @return  JSONObject : Returns JSON data regarding information
     *              about all apps
     * @throws  JSONException
     */
    public JSONObject getApps() throws Exception {
    	Url url = apiUrl("apps");
    	url.addQueryString("auth_token", this.auth_token);
        HttpGet getKeys = new HttpGet(url.toString());
        return new JSONObject(getJson(getKeys));

    }
    
    /**
     * @brief   Get information regarding a specific application id
     * @param   id : Get information regarding a specific application
     * @return  JSONObject : returns JSON data regarding information
     *              on a specific platform
     * @throws  JSONException
     */
    public JSONObject getApp(int id) throws Exception {
    	Url url = apiUrl("apps/" + new Integer(id).toString());
    	url.addQueryString("auth_token", this.auth_token);
        HttpGet getKeys = new HttpGet(url.toString());
        return new JSONObject(getJson(getKeys));
    }
    
    /** 
     * @brief   Create a new application on build.phonegap.com
     * @param	create : true to create a new app false to update
     * @param   title : The title of the application
     * @param   package_name : The package identifier
     *              ex: com.phonegap.build
     * @param   version : Current version of the application ex: 1.0.0
     * @param   description : A description regarding the application
     * @param   debug : Whether or not to enable debugging (weinre)
     * @param   ios_key : Id of which key to use for signing this
     *              application
     * @param   android_key : Id of which key to use for signing
     *              this application
     * @param   blackberry_key : Id of which key to use for signing
     *               this application
     * @param 	zip_path : path to zip file
     * @param	app_id : used only when updateing an application otherwise set
     * 				it to null
     * @throws Exception 
     */
    
    public String createAppFromZip(
        boolean create, String title, String package_name,
        String version, String description, Boolean debug,
        Integer ios_key, Integer android_key, Integer blackberry_key,
        String zip_path, Integer app_id
    )   throws Exception {
    	// yeah this is an ugly method but take it for now -_-
    	
        title = (title.isEmpty()) ? this.default_title : title;

        package_name = (package_name.isEmpty()) ? 
            this.default_package_name : package_name;

        version = (version.isEmpty()) ?
            this.default_version : version;

        description = (description.isEmpty()) ?
            this.default_description : description;

        debug = (isNull(debug)) ?
            this.default_debug : debug;

        ios_key = (isNull(ios_key)) ?
            this.default_ios_key : ios_key;

        android_key = (isNull(android_key)) ?
            this.default_android_key : android_key;

        blackberry_key = (isNull(blackberry_key)) ?
            this.default_blackberry_key : blackberry_key;

        String create_method = "file";
    
        HashMap json_data = new HashMap();
        json_data.put("title", title);
        json_data.put("create_method", create_method);
        json_data.put("package", package_name);
        json_data.put("version", version);
        json_data.put("description", description);
        json_data.put("debug", debug);
        
        if ( !isNull(ios_key) 
                || !isNull(android_key) 
                || !isNull(blackberry_key)
        ) {
            HashMap keys = new HashMap();
            if (isNull(ios_key)) keys.put("ios", ios_key); 
            if (isNull(android_key)) keys.put("android", android_key); 
            if (isNull(blackberry_key)) keys.put(
                "blackberry", blackberry_key
                ); 
            json_data.put("keys", keys);
        }
        
        // This way did not seem to be recursive? so use the helper
        // method instead
        // JSONObject json = new JSONObject(json_data);
        String json = buildJson(json_data);
        
        MultipartEntity post_entity = new MultipartEntity(
            HttpMultipartMode.BROWSER_COMPATIBLE
            );
        post_entity.addPart("data", new StringBody(json) );
        post_entity.addPart("file", new FileBody(
            new File(zip_path))
            );
        
        // create a new app or update an existing one
        if (create)
        {
        	Url url = apiUrl("apps");
        	url.addQueryString("auth_token", this.auth_token);
            HttpPost post = new HttpPost(url.toString());
            
            post.setEntity(post_entity);
            HttpResponse response = this.http_client.execute(post);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            return result;

        } else {
        	Url url = apiUrl("apps/" + app_id.toString());
        	url.addQueryString("auth_token", this.auth_token);
            HttpPost post = new HttpPost(url.toString());
            
            post.setEntity(post_entity);
            HttpResponse response = this.http_client.execute(post);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            return result;
        }
    }
}
