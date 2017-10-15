package net.mobilim.NaviGateService.Managers;


import com.jcabi.aspects.Loggable;
import net.mobilim.NaviGateService.Exceptions.AdvisoryException;
import net.mobilim.NaviGateService.Helpers.XmlDefinitions;
import net.mobilim.NaviGateService.Services.ProductService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Component
public class ProductSyncManager {

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMddyyyy");
    private final Logger logger = LogManager.getLogger(ProductSyncManager.class);
    private final Integer MAX_YEAR = 5;

    @Autowired
    private DownloadManager downloadManager;

    @Autowired
    private ApplicationContext appContext;

    @Loggable(Loggable.DEBUG)
    public void startSync() throws Exception {

        Date date = new Date();
        String fromDate = dateFormatter.format(date);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, MAX_YEAR);
        Date tillDate = calendar.getTime();

        while (date.compareTo(tillDate) < 0) {
            String xmlPostData = String.format(XmlDefinitions.PRODUCT, fromDate, "", "5", "60", "", "");
            logger.info("Begin product availability request. Begin date:{}", fromDate);
            JSONObject jsonObject = downloadManager.download(xmlPostData);
            logger.info("End product availability request");
            Object productAvailability = jsonObject.get("ProductAvailabilityResponse");
            if( productAvailability instanceof JSONArray) {
                logger.info("Product availability is array and size is {}", ((JSONArray)productAvailability).length());
                Integer index = 0;
                for (Object item : (JSONArray)productAvailability ) {
                    if (!(item instanceof JSONObject)) continue;
                    logger.info("{}. product details is being download.", index++);
                    if (((JSONObject) item).getJSONObject("SailingStatus").getString("Code").equals("CL")) continue;
                    try {
                        ProductService productService = appContext.getBean(ProductService.class);
                        date = productService.saveOrUpdateProduct((JSONObject) item);
                    } catch (AdvisoryException ex) {
                        logger.info("Advisory exception.", ex);
                    }
                }
            }
            calendar.setTime(date);
            calendar.add(Calendar.DATE, 1);
            date = calendar.getTime();
            fromDate = dateFormatter.format(date);
        }
    }
}
