package net.mobilim.NaviGateService;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

@Component
public class HttpWebRequest {
    private  final  String _url;
    private final String USER_AGENT = "Mozilla/5.0";
    private final int OK = 200;
    private Logger logger = LoggerFactory.getLogger(HttpWebRequest.class);

    public HttpWebRequest(@Value("${website.url}") String url) {
        _url = url;
    }

    public String Post(String postData) throws Exception {
        logger.info("Begin post");
        URL url = new URL(_url);
        logger.debug("Url is {}", _url);
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
        logger.debug("Http request response code is {}", responseCode);
        if( OK != responseCode) {
            throw  new Exception(String.format("Web site response error. Response: %d", responseCode));
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
