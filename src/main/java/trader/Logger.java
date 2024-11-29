/* @author Stefan Månsby */
package trader;


//import java.io.*;
import java.io.FileWriter;
import java.io.File;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Logger
{
    public Date date;
    String HOMEDIR = System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "Trader";
    public Logger()
    {
    }

    public synchronized void writelog(String logentry)
    {
        date = new Date();

        //Initsiera loggfilen genom att hmta hemkatalog frn RunMe.class
        try{
            //String homedir = RunMe.HOMEDIR.trim();
            FileWriter fw = new FileWriter(HOMEDIR + FileSystems.getDefault().getSeparator() + "Trader.log",true); //st pekare
            //FileWriter fw = new FileWriter(homedir + "\\mProbe.log",true); //s pekare
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            //fw.write(date.toString() +": "  + logentry + System.getProperty("line.separator")); //skriv till loggen
            fw.write(formatter.format(date) +": "  + logentry + System.lineSeparator()); //skriv till loggen
            fw.close();
        }catch (Exception e) {System.out.println("Could not open the logfile!\n");}
    } //end public void writelog()

    public void createworkdir(){
        HOMEDIR = System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "Trader";
        File filkoll = new File(HOMEDIR);

        if (!filkoll.exists())
        {
            System.out.println("Creating a homedirectory: " + HOMEDIR + "\n");
            try {
                File arbdir = new File(HOMEDIR);
                arbdir.mkdirs();

            } catch (Exception e)
            {
                System.out.println("Error creating homedir: " + e);
            }
        }
    }
}
