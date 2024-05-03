CREATE TABLE `options_data` (
  `INSTRUMENT` varchar(40) DEFAULT NULL,
  `SYMBOL` varchar(40) DEFAULT NULL,
  `EXPIRY_DT` varchar(20) DEFAULT NULL,
  `STRIKE_PR` double(20,2) DEFAULT NULL,
  `OPTION_TYP` varchar(10) DEFAULT NULL,
  `OPEN` double(12,2) DEFAULT NULL,
  `HIGH` double(12,2) DEFAULT NULL,
  `LOW` double(12,2) DEFAULT NULL,
  `CLOSE` double(12,2) DEFAULT NULL,
  `SETTLE_PR` double(12,2) DEFAULT NULL,
  `CONTRACTS` bigint DEFAULT NULL,
  `VAL_INLAKH` double(12,2) DEFAULT NULL,
  `OPEN_INT` bigint DEFAULT NULL,
  `CHG_IN_OI` bigint DEFAULT NULL,
  `TRADING_DATE` varchar(20) DEFAULT NULL,
  KEY `options_data_1` (`OPTION_TYP`),
  KEY `options_data_1222` (`OPTION_TYP`,`SYMBOL`,`EXPIRY_DT`,`TRADING_DATE`),
  KEY `options_data_index_opt_edt_td_s` (`OPTION_TYP`,`EXPIRY_DT`,`TRADING_DATE`,`SYMBOL`),
  KEY `options_data_index_opt_edt_td_ssss` (`OPTION_TYP`,`EXPIRY_DT`,`TRADING_DATE`,`SYMBOL`,`STRIKE_PR`,`OPEN_INT`)
);


CREATE TABLE `bhav_data` (
  `SYMBOL` varchar(40) DEFAULT NULL,
  `SERIES` varchar(10) DEFAULT NULL,
  `DATE1` date DEFAULT NULL,
  `PREV_CLOSE` double(12,2) DEFAULT NULL,
  `OPEN_PRICE` double(12,2) DEFAULT NULL,
  `HIGH_PRICE` double(12,2) DEFAULT NULL,
  `LOW_PRICE` double(12,2) DEFAULT NULL,
  `LAST_PRICE` double(12,2) DEFAULT NULL,
  `CLOSE_PRICE` double(12,2) DEFAULT NULL,
  `AVG_PRICE` double(12,2) DEFAULT NULL,
  `TTL_TRD_QNTY` bigint DEFAULT NULL,
  `TURNOVER_LACS` bigint DEFAULT NULL,
  `NO_OF_TRADES` bigint DEFAULT NULL,
  `DELIV_QTY` bigint DEFAULT NULL,
  `DELIV_PER` double(12,2) DEFAULT NULL,
  UNIQUE KEY `SYMBOL` (`SYMBOL`,`SERIES`,`DATE1`),
  KEY `options_data_bhav_data_index_opt_edt_td_ssss` (`SYMBOL`,`DATE1`)
)