package alexainterface;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HTTPRequests {
	
	public static final String USER_AGENT = "Mozilla/5.0";
	
	public static void sendGetRequest(String url) throws Exception {
		URL u = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) u.openConnection();
		con.setRequestMethod("GET");							//optional default is GET
		con.setRequestProperty("User-Agent", USER_AGENT);		//add request header
		
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println(con.getResponseMessage());
	}

}
