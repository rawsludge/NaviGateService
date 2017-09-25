package com.naviosa.Startup.Connector;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class WebClient {
    private  final  String _url;
    private final String USER_AGENT = "Mozilla/5.0";
    private final int OK = 200;

    public WebClient(String url) {
        _url = url;
    }

    public String Post(String postData) throws Exception {
        URL url = new URL(_url);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setDoOutput(true);
        if( postData!= null && !postData.isEmpty() ) {
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postData);
            wr.flush();
            wr.close();
        }
        int responseCode = con.getResponseCode();
        if( OK != responseCode) {
            throw  new Exception( String.format("Web site response error. Response: %d", responseCode));
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return  response.toString();
    }
}
