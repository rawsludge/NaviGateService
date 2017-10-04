package net.mobilim.NaviGateService.Managers;


import net.mobilim.NaviGateData.Entities.Destination;
import net.mobilim.NaviGateData.Entities.Port;
import net.mobilim.NaviGateData.Entities.Product;
import net.mobilim.NaviGateData.Entities.Ship;
import net.mobilim.NaviGateData.Repositories.DestinationRepository;
import net.mobilim.NaviGateData.Repositories.PortRepository;
import net.mobilim.NaviGateData.Repositories.ProductRepository;
import net.mobilim.NaviGateData.Repositories.ShipRepository;
import net.mobilim.NaviGateService.Helpers.XmlDefinitions;
import net.mobilim.NaviGateService.HttpWebRequest;
import net.mobilim.NaviGateService.ServiceApp;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProductSyncManager {
    private ProductRepository productRepository = null;
    private PortRepository portRepository = null;
    private ShipRepository shipRepository = null;
    private DestinationRepository destinationRepository = null;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMddyyyy");
    private final Logger logger = LogManager.getLogger(ServiceApp.class);

    @Value("${website.url}")
    private String websiteUrl;

    public ProductSyncManager(ProductRepository productRepository, PortRepository portRepository, ShipRepository shipRepository,DestinationRepository destinationRepository) {
        this.productRepository = productRepository;
        this.portRepository = portRepository;
        this.shipRepository = shipRepository;
        this.destinationRepository = destinationRepository;
    }

    public void startSync() throws Exception {
        String response = "";
        JSONObject jsonObject = null;
        JSONArray jsonArray = null;
        Date date = new Date();
        String fromDate = dateFormatter.format(date);

        String xmlPostData = String.format( XmlDefinitions.PRODUCT, fromDate, "", "5", "60", "", "");

        HttpWebRequest webRequest = HttpWebRequest.Create(websiteUrl);

        try {
            response = webRequest.Post(xmlPostData);
            jsonObject = XML.toJSONObject(response.toString());
            jsonArray = jsonObject.getJSONObject("CruiseLineResponse").getJSONArray("ProductAvailabilityResponse");
        } catch (Exception e) {
            logger.error(e);
            throw  e;
        }

        for (Object item:jsonArray) {
            if( !(item instanceof JSONObject))  continue;

            jsonObject = (JSONObject)item;

            Destination destination = prepateDestionation(jsonObject.getJSONObject("Destination"));
            Port embarkPort = preparePort(jsonObject.getJSONObject("EmbarkPort"));
            Port debarkPort = preparePort(jsonObject.getJSONObject("DebarkPort"));
            JSONObject tempJsonObject = jsonObject.getJSONObject("Sailing");
            Ship ship = prepareShip(tempJsonObject.getJSONObject("Ship"));

            Integer duration = tempJsonObject.getInt("DurationDays");
            Date sailingDate = null;
            try {
                sailingDate = dateFormatter.parse( tempJsonObject.get("Date").toString() );
            } catch (ParseException e) {
                logger.error(e);
                throw e;
            }

            String sailingID = jsonObject.get("SailingId").toString();
            Integer maxOccupancy = jsonObject.getInt("MaxOccupancy");

            Product product = new Product();
            product.setDestination(destination);
            product.setEmbarkPort(embarkPort);
            product.setShip(ship);
            product.setDuration(duration);
            product.setSailingDate(sailingDate);
            product.setMaxOccupancy(maxOccupancy);
            product.setDebarkPort(debarkPort);
            product.setSailingID(sailingID);
            product.setCruiseLineCode("PCL");
            product.setLastUpdateDate(date);
            productRepository.save(product);
        }

    }

    private Destination prepateDestionation(JSONObject jsonObject) {
        String code = jsonObject.getString("Code");
        String name = jsonObject.getString("Name");
        Destination destination = destinationRepository.checkAndSave(code, name);
        return  destination;
    }

    private Port preparePort(JSONObject jsonObject) {
        String code = jsonObject.getString("Code");
        String name = jsonObject.getString("Name");
        Port port = portRepository.checkAndSave(code, name);
        return port;
    }

    private Ship prepareShip(JSONObject jsonObject) {
        String code = jsonObject.getString("Code");
        String name = jsonObject.getString("Name");
        Ship ship = shipRepository.checkAndSave(code, name);
        return ship;
    }
}
