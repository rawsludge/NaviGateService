package net.mobilim.NaviGateService;

import net.mobilim.NaviGateData.Entities.Destination;
import net.mobilim.NaviGateData.Entities.Port;
import net.mobilim.NaviGateData.Entities.Product;
import net.mobilim.NaviGateData.Entities.Ship;
import net.mobilim.NaviGateService.Interfaces.ProductRepository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EntityScan("net.mobilim.NaviGateData.Entities")
public class ServiceApp {
    private Logger _logger = LogManager.getLogger(ServiceApp.class);

    @Value("{website.url}")
    private String websiteUrl;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder()
                .sources(ServiceApp.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
        ServiceApp app = context.getBean(ServiceApp.class);
        app.start();
    }

    @Autowired
    private ProductRepository productRepository;

    private void start() {
        _logger.info("Application started");
        Product product = new Product();
        Ship ship = new Ship("PA", "Princes");
        product.setShip(ship);
        Destination dest = new Destination("LA", "Losangels");
        product.setDestination(dest);
        Port port = new Port("FLL", "Ft. Lauderdale, Florida");
        product.setEmbarkPort(port);
        port = new Port("FLL", "Ft. Lauderdale, Florida");
        product.setDebarkPort(port);
        productRepository.save(product);
    }
}
