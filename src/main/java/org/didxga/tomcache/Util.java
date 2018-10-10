package org.didxga.tomcache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    //Sunday, 06-Nov-94 08:49:37 GMT
    private static Pattern expires_format_one = Pattern.compile("(Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday)\\s*,\\s+([0-9]{2,2})-(\\w{3,3})-([0-9]{2,2})\\s+([0-9]{2,2}):([0-9]{2,2}):([0-9]{2,2})");
    //Sun, 06 Nov 1994 08:49:37 GMT
    private static Pattern expires_format_two = Pattern.compile("(Sun|Mon|Tue|Wed|Thu|Fri|Sat)\\s*,\\s+([0-9]{2,2})\\s+(\\w{3,3})\\s+([0-9]{4,4})\\s+([0-9]{2,2}):([0-9]{2,2}):([0-9]{2,2})");
    //Sun Nov  6 08:49:37 1994
    private static Pattern expires_format_three = Pattern.compile("(Sun|Mon|Tue|Wed|Thu|Fri|Sat)\\s+(\\w{3,3})\\s+([0-9]{1,2})\\s+([0-9]{2,2}):([0-9]{2,2}):([0-9]{2,2})\\s+([0-9]{4,4})");

    public static String getURI(HttpServletRequest req) {
        return req.getRequestURI();
    }

    public static Date getExpirationDate(HttpServletResponse resp) {
        String cache_control = resp.getHeader("Cache-Control");
        String[] cache_operands;
        //use Cache-Control if it presents else use Expires, as Cache-Control takes precedence to Expires
        if(cache_control!=null) {
            cache_operands = cache_control.split(",");
            for (String str : cache_operands) {
                if (str.equals("no-cache")) {
                    return null;
                }
                else if (str.startsWith("max-age=")) {
                    return getDateBy(str.split("=")[1]);
                }
            }
        } else {
            String expires = resp.getHeader("Expires");
            return getExpiresDate(expires);
        }

        return null;
    }


    private static Date getDateBy(String sec) {
        int timeSec = Integer.valueOf(sec);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, timeSec);
        return calendar.getTime();
    }

    /**
     * This method extract the expire date from Expires header
     * According to the spec rfc7231 (https://tools.ietf.org/html/rfc7231#section-7.1.1.1)
     * quote from the spec:
     * <blockquote>
     * Prior to 1995, there were three different formats commonly used by
     *    servers to communicate timestamps.  For compatibility with old
     *    implementations, all three are defined here.  The preferred format is
     *    a fixed-length and single-zone subset of the date and time
     *    specification used by the Internet Message Format [RFC5322].
     *
     *      HTTP-date    = IMF-fixdate / obs-date
     *
     *    An example of the preferred format is
     *
     *      Sun, 06 Nov 1994 08:49:37 GMT    ; IMF-fixdate
     *
     *    Examples of the two obsolete formats are
     *
     *      Sunday, 06-Nov-94 08:49:37 GMT   ; obsolete RFC 850 format
     *      Sun Nov  6 08:49:37 1994         ; ANSI C's asctime() format
     * </blockquote>
     *
     * @param dateStr
     * @return Date
     */
    private static Date getExpiresDate(String dateStr) {
        int dayOfMonth;
        int month;
        int year;
        int hour;
        int minute;
        int second;
        if(dateStr.indexOf("-") > -1) {
            //Sunday, 06-Nov-94 08:49:37 GMT
            Matcher matcher = expires_format_one.matcher(dateStr);
            dayOfMonth = Integer.valueOf(matcher.group(2));
            month = Integer.valueOf(matcher.group(3));
            year = Integer.valueOf(matcher.group(4));
            hour = Integer.valueOf(matcher.group(5));
            minute = Integer.valueOf(matcher.group(6));
            second = Integer.valueOf(matcher.group(7));
        }
        else if(dateStr.indexOf(",") > -1) {
            //Sun, 06 Nov 1994 08:49:37 GMT
            Matcher matcher = expires_format_two.matcher(dateStr);
            dayOfMonth = Integer.valueOf(matcher.group(2));
            month = Integer.valueOf(matcher.group(3));
            year = Integer.valueOf(matcher.group(4));
            hour = Integer.valueOf(matcher.group(5));
            minute = Integer.valueOf(matcher.group(6));
            second = Integer.valueOf(matcher.group(7));
        }
        else {
            //Sun Nov  6 08:49:37 1994
            Matcher matcher = expires_format_three.matcher(dateStr);
            dayOfMonth = Integer.valueOf(matcher.group(2));
            month = Integer.valueOf(matcher.group(3));
            year = Integer.valueOf(matcher.group(4));
            hour = Integer.valueOf(matcher.group(5));
            minute = Integer.valueOf(matcher.group(6));
            second = Integer.valueOf(matcher.group(7));
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, hour, minute, second);
        return calendar.getTime();
    }

 }
