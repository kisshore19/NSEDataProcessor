package com.nse.constants;

import java.util.HashMap;
import java.util.Map;

public interface NSEConstant {
	//https://archives.nseindia.com/products/content/sec_bhavdata_full_01062021.csv
	//https://www1.nseindia.com/content/historical/DERIVATIVES/2021/FEB/fo01FEB2021bhav.csv.zip
	final public static String BHAV_DATA_URL = "https://archives.nseindia.com/products/content/sec_bhavdata_full_%s.csv";
	final public static String INDEX_DATA_URL = "https://archives.nseindia.com/content/indices/ind_close_all_%s.csv";
	final public static String OPTIONS_DATA_URL = "https://archives.nseindia.com/content/historical/DERIVATIVES/%s/%s/fo%sbhav.csv.zip";
	final public static String INDEX_DATA_OUTPUT_FOLDER = "E:\\Stock Research\\Data\\NseData\\Index-Data\\ind_close_all_%s.csv";
	final public static String BHAV_DATA_OUTPUT_FOLDER = "E:\\Stock Research\\Data\\NseData\\Bhav-Data\\sec_bhavdata_full_%s.csv";
	final public static String OPTIONS_DATA_OUTPUT_FOLDER = "E:\\Stock Research\\Data\\NseData\\Options-Data\\fo%sbhav.csv.zip";
	final public static String OPTIONS_DATA_OUTPUT_FOLDER_CSV = "E:\\Stock Research\\Data\\NseData\\Options-Data\\extracted\\fo%sbhav.csv";
	final public static String OPTIONS_DATA_REPORT_FOLDER = "E:\\Stock Research\\Data\\NseData\\Reports\\%s";
	final public static String DAILY_REPORTS_FILE_PATH = "E:\\Stock Research\\Watchlist\\GeneratedReports\\";
	final public static String SUPPORT_RESISTANCES_PATH = "E:\\Stock Research\\Kishore-TradeSetup\\BackTesting\\";
	final public static String NSE_MASTER_DATA = "E:\\myspace\\stocks\\NiftyMasterData.csv";
	// ONLY NIFTY - 50 STOCKS
	final public static String NSE_OPTIONS_STOCK_LIST ="NIFTY,BANKNIFTY,ADANIPORTS,ASIANPAINT,AXISBANK,BAJAJ-AUTO,BAJAJFINSV,BAJFINANCE,BHARTIARTL,BPCL,BRITANNIA,CIPLA,DIVISLAB,DRREDDY,EICHERMOT,GRASIM,HCLTECH,HDFC,HDFCBANK,HDFCLIFE,HEROMOTOCO,HINDALCO,HINDUNILVR,ICICIBANK,INDUSINDBK,INFY,JSWSTEEL,KOTAKBANK,LT,M&M,MARUTI,ONGC,RELIANCE,SBILIFE,SBIN,SHREECEM,SUNPHARMA,TATACONSUM,TATAMOTORS,TATASTEEL,TCS,TECHM,TITAN,ULTRACEMCO,UPL,WIPRO";
	public static Map<String, String> NSE_OPTIONS_STOCK_MAP = new HashMap<>();

	//, "","EICHERMOT", "","GRASIM", "","HCLTECH", "","HDFC", "","HDFCBANK", "","HDFCLIFE", "","HEROMOTOCO", "","HINDALCO", "","HINDUNILVR", "","ICICIBANK", "","INDUSINDBK", "","INFY", "","JSWSTEEL", "","KOTAKBANK", "","LT", "","M&M", "","MARUTI", "","ONGC", "","RELIANCE", "","SBILIFE", "","SBIN", "","SHREECEM", "","SUNPHARMA", "","TATACONSUM", "","TATAMOTORS", "","TATASTEEL", "","TCS", "","TECHM", "","TITAN", "","ULTRACEMCO", "","UPL", "","WIPRO"
//	final public static String NSE_OPTIONS_STOCK_LIST ="NIFTY,BANKNIFTY,AARTIIND,ABBOTINDIA,ABCAPITAL,ABFRL,ACC,ADANIENT,ADANIPORTS,ALKEM,AMARAJABAT,AMBUJACEM,APLLTD,APOLLOHOSP,APOLLOTYRE,ASHOKLEY,ASIANPAINT,ASTRAL,ATUL,AUBANK,AUROPHARMA,AXISBANK,BAJAJ-AUTO,BAJAJFINSV,BAJFINANCE,BALKRISIND,BALRAMCHIN,BANDHANBNK,BANKBARODA,BATAINDIA,BEL,BERGEPAINT,BHARATFORG,BHARTIARTL,BHEL,BIOCON,BOSCHLTD,BPCL,BRITANNIA,BSOFT,CADILAHC,CANBK,CANFINHOME,CHAMBLFERT,CHOLAFIN,CIPLA,COALINDIA,COFORGE,COLPAL,CONCOR,COROMANDEL,CROMPTON,CUB,CUMMINSIND,DABUR,DALBHARAT,DEEPAKNTR,DELTACORP,DIVISLAB,DIXON,DLF,DRREDDY,EICHERMOT,ESCORTS,EXIDEIND,FEDERALBNK,FSL,GAIL,GLENMARK,GMRINFRA,GNFC,GODREJCP,GODREJPROP,GRANULES,GRASIM,GSPL,GUJGASLTD,HAL,HAVELLS,HCLTECH,HDFC,HDFCAMC,HDFCBANK,HDFCLIFE,HEROMOTOCO,HINDALCO,HINDCOPPER,HINDPETRO,HINDUNILVR,HONAUT,IBULHSGFIN,ICICIBANK,ICICIGI,ICICIPRULI,IDEA,IDFC,IDFCFIRSTB,IEX,IGL,INDHOTEL,INDIACEM,INDIAMART,INDIGO,INDUSINDBK,INDUSTOWER,INFY,IOC,IPCALAB,IRCTC,ITC,JINDALSTEL,JKCEMENT,JSWSTEEL,JUBLFOOD,KOTAKBANK,L&TFH,LALPATHLAB,LAURUSLABS,LICHSGFIN,LT,LTI,LTTS,LUPIN,M&M,M&MFIN,MANAPPURAM,MARICO,MARUTI,MCDOWELL-N,MCX,METROPOLIS,MFSL,MGL,MINDTREE,MOTHERSUMI,MPHASIS,MRF,MUTHOOTFIN,NAM-INDIA,NATIONALUM,NAUKRI,NAVINFLUOR,NBCC,NESTLEIND,NMDC,NTPC,OBEROIRLTY,OFSS,ONGC,PAGEIND,PEL,PERSISTENT,PETRONET,PFC,PFIZER,PIDILITIND,PIIND,PNB,POLYCAB,POWERGRID,PVR,RAIN,RAMCOCEM,RBLBANK,RECLTD,RELIANCE,SAIL,SBICARD,SBILIFE,SBIN,SHREECEM,SIEMENS,SRF,SRTRANSFIN,STAR,SUNPHARMA,SUNTV,SYNGENE,TATACHEM,TATACOMM,TATACONSUM,TATAMOTORS,TATAPOWER,TATASTEEL,TCS,TECHM,TITAN,TORNTPHARM,TORNTPOWER,TRENT,TVSMOTOR,UBL,ULTRACEMCO,UPL,VEDL,VOLTAS,WHIRLPOOL,WIPRO,ZEEL";

	//https://www1.nseindia.com/content/indices/ind_close_all_19082021.csv
	//https://archives.nseindia.com/products/content/sec_bhavdata_full_01062021.csv
	//https://archives.nseindia.com/products/content/sec_bhavdata_full_03012018.csv

	//16042021
	final public static String DATE_FORMAT = "ddMMyyyy";

	public static Map<String, String> getNiftyList(){

		if(NSE_OPTIONS_STOCK_MAP.isEmpty()){
			NSE_OPTIONS_STOCK_MAP.put("NIFTY","");
			NSE_OPTIONS_STOCK_MAP.put("BANKNIFTY","");
			NSE_OPTIONS_STOCK_MAP.put("ADANIPORTS", "");
			NSE_OPTIONS_STOCK_MAP.put("ASIANPAINT", "");
			NSE_OPTIONS_STOCK_MAP.put("AXISBANK", "");
			NSE_OPTIONS_STOCK_MAP.put("BAJAJ-AUTO", "");
			NSE_OPTIONS_STOCK_MAP.put("BAJAJFINSV", "");
			NSE_OPTIONS_STOCK_MAP.put("BAJFINANCE", "");
			NSE_OPTIONS_STOCK_MAP.put("BHARTIARTL", "");
			NSE_OPTIONS_STOCK_MAP.put("BPCL", "");
			NSE_OPTIONS_STOCK_MAP.put("BRITANNIA", "");
			NSE_OPTIONS_STOCK_MAP.put("CIPLA", "");
			NSE_OPTIONS_STOCK_MAP.put("DIVISLAB", "");
			NSE_OPTIONS_STOCK_MAP.put("DRREDDY", "");
			NSE_OPTIONS_STOCK_MAP.put("EICHERMOT", "");
			NSE_OPTIONS_STOCK_MAP.put("GRASIM", "");
			NSE_OPTIONS_STOCK_MAP.put("HCLTECH", "");
			NSE_OPTIONS_STOCK_MAP.put("HDFC", "");
			NSE_OPTIONS_STOCK_MAP.put("HDFCBANK", "");
			NSE_OPTIONS_STOCK_MAP.put("HDFCLIFE", "");
			NSE_OPTIONS_STOCK_MAP.put("HEROMOTOCO", "");
			NSE_OPTIONS_STOCK_MAP.put("HINDALCO", "");
			NSE_OPTIONS_STOCK_MAP.put("HINDUNILVR", "");
			NSE_OPTIONS_STOCK_MAP.put("ICICIBANK", "");
			NSE_OPTIONS_STOCK_MAP.put("INDUSINDBK", "");
			NSE_OPTIONS_STOCK_MAP.put("INFY", "");
			NSE_OPTIONS_STOCK_MAP.put("JSWSTEEL", "");
			NSE_OPTIONS_STOCK_MAP.put("KOTAKBANK", "");
			NSE_OPTIONS_STOCK_MAP.put("LT", "");
			NSE_OPTIONS_STOCK_MAP.put("M&M", "");
			NSE_OPTIONS_STOCK_MAP.put("MARUTI", "");
			NSE_OPTIONS_STOCK_MAP.put("ONGC", "");
			NSE_OPTIONS_STOCK_MAP.put("RELIANCE", "");
			NSE_OPTIONS_STOCK_MAP.put("SBILIFE", "");
			NSE_OPTIONS_STOCK_MAP.put("SBIN", "");
			NSE_OPTIONS_STOCK_MAP.put("SHREECEM", "");
			NSE_OPTIONS_STOCK_MAP.put("SUNPHARMA", "");
			NSE_OPTIONS_STOCK_MAP.put("TATACONSUM", "");
			NSE_OPTIONS_STOCK_MAP.put("TATAMOTORS", "");
			NSE_OPTIONS_STOCK_MAP.put("TATASTEEL", "");
			NSE_OPTIONS_STOCK_MAP.put("TCS", "");
			NSE_OPTIONS_STOCK_MAP.put("TECHM", "");
			NSE_OPTIONS_STOCK_MAP.put("TITAN", "");
			NSE_OPTIONS_STOCK_MAP.put("ULTRACEMCO", "");
			NSE_OPTIONS_STOCK_MAP.put("UPL", "");
			NSE_OPTIONS_STOCK_MAP.put("WIPRO","");

		}

		return NSE_OPTIONS_STOCK_MAP;
	}

}
