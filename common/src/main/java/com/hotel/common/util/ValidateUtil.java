package com.hotel.common.util;

import java.util.regex.Pattern;

public final class ValidateUtil {
    private ValidateUtil() {}

    private static final Pattern EMAIL =
            Pattern.compile("[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern USERNAME =
            Pattern.compile("^[a-zA-Z0-9._]{1,20}$");
    private static final Pattern PHONE =
            Pattern.compile("\\d{10,15}");
    private static final Pattern CARD =
            Pattern.compile("^(4\\d{15}|5[1-5]\\d{14}|5018\\d{12}|5020\\d{12}|5038\\d{12})$");
    private static final Pattern DATE =
            Pattern.compile("^\\d{2}\\d{2}\\d{4}$"); // DDMMYYYY

    public static boolean isValidEmail(String s)   { return EMAIL.matcher(s).matches(); }
    public static boolean isValidUsername(String s){ return USERNAME.matcher(s).matches(); }
    public static boolean isValidPassword(String s){
        if (s.length() < 7 || s.length() > 20) return false;
        boolean up = false, low = false, dig = false, spec = false;
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) up = true;
            else if (Character.isLowerCase(c)) low = true;
            else if (Character.isDigit(c)) dig = true;
            else spec = true;
        }
        return up && low && dig && spec;
    }
    public static boolean isValidPhone(String s)   { return PHONE.matcher(s).matches(); }
    public static boolean isValidFullName(String s) {
        if (s == null || s.isBlank()) return false;
        return s.codePoints().allMatch(c ->
                Character.isLetter(c) ||
                        Character.isWhitespace(c) ||   // любой пробельный символ
                        c == '-'                      // дефис
        );
    }
    public static boolean isValidCard(String s)    { return CARD.matcher(s).matches(); }
    public static boolean isValidExpiry(String s)  {
        if (s.length() != 5 || s.charAt(2) != '/') return false;
        try {
            int m = Integer.parseInt(s.substring(0,2));
            int y = Integer.parseInt(s.substring(3,5)) + 2000;
            if (m < 1 || m > 12) return false;
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.LocalDate exp = java.time.YearMonth.of(y, m).atEndOfMonth();
            return !exp.isBefore(now);
        } catch (Exception e) { return false; }
    }
    public static boolean isValidCVV(String s)     { return s.length()==3 && s.chars().allMatch(Character::isDigit); }
    public static boolean isValidDate(String s)    {
        if (!DATE.matcher(s).matches()) return false;
        int d = Integer.parseInt(s.substring(0,2)),
                m = Integer.parseInt(s.substring(2,4)),
                y = Integer.parseInt(s.substring(4,8));
        try {
            java.time.LocalDate date = java.time.LocalDate.of(y,m,d);
            return !date.isBefore(java.time.LocalDate.now().plusDays(1));
        } catch (Exception e) { return false; }
    }
}

