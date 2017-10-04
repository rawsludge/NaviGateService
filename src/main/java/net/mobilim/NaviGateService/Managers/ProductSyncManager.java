package net.mobilim.NaviGateService.Managers;

import net.mobilim.Repositories.Entities.Destination;
import net.mobilim.Repositories.Repositories.DestinationRepository;
import net.mobilim.Repositories.Repositories.PortRepository;
import net.mobilim.Repositories.Repositories.ProductRepository;
import net.mobilim.Repositories.Repositories.ShipRepository;
import net.mobilim.NaviGateService.Helpers.XmlDefinitions;
import net.mobilim.NaviGateService.HttpWebRequest;
import net.mobilim.NaviGateService.ServiceApp;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Value;

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

            JSONObject tempObject = jsonObject.getJSONObject("Destination");
            String code = tempObject.getString("Code");
            String name = tempObject.getString("Name");
            Destination destination = destinationRepository. checkDestinationAndSave(code, name);

        }

    }
}
