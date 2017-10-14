package net.mobilim.NaviGateService;


import com.jcabi.aspects.Loggable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private Logger logger = LogManager.getLogger(HttpWebRequest.class);

    public HttpWebRequest(@Value("${website.url}") String url) {
        _url = url;
    }

    @Loggable(Loggable.DEBUG)
    public String Post(String postData) throws Exception {
        logger.info("Begin post");
        URL url = new URL(_url);
        logger.debug("Url is {}", _url);
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
        httpsURLConnection.setDoOutput(true);
        if( postData!= null && !postData.isEmpty() ) {
            DataOutputStream dataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
            dataOutputStream.writeBytes(postData);
            dataOutputStream.flush();
            dataOutputStream.close();
        }
        int responseCode = httpsURLConnection.getResponseCode();
        logger.debug("Http request response code is {}", responseCode);
        if( OK != responseCode) {
            throw  new Exception(String.format("Web site response error. Response: %d", responseCode));
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        logger.info("End post");
        return  response.toString();
    }
}
