package com.nse;

import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.derivaties.OptionsData;
import com.nse.utils.file.FileUtils;
import com.nse.utils.file.NseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StratagySupportResistanceWithVolume {

    static final Logger LOGGER = LoggerFactory.getLogger(StratagyWithChangeOI.class);

    public static void main(String[] args) {
        String from = "01122021";
        String to = "30122021";
        StratagySupportResistanceWithVolume stratagy = new StratagySupportResistanceWithVolume();
        stratagy.start(from, to);
    }

    public String start(String from, String to) {
        NseData stratagy = new NseData(from, to);
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to + "-LEVELS.csv");

        stratagy.getBhavDataFull().forEach(bhavData -> {
            stratagy.isLevelConfirmed(bhavData);
        });

        FileUtils.saveBhavDataNifty50ToFile(reportFileName, stratagy.getBhavDataFull());
        return reportFileName;
    }
}
