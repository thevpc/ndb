package net.thevpc.ndb.desktop.util;

import net.thevpc.nuts.util.NStringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomFormatter  extends Formatter {
    private final String format;
    private final String dateFormat;
    private final Date dat = new Date();
    private SimpleDateFormat simpleDateFormat;

    public static CustomFormatter ofTwoLines() {
        return new CustomFormatter("%1$s %2$s%n%4$s: %5$s%6$s%n",null);
    }

    public static CustomFormatter ofOneLine() {
        return new CustomFormatter("[%1$s][%4$s][%2$s] : %5$s%6$s%n",null);
    }

    public CustomFormatter(String format,String dateFormat) {
        this.dateFormat = NStringUtils.firstNonBlank(dateFormat,"yyyy-MM-dd HH:mm:ss");
        this.format = NStringUtils.firstNonBlankTrimmed(format,
                System.getProperty("java.util.logging.SimpleFormatter.format"),
                "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n"
                );
        this.simpleDateFormat = new SimpleDateFormat(this.dateFormat);
    }

    @Override
    public String format(LogRecord record) {
        dat.setTime(record.getMillis());
        String source;
        if (record.getSourceClassName() != null) {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null) {
                source += "::" + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }
        String message = formatMessage(record);
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        return String.format(format,
                simpleDateFormat.format(dat),
                source,
                record.getLoggerName(),
                record.getLevel().getLocalizedName(),
                message,
                throwable);
    }
}
