package net.fajarachmad.prayer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpRequestUtil {
	
	public static String GET(String url) throws ClientProtocolException, IOException{
		String qResult = null;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();

    	if (httpEntity != null){
    		InputStream inputStream = httpEntity.getContent();
    	    Reader in = new InputStreamReader(inputStream);
    	    BufferedReader bufferedreader = new BufferedReader(in);
    	    StringBuilder stringBuilder = new StringBuilder();

    	    String stringReadLine = null;

    	    while ((stringReadLine = bufferedreader.readLine()) != null) {
    	    	stringBuilder.append(stringReadLine + "\n");	
    	    }

    	    qResult = stringBuilder.toString();   
    	}	
		return qResult;
    }
}
