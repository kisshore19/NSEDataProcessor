package com.nse;

import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.BhavResult;
import com.nse.utils.file.FileUtils;
import com.nse.utils.file.NseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrategyMonthlyHighestDeliveryVolume {

    static final Logger LOGGER = LoggerFactory.getLogger(StratagyWithChangeOI.class);
    static Map<String, BhavResult> result = new HashMap();

    public static void main(String[] args) {
        String from1 = "01-01-2024".replace("-", "");
        String to1 = "31-01-2024".replace("-", "");

        String from2 = "01-02-2024".replace("-", "");
        String to2 = "29-02-2024".replace("-", "");

        StrategyMonthlyHighestDeliveryVolume firstMonth = new StrategyMonthlyHighestDeliveryVolume();
        firstMonth.start(from1, to1);

        StrategyMonthlyHighestDeliveryVolume secondMonth = new StrategyMonthlyHighestDeliveryVolume();
        secondMonth.start(from2, to2);



        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from2 + "-" + to2 + "-MONTH-HIGH-DELIVERY.csv");
        FileUtils.saveBhavResultNifty50ToFile(reportFileName, result);
    }

    public void start(String from, String to) {
        NseData strategy = new NseData(from, to);
        List<BhavData> finalData = new ArrayList<>();
        strategy.bhavDataBySymbol.forEach((s, bhavData) -> {
            BhavData maxDevlivery = new BhavData();
            BhavResult highDelVol = result.get(bhavData.get(0).getSymbol());
            if(null == highDelVol){
                for (BhavData bv : bhavData) {
                    if (bv.getDeliveryQty() > maxDevlivery.getDeliveryQty()) {
                        maxDevlivery = bv.copy();
                    }
                }
            }else{
                for (BhavData bv : bhavData) {
                    if (bv.getDeliveryQty() > maxDevlivery.getDeliveryQty()) {
                        maxDevlivery = bv.copy();
                        //break;
                    }
                }
            }


            BhavResult bhavResult = result.get(maxDevlivery.getSymbol());
            if (null == bhavResult) {
                if(null != maxDevlivery.getSymbol()){
                    result.put(maxDevlivery.getSymbol(), new BhavResult(maxDevlivery));
                }
            } else {
                bhavResult.setDeliveryVolumeBODate(maxDevlivery.getTradingDate());
                bhavResult.setDeliveryVolumeBOHigh(maxDevlivery.getHighPrice());
                bhavResult.setDeliveryVolumeBOLow(maxDevlivery.getLowPrice());
                bhavResult.setDeliveryBoQty(maxDevlivery.getDeliveryQty());
                if (maxDevlivery.getDeliveryQty() > bhavResult.getDeliveryQty()) {
                    bhavResult.setDeliveryBO(true);
                }
            }
        });
    }

}
