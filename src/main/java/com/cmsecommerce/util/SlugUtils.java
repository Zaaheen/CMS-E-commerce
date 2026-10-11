package com.cmsecommerce.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("-");

        // Chuyển 'đ' và 'Đ' thành 'd'
        slug = slug.replace("đ", "d").replace("Đ", "d");

        return slug.toLowerCase(Locale.ENGLISH);
    }
}
