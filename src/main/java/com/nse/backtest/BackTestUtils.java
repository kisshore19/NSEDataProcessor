package com.nse.backtest;

import com.nse.model.equity.BhavData;
import com.nse.model.equity.BhavStatistics;

import java.util.List;

public class BackTestUtils {

    public static String backTesting(BhavStatistics lastData, BhavStatistics currentData, List<BhavData> data){

        boolean isPriceBroke = false;
        double refCandleHigh=0;
        boolean isSupportConfirmed =false;
        boolean isEntryFound = false;
        String result = "NOTHING";

//        if(null != data && data.size()>0 && data.get(0).getOpenPrice() > lastData.getLow()
//                && data.get(0).getOpenPrice() < currentData.getHigh() && data.get(0).getHighPrice() < currentData.getHigh()){
        if(null != data && data.size()>0){
            for (BhavData bhavData: data) {
                if(isSupportConfirmed){
                    if(isEntryFound){
                        if(bhavData.getHighPrice() > lastData.getHigh()){
                            result = "TARGET";
                            break;
                        } else if (bhavData.getLowPrice() < currentData.getLow()) {
                            result = "STOP-LOSS";
                            break;
                        }

                    }else if(bhavData.getLastPrice() < refCandleHigh){
                        isEntryFound = true;
                        result = "ENTRY-FOUND";
                    }
                    continue;
                }else if(bhavData.getHighPrice() > lastData.getHigh()){
                    result = "MOVING-TOWARDS-RESISTANCE";
                    break;
                }

                if(!isPriceBroke && bhavData.getLastPrice() < lastData.getLow()){
                    refCandleHigh = bhavData.getHighPrice();
                    result = "REACHED-LOW";
                    isPriceBroke = true;
                }else if(isPriceBroke && bhavData.getLastPrice() < currentData.getLow()){
                    result = "NO-CONFIRMATION";
                    break;
                } else if (isPriceBroke && bhavData.getLastPrice() > refCandleHigh) {
                    isSupportConfirmed = true;
                    result = "CONFIRMED-WAITING-FOR-ENTRY";
                }
            }

        }
        return result;
    }
}
