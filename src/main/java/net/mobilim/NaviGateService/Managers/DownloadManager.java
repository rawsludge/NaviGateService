package net.mobilim.NaviGateService.Managers;

import com.jcabi.aspects.Loggable;
import net.mobilim.NaviGateService.Exceptions.AdvisoryException;
import net.mobilim.NaviGateService.HttpWebRequest;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;

@Component
public class DownloadManager {
    //private final Logger logger = LoggerFactory.getLogger(ProductSyncManager.class);


    @Autowired
    private HttpWebRequest httpWebRequest;

    @Loggable(Loggable.DEBUG)
    public JSONObject download(String postData) throws Exception {
        String response;
        JSONObject jsonObject;

        response = httpWebRequest.Post(postData);
        jsonObject = XML.toJSONObject(response.toString()).getJSONObject("CruiseLineResponse");
        if( jsonObject.has("MessageHeader") ) {
            JSONObject tempObject = jsonObject.getJSONObject("MessageHeader");
            if( tempObject.getString("MessageId").equals("CCMSGERR") ) {
                tempObject = tempObject.getJSONObject("Advisory");

                String errorText = tempObject.getString("Text");
                String errorCode = tempObject.get("Code").toString();

                if( tempObject.has("CruiseLineAdvisoryText")) {
                    String advisoryText = tempObject.getString("CruiseLineAdvisoryText");
                    throw new InvalidApplicationException(String.format("Advice: %s, Code: %s, error: %s", advisoryText, errorCode, errorText));
                } else  {
                    throw new AdvisoryException(errorCode, errorText);
                }
            }
        }
        return jsonObject;
    }
}
