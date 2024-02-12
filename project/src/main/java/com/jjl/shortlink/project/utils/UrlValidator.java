package com.jjl.shortlink.project.utils;

import java.util.regex.Pattern;

public class UrlValidator {
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^((http|https)://)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(/[a-zA-Z0-9-._~:/?#\\[\\]@!$&'()*+,;=]*)?$"
    );

    public static boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }
}