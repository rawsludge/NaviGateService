package net.mobilim.NaviGateService;

import com.sun.tools.javah.Util;
import jdk.nashorn.internal.parser.JSONParser;
import net.mobilim.NaviGateData.Entities.Destination;
import net.mobilim.NaviGateData.Entities.Port;
import net.mobilim.NaviGateData.Entities.Product;
import net.mobilim.NaviGateData.Entities.Ship;
import net.mobilim.NaviGateData.Repositories.DestinationRepository;
import net.mobilim.NaviGateData.Repositories.PortRepository;
import net.mobilim.NaviGateData.Repositories.ProductRepository;
import net.mobilim.NaviGateData.Repositories.ShipRepository;
import net.mobilim.NaviGateService.Helpers.XmlDefinitions;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.JsonPath;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SpringBootApplication
@EntityScan("net.mobilim.NaviGateData.Entities")
@EnableJpaRepositories("net.mobilim.NaviGateData.Repositories")
@ComponentScan("net.mobilim.NaviGateData.Repositories")
public class ServiceApp {
    private Logger _logger = LogManager.getLogger(ServiceApp.class);

    @Value("${website.url}")
    private String websiteUrl;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder()
                .sources(ServiceApp.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
        ServiceApp app = context.getBean(ServiceApp.class);
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
        _logger.info(String.format("Application started.", args));

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
            _logger.error(e);
            return;
        }
        JSONObject jsonObject = XML.toJSONObject(response.toString());
        JSONArray jsonArray = jsonObject.getJSONObject("CruiseLineResponse").getJSONArray("ProductAvailabilityResponse");

        for ( Object object : jsonArray ) {
            if( object instanceof JSONObject) {
                _logger.info(object);
                JSONObject productJsonObject = (JSONObject)object;
                JSONObject tempObject = productJsonObject.getJSONObject("Destination");
                String code = tempObject.getString("Code");
                String name = tempObject.getString("Name");
                Destination destination = checkDestinationAndSave(code, name);

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
        /*
        Product product = new Product();

        Ship ship = checkShipAndSave("PA", "Losangels");
        product.setShip(ship);

        Destination dest = checkDestinationAndSave("LA", "Losangels");
        product.setDestination(dest);

        Port port = checkPortAndSave("FLL", "Ft. Lauderdale, Florida");
        product.setEmbarkPort(port);

        port = checkPortAndSave("FLL", "Ft. Lauderdale, Florida");
        product.setDebarkPort(port);

        product.setSailingID("K902");
        product.setLastUpdateDate(new Date());
        productRepository.save(product);
        */
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
