package net.mobilim.NaviGateService;


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

    }
}
