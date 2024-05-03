package com.nse.utils.file;

import org.springframework.util.StringUtils;

public class CommonUtils {

    public static Long toLong(String value) {
        try {
            return StringUtils.hasLength(value) ? Long.parseLong(value.trim()) : 0l;
        } catch (Exception e) {
            e.getMessage();
        }
        return 0l;
    }

    public static Double toDouble(String value) {
        try {
            return StringUtils.hasLength(value) ? Double.parseDouble(value.trim()) : 0.0;
        } catch (Exception e) {
            e.getMessage();
        }
        return 0.0;
    }
}
