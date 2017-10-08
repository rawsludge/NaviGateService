package net.mobilim.NaviGateService.Managers;

import net.mobilim.NaviGateData.Entities.Product;
import net.mobilim.NaviGateService.HttpWebRequest;
import org.springframework.beans.factory.annotation.Autowired;

public class CategorySyncManager {
    @Autowired
    private HttpWebRequest httpWebRequest;

    public CategorySyncManager() {

    }

    public void startSync(Product product) throws Exception {

    }
}
