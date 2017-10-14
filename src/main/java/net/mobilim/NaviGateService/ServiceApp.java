package net.mobilim.NaviGateService;


import net.mobilim.NaviGateService.Managers.ProductSyncManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement
@ComponentScan("net.mobilim.NaviGateService")
@EntityScan("net.mobilim.NaviGateData.Entities")
@EnableJpaRepositories("net.mobilim.NaviGateData.Repositories")
public class ServiceApp {
    private final Logger logger = LogManager.getLogger(ServiceApp.class);
    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        context = new SpringApplicationBuilder()
                .sources(ServiceApp.class)
                //.bannerMode(Banner.Mode.OFF)
                .run(args);
        ServiceApp app = context.getBean(ServiceApp.class);
        app.start(args);
    }

    private void start(String[] args) {
        StringBuilder sb = new StringBuilder();
        for(String s : args){
            sb.append(s).append(" ");
        }
        logger.info("Application started. Args:{}", sb.toString());

        ProductSyncManager productSyncManager = context.getBean(ProductSyncManager.class);
        try {
            productSyncManager.startSync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
