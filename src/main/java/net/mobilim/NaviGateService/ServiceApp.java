package net.mobilim.NaviGateService;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ServiceApp {
    private Logger _logger = LogManager.getLogger(ServiceApp.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder()
                .sources(ServiceApp.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
        ServiceApp app = context.getBean(ServiceApp.class);
        app.start();
    }

    private void start() {
        _logger.info("Application started");
    }
}
