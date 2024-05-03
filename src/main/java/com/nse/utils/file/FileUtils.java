package com.nse.utils.file;

import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.BhavResult;
import com.nse.model.equity.BhavStatistics;
import com.nse.model.equity.Result;
import com.nse.model.equity.derivaties.OptionsData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class FileUtils {
    final private static Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    public static Mono<String> downloadOptionsData(String date) {
        try {
            String year = DateUtils.getDateStringForGivenFormat(date, NSEConstant.DATE_FORMAT, "yyyy");
            String month = DateUtils.getDateStringForGivenFormat(date, NSEConstant.DATE_FORMAT, "MMM").toUpperCase();
            String fullDate = DateUtils.getDateStringForGivenFormat(date, NSEConstant.DATE_FORMAT, "ddMMMyyyy").toUpperCase();
            String downloadUrl = String.format(NSEConstant.OPTIONS_DATA_URL, year, month, fullDate);
            String toDownload = String.format(NSEConstant.OPTIONS_DATA_OUTPUT_FOLDER, fullDate);
            return downloadFile(downloadUrl, toDownload);
        } catch (Exception e) {
        }
        return Mono.just("false");

    }

    public static Mono<String> downloadBhavData(String date) {
        try {
            return downloadFile(String.format(NSEConstant.BHAV_DATA_URL, date), String.format(NSEConstant.BHAV_DATA_OUTPUT_FOLDER, date));
        } catch (Exception e) {
        }
        return Mono.just("false");

    }

    public static Mono<String> downloadIndexData(String date) {
        try {
            return downloadFile(String.format(NSEConstant.INDEX_DATA_URL, date), String.format(NSEConstant.INDEX_DATA_OUTPUT_FOLDER, date));
        } catch (Exception e) {
        }
        return Mono.just("false");

    }

    public static Mono<String> downloadFile(String downloadFrom, String downloadTo) {
        Flux<DataBuffer> fileDataStream = WebClient.builder()
                .baseUrl(downloadFrom)
                .clientConnector(new ReactorClientHttpConnector()).build()
                .get().accept(MediaType.APPLICATION_OCTET_STREAM).retrieve()
                .onStatus(HttpStatus::is3xxRedirection, clientResponse -> Mono.error(() -> {
                    LOGGER.error("Failed to download the file status code : {}, reason : {} ", clientResponse.statusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return new Exception("File not found");
                })).onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(() -> {
                    LOGGER.error("Failed to download the file status code : {}, reason : {} ", clientResponse.statusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return new Exception("File not found");
                })).onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(() -> {
                    LOGGER.error("Failed to download the file status code : {}, reason : {} ", clientResponse.statusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return new Exception("File not found");
                })).
                bodyToFlux(DataBuffer.class);

        if (!fileDataStream.hasElements().block())
            return Mono.just("Failed to save");
        DataBufferUtils.write(fileDataStream, Path.of(downloadTo), StandardOpenOption.CREATE)
                .doOnError(throwable -> {
                    new Exception("Fie not found", throwable.getCause());
                }).thenReturn(Mono.just(false)).
                doOnTerminate(() -> {
                    LOGGER.info("File {} downloaded to {} ", downloadFrom, downloadTo);
                }).thenReturn(Mono.just(true))
                .block();
        return Mono.just("File saved to : " + downloadTo);
    }


    public static List<String> getCsvFileFromZipFile(String fileName) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
        final ZipInputStream is = new ZipInputStream(bis);

        ZipFile zipFilePath = new ZipFile(fileName);
        List<String> optionsData = null;

        //String csvFileName = fileName.getName().replaceAll("fo", "op").replaceAll(".zip", ".csv");
        //System.out.println("Zip file name : " + csvFileName);
        try {
            Enumeration<? extends ZipEntry> entries = zipFilePath.entries();
            while (entries.hasMoreElements()) {
                ZipEntry nextElement = entries.nextElement();
                //if (csvFileName.equalsIgnoreCase(nextElement.getName())) {
                InputStream inputStream = zipFilePath.getInputStream(nextElement);
                if (null != inputStream) {
                    try {
                        optionsData = readFileCollectOptionsData(inputStream);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } finally {
                        if (null != inputStream) {
                            inputStream.close();
                        }
                    }
                }
                break;
                // }
            }
        } finally {
            is.close();
            zipFilePath.close();
        }

        return optionsData;
    }

    public static String extractCsvFileFromZipFile(String fileName, String toFileName) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
        final ZipInputStream is = new ZipInputStream(bis);

        ZipFile zipFilePath = new ZipFile(fileName);

        //String csvFileName = fileName.replaceAll("fo", "op").replaceAll(".zip", ".csv");
        String csvFileName = fileName.replaceAll(".zip", "");
        System.out.println("Zip file name : " + csvFileName);
        try {
            Enumeration<? extends ZipEntry> entries = zipFilePath.entries();
            while (entries.hasMoreElements()) {
                ZipEntry nextElement = entries.nextElement();
                //if (csvFileName.equalsIgnoreCase(nextElement.getName())) {
                InputStream inputStream = zipFilePath.getInputStream(nextElement);
                if (null != inputStream) {
                    try {
                        extractFile(inputStream, toFileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (null != inputStream) {
                            inputStream.close();
                        }
                    }
                }
                break;
            }
        } finally {
            is.close();
            zipFilePath.close();
        }
        return csvFileName;
    }

    static List<String> readFileCollectOptionsData(InputStream is)
            throws FileNotFoundException, IOException, ParseException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        List<String> optionsData = bufferedReader.lines().collect(Collectors.toList());
        //optionsData.stream().findFirst(System.out::println);
        bufferedReader.close();
        return optionsData;
    }

    private static void extractFile(InputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public static void saveDataToFile(String fileName, List<List<OptionsData>> finalData) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter);) {
            writer.write(finalData.get(0).get(0).toStringWithHeader());

            for (List<OptionsData> finalDatum : finalData) {
                for (OptionsData data : finalDatum) {
                    try {
                        if (null != data) {
                            writer.write(data.toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // writer.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }

    public static void saveBhavDataNifty50ToFile(String fileName, List<BhavData> finalData) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter);) {
            writer.write(finalData.get(0).toStringWithHeader());

            for (BhavData data : finalData) {
                try {
                    if (null != data && NSEConstant.NSE_OPTIONS_STOCK_LIST.contains(data.getSymbol())) {
                        writer.write(data.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }

    public static void saveBhavStaticsToFile(String fileName, Map<String, BhavStatistics> stats) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SCRIPT,BREAK-OUT, BREAK-OUT-DATE, TREND, ACTION\n");

            for (BhavStatistics data : stats.values()) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.getSymbol() + "," + data.isDeliveryBreakout() + "," + data.getMaxDeliveryVolumeDate() + ","
                                + data.getBreakOutLocation() + "," + String.join(",", data.getAction()) + "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }

    public static void volumeBreakoutsToFile(String fileName, Map<String, BhavStatistics> stats) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SCRIPT,BREAK-OUT, PM-BREAK-OUT-DATE, NM-BREAK-OUT-DATE\n");

            for (BhavStatistics data : stats.values()) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.getSymbol() + "," + data.isVolumeBreakout() + "," + String.join(",", data.getAction()) + "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }

    public static void volumeBreakoutsToFileByTimeFrame(String fileName, Map<String, BhavStatistics> stats) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SCRIPT,BREAK-OUT, TIME-FRAME, PM-BREAK-OUT-DATE, NM-BREAK-OUT-DATE\n");

            for (BhavStatistics data : stats.values()) {
                try {
//                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
//                        writer.write(data.getSymbol() + "," + data.isVolumeBreakout() + "," + String.join(",", data.getAction()) + "\n");
//                    }
                    writer.write(data.getSymbol() + "," + data.isVolumeBreakout() + "," + String.join(",", data.getAction()) + "\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }
    public static void saveVolumeAndDeliveryVolumesBreakoutsToFile(String fileName, Map<String, BhavStatistics> stats) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SCRIPT,Delivery-Volume-BO,Delivery-Volume-BO-Date,Volume-BO,Volume-BO-Date, Candle-Formation, Trend, D-Location,Closing,Hypo,Action\n");

            for (BhavStatistics data : stats.values()) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.getSymbol() + "," + data.isDeliveryBreakout()+ "," + data.getMaxDeliveryVolumeBreakOutDate() + ","
                                + data.isVolumeBreakout() + "," + data.getMaxVolumeBreakoutDate()  + ","
                                + data.getCandlesPattern()  + ","
                                + data.getCandleTrend()  + ","
                                + data.getMaxDeliveryLocation()  + ","
                                + data.getCloseAsPerHighDeliveryCandle()  + ","
                                + data.getHypo()
                                //+ ","
                                //+data.getAction().stream().map(Object::toString).collect(Collectors.joining(","))
                                +"\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }

    public static void saveBhavDeliveryDataToFile(String fileName, Map<String, BhavStatistics> stats) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SCRIPT,LOW-DELIVERY-DATE,LOW-DELIVERY, HIGH-DELIVERY-DATE,HIGH-DELIVERY \n");

            for (BhavStatistics data : stats.values()) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.getSymbol() + "," + data.getMinDeliveryVolumeDate() + "," + data.getMinDeliveryQty() + "," + data.getMaxDeliveryVolumeDate() + "," + data.getMaxDeliveryQty() + "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }

    public static void saveActions(String fileName, Map<String, BhavStatistics> stats) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SCRIPT,Actions  \n");

            for (BhavStatistics data : stats.values()) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.getSymbol() + ","+ data.getMaxVolumeDate() +
                                "," + String.join(",", data.getAction()) +
                                "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }
    public static void saveLowDeliveryOfLastTwoMonths(String fileName, Map<String, BhavStatistics> stats) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SCRIPT,Low-Delivery-Date  \n");

            for (BhavStatistics data : stats.values()) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.getSymbol() +
                                "," + String.join(",", data.getAction()) +
                                "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }

    public static void saveBackTestData(String fileName, List<Result> results) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SYMBOL,HV-DATE,FORMATION,FORM-DATE,CONFIRM-DATE,ENTRY-DATE,MAX-PERCENTAGE," +
                    "ENTRY, PMIN, PMAX, ENTRY, CMIN, CMAX, " +
                    "RESULT   \n");

            for (Result data : results) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.getSymbol() +
                                "," + data.getLowDeliveryDate() +
                                "," + data.getCandleFormationType() +
                                "," + data.getMomentStartDate() +
                                "," + data.getMomentConfirmDate() +
                                "," + data.getEntryDate() +
                                "," + data.getMaxPercentage() +
                                "," + data.getPutEntry() +
                                "," + data.getPutMin() +
                                "," + data.getPutMax() +
                                "," + data.getCallEntry() +
                                "," + data.getCallMin() +
                                "," + data.getCallMax() +
                                "," + data.getResult() +
                                "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }

    public static void saveLowDeliveryDataToFile(String fileName, Map<String, BhavStatistics> stats) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SCRIPT,LOW-DELIVERY-BO-DATE, HIGH, LOW, MONTH-CLOSE \n");

            for (BhavStatistics data : stats.values()) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.getSymbol() + "," + data.getMinDeliveryVolumeBreakOutDate() +
                                "," + data.getMinDeliveryQtyHigh() +
                                "," + data.getMinDeliveryQtyLow() +
                                "," + data.getClose() +
                                "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }


    public static void saveHighVolumesDatasToFile(String fileName, Map<String, BhavStatistics> stats) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SCRIPT, Delivery-Breakout-Date, Delivery-Breakin-Date \n");

            for (BhavStatistics data : stats.values()) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.getSymbol() + "," + data.isDeliveryBreakout() + "," + data.getMaxDeliveryVolumeBreakOutDate() +
                                "," + data.getMaxDeliveryBreakoutLow() + "," + data.getMaxDeliveryBreakoutHigh() +
                                "," + data.getMaxDeliveryQtyLow() + "," + data.getMaxDeliveryQtyHigh() + "," + data.getMaxDeliveryQtyClose() +
                                "," + String.join(",", data.getAction()) +
                                "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }

    public static void saveBreakOutFailToFile(String fileName, Map<String, BhavStatistics> stats) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SCRIPT, Breakout-Date, Break-in-Date, Volumes-Breakout, high-Volume-Date, high-Delivery-Date, Month-Target, High-Volume-Target, target, action \n");

            for (BhavStatistics data : stats.values()) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.getSymbol() + "," + data.getMaxVolumeBreakoutDate()
                                + "," + data.getMinVolumeBreakoutDate()
                                + "," + data.isVolumeBreakout()
                                + "," + data.getMaxVolumeDate()
                                + "," + data.getMaxDeliveryVolumeDate()
                                + "," + data.isMonthHighLowTarget()
                                + "," + data.isHighVolumeHighLowTarget()
                                + "," + String.join(",", data.getAction()) +"\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }


    public static void saveGapUpFailToFile(String fileName, Map<String, BhavStatistics> stats) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SCRIPT, Action \n");

            for (BhavStatistics data : stats.values()) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.getSymbol()
                                + "," + String.join(",", data.getAction()) +"\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }




    public static void saveVolumeBreakoutAnalysis(String fileName, Map<String, BhavStatistics> stats) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("SCRIPT, Action \n");

            for (BhavStatistics data : stats.values()) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.getSymbol()
                                + "," + String.join(",", data.getAction()) +"\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }
    public static void saveBhavResultNifty50ToFile(String fileName, Map<String, BhavResult> finalData) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write(finalData.values().stream().findFirst().get().toStringWithHeader());

            for (BhavResult data : finalData.values()) {
                try {
                    if (null != data && NSEConstant.getNiftyList().get(data.getSymbol()) != null) {
                        writer.write(data.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }

    public static void saveDataListToFile(String fileName, List<OptionsData> finalData) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter);) {
            writer.write(finalData.get(0).toStringWithHeader());

            for (OptionsData data : finalData) {
                try {
                    if (null != data) {
                        writer.write(data.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // writer.write("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }

    public static void saveDataToFileWithOption(String fileName, List<Optional<OptionsData>> finalData) {
        File file = new File(fileName);
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter);) {
            writer.write(finalData.get(0).get().toStringWithHeader());

            // writer.write("\n");
            finalData.stream().flatMap(Optional::stream).forEach(data -> {
                try {
                    if (null != data) {
                        writer.write(data.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("File saved to : " + file.getPath());
    }

    public static Flux<String> readFileFromLocation(String fromLocation) {
        Flux<String> stringFlux = Flux.empty();
        stringFlux = Flux.using(
                () -> Files.lines(Path.of(fromLocation)),
                Flux::fromStream,
                Stream::close
        ).onErrorReturn("");
        return stringFlux;
    }

    public static boolean isFileExists(String fromLocation) {
        return new File(fromLocation).exists();
    }

    public static void main(String[] args) {
        Flux<String> stringFlux = readFileFromLocation(String.format(NSEConstant.BHAV_DATA_OUTPUT_FOLDER, "01122021"));
        stringFlux.subscribe(s -> {
            LOGGER.info(s);
        });
    }

    public static List<BhavData> loadBhavDataIfNotExistsDownloadFromNse(String date) {
        String fileLocation = String.format(NSEConstant.BHAV_DATA_OUTPUT_FOLDER, date);
        Flux<String> stringFlux = FileUtils.readFileFromLocation(fileLocation);
        List<String> retry = new ArrayList<>();
        stringFlux.subscribe(s -> {
            if (s == null || s.isEmpty()) {
                FileUtils.downloadBhavData(date).block();
                LOGGER.info("Downloading from nse website");
//                retry.add("1");
            }
        });

        /*if (!retry.isEmpty()) {
            stringFlux = FileUtils.readFileFromLocation(fileLocation);
        }*/
        return new BhavData().toBean(stringFlux);
    }
}
