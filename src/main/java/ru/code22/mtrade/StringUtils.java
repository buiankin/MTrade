package ru.code22.mtrade;

public final class StringUtils {

    static final String removeQuotes(String s)
    {
        return s.replace("\"", "").replace("'", "");
    }
    static final String defaultIfBlank(String s, String s2) {return s==null?s2:s;}
}
