package net.mobilim.NaviGateService.Managers;

import net.mobilim.NaviGateData.Entities.Product;
import net.mobilim.NaviGateService.Helpers.XmlDefinitions;
import net.mobilim.NaviGateService.HttpWebRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.InvalidObjectException;
import java.text.SimpleDateFormat;

@Component
public class CategorySyncManager {
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMddyyyy");
    private final Logger logger = LoggerFactory.getLogger(CategorySyncManager.class);

    @Autowired
    private HttpWebRequest httpWebRequest;

    public CategorySyncManager() {

    }

    public void startSync(Product product) throws Exception {

        String guestData = "";
        String response;
        JSONObject jsonObject;

        for (int index=0; index < product.getMaxOccupancy(); index++) {
            guestData += String.format("<Guest SeqNumber=\"%d\" AgeCode=\"A\"/>", index + 1);
        }
        String sailingDate = dateFormatter.format(product.getSailingDate());
        String xmlPostData = String.format(XmlDefinitions.CATEGORY, product.getSailingID(),
                sailingDate, product.getShip().getCode(), String.format(
                        "<NumberOfGuests>%d</NumberOfGuests>%s", product.getMaxOccupancy(), guestData));
        try {
            response = httpWebRequest.Post(xmlPostData);
            jsonObject = XML.toJSONObject(response.toString()).getJSONObject("CruiseLineResponse");
            if( jsonObject.has("MessageHeader") ) {
                JSONObject tempObject = jsonObject.getJSONObject("MessageHeader");
                if (tempObject.getString("MessageId").equals("CCMSGERR")) {
                    tempObject = tempObject.getJSONObject("Advisory");
                    String advisoryText = tempObject.getString("CruiseLineAdvisoryText");
                    String errorText = tempObject.getString("Text");
                    String errorCode = tempObject.get("Code").toString();
                    logger.error("AdvisoryText: {}, Code: {}, Text: {}.", advisoryText, errorCode, errorText);
                    throw  new Exception(advisoryText);
                }
            }
            Object object = findObjectByPath(jsonObject, "CategoryAvailabilityResponse/Category/");
            if( object instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray)object;
                for (Object item : jsonArray ) {

                    logger.info(item.toString());
                }
            }
        }
        catch (Exception ex) {
            logger.error("", ex);
        }
    }
    private Object findObjectByPath(JSONObject jsonObject, String path) throws Exception{
        String[] objectNames = path.split("/");
        Object returnObject = jsonObject;
        for (String name:objectNames) {
            if(name.isEmpty()) continue;
            if( returnObject instanceof JSONObject ) {
                jsonObject = (JSONObject) returnObject;
                if ( jsonObject.has(name))
                    returnObject = jsonObject.get(name);
                else
                    throw new InvalidObjectException(name);
            }
        }
        return returnObject;
    }
}
