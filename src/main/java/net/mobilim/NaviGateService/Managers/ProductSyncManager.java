package net.mobilim.NaviGateService.Managers;


import net.mobilim.NaviGateService.Exceptions.AdvisoryException;
import net.mobilim.NaviGateService.Helpers.XmlDefinitions;
import net.mobilim.NaviGateService.Services.ProductService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Component
public class ProductSyncManager {

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMddyyyy");
    private final Logger logger = LoggerFactory.getLogger(ProductSyncManager.class);
    private final Integer MAX_YEAR = 5;

    @Autowired
    private DownloadManager downloadManager;

    @Autowired
    private ApplicationContext appContext;

    public ProductSyncManager() {
    }

    public void startSync() throws Exception {

        JSONArray jsonArray;
        Date date = new Date();
        String fromDate = dateFormatter.format(date);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, MAX_YEAR);
        Date tillDate = calendar.getTime();

        while (date.compareTo(tillDate) < 0) {
            try {

            String xmlPostData = String.format(XmlDefinitions.PRODUCT, fromDate, "", "5", "60", "", "");
            JSONObject jsonObject = downloadManager.download(xmlPostData);
            jsonArray = jsonObject.getJSONArray("ProductAvailabilityResponse");

            for (Object item : jsonArray) {
                if (!(item instanceof JSONObject)) continue;
                if( ((JSONObject)item).getJSONObject("SailingStatus").getString("Code").equals("CL") ) continue;
                logger.info(item.toString());
                try {
                    ProductService productService = appContext.getBean(ProductService.class);
                    date = productService.saveOrUpdateProduct( (JSONObject)item );
                }
                catch (AdvisoryException ex) {
                    logger.warn("", ex);
                }
            }
            calendar.setTime(date);
            calendar.add(Calendar.DATE, 1);
            date = calendar.getTime();
            fromDate = dateFormatter.format(date);
            logger.info(String.format("from date:%s", fromDate));
            }
            catch (Exception ex) {
                logger.error("", ex);
                throw ex;
            }
        }
    }
}
