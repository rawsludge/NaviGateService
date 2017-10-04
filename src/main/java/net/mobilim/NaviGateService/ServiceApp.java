package net.mobilim.NaviGateService;

import net.mobilim.Repositories.Entities.Destination;
import net.mobilim.Repositories.Entities.Port;
import net.mobilim.Repositories.Entities.Product;
import net.mobilim.Repositories.Entities.Ship;
import net.mobilim.Repositories.Repositories.DestinationRepository;
import net.mobilim.Repositories.Repositories.PortRepository;
import net.mobilim.Repositories.Repositories.ProductRepository;
import net.mobilim.Repositories.Repositories.ShipRepository;
import net.mobilim.NaviGateService.Helpers.XmlDefinitions;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


@EntityScan("net.mobilim.NaviGateData.Entities")
@EnableJpaRepositories("net.mobilim.NaviGateData.Repositories")
public class ServiceApp {
    private final Logger logger = LogManager.getLogger(ServiceApp.class);
    private ConfigurableApplicationContext context;

    public void main(String[] args) {
        this.context = new SpringApplicationBuilder()
                .sources(ServiceApp.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
        ServiceApp app = this.context.getBean(ServiceApp.class);
        app.start(args);
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PortRepository portRepository;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    private void start(String[] args) {
        StringBuilder sb = new StringBuilder();
        for(String s : args){
            sb.append(s).append(" ");
        }
        logger.info(String.format("Application started. Args:%s", sb.toString()));

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy");

        Calendar calendar = Calendar.getInstance();
        String fromDate = dateFormat.format(date);

        calendar.setTime(date);
        calendar.add(Calendar.YEAR, 1);
        date = calendar.getTime();

        String toDate = dateFormat.format(date);

        String xmlPostData = String.format( XmlDefinitions.PRODUCT, fromDate, toDate, "5", "30", "", "");

        HttpWebRequest webRequest = HttpWebRequest.Create(websiteUrl);
        String response = "";
        try {
            response = webRequest.Post(xmlPostData);
        } catch (Exception e) {
            logger.error(e);
            return;
        }
        JSONObject jsonObject = XML.toJSONObject(response.toString());

        for ( Object object : jsonArray ) {
            if( object instanceof JSONObject) {
                logger.info(object);
                JSONObject productJsonObject = (JSONObject)object;

                tempObject = productJsonObject.getJSONObject("EmbarkPort");
                code = tempObject.getString("Code");
                name = tempObject.getString("Name");
                Port embarkPort = checkPortAndSave(code, name);

                tempObject = productJsonObject.getJSONObject("Sailing");
                code = tempObject.getJSONObject("Ship").getString("Code");
                name = tempObject.getJSONObject("Ship").getString("Name");
                Ship ship = checkShipAndSave(code, name);

                Integer duration = tempObject.getInt("DurationDays");
                Date sailingDate = null;
                try {
                    sailingDate = dateFormat.parse( tempObject.get("Date").toString() );
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                tempObject = productJsonObject.getJSONObject("DebarkPort");
                code = tempObject.getString("Code");
                name = tempObject.getString("Name");
                Port debarkPort = checkPortAndSave(code, name);

                String sailingID = productJsonObject.get("SailingId").toString();
                Integer maxOccupancy = productJsonObject.getInt("MaxOccupancy");
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
    }

    private Ship checkShipAndSave(String code, String name) {
        Ship ship = shipRepository.findByCode(code);
        if( ship == null) {
            ship = new Ship(code, name);
            ship.setLastUpdateDate(new Date());
            shipRepository.save(ship);
        }
        return ship;
    }

    private Port checkPortAndSave(String code, String name) {
        Port port = portRepository.findByCode(code);
        if(port == null) {
            port = new Port(code, name);
            port.setLastUpdateDate(new Date());
            portRepository.save(port);
        }
        return port;
    }

    private Destination checkDestinationAndSave(String code, String name) {
        Destination destination = destinationRepository.findByCode(code);
        if(destination == null) {
            destination = new Destination(code, name);
            destination.setLastUpdateDate(new Date());
            destinationRepository.save(destination);
        }
        return destination;
    }
}
