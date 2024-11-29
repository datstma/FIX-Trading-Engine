/* @author Stefan Månsby */
package trader;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class ConvertlTime {

    public String convert(String inputTimeZone, String inputDateFormat, String outputTimeZone, String outputDateFormat, String inputDateString){

        TimeZone est = TimeZone.getTimeZone(inputTimeZone);
        TimeZone cet = TimeZone.getTimeZone(outputTimeZone);


        try
        {
//create SimpleDateFormat object with source string date format
            SimpleDateFormat sdfSource = new SimpleDateFormat(inputDateFormat);
            sdfSource.setTimeZone(est);
//parse the string into Date object
            Date date = sdfSource.parse(inputDateString);

//create SimpleDateFormat object with desired date format
            SimpleDateFormat sdfDestination = new SimpleDateFormat(outputDateFormat);
            sdfDestination.setTimeZone(cet);
//parse the date into another format
            inputDateString = sdfDestination.format(date);

//System.out.println("Converted date is : " + strDate);

        }
        catch(ParseException pe)
        {
            System.out.println("Parse Exception : " + pe);
        }
        return inputDateString;
    }
}
