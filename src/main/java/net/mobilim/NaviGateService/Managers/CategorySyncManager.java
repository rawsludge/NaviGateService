package net.mobilim.NaviGateService.Managers;

import net.mobilim.NaviGateData.Entities.Product;
import net.mobilim.NaviGateService.Helpers.XmlDefinitions;
import net.mobilim.NaviGateService.HttpWebRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component
public class CategorySyncManager {
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMddyyyy");

    @Autowired
    private HttpWebRequest httpWebRequest;

    public CategorySyncManager() {

    }

    public void startSync(Product product) throws Exception {

        String guestData = "";
        String response;

        for (int index=0; index < product.getMaxOccupancy(); index++) {
            guestData += String.format("<Guest SeqNumber=\"%d\" AgeCode=\"A\"/>", index + 1);
        }
        String sailingDate = dateFormatter.format(product.getSailingDate());
        String xmlPostData = String.format(XmlDefinitions.CATEGORY, product.getSailingID(),
                sailingDate, String.format(
                        "<NumberOfGuests>%d</NumberOfGuests>%s", product.getMaxOccupancy(), guestData));
        try {
            response = httpWebRequest.Post(xmlPostData);
        }
        catch (Exception ex) {

        }
    }
}
