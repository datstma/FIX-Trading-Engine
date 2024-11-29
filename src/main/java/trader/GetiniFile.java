/* @author Stefan Månsby */
package trader;


//import java.io.*;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import ccbackend.mSockServ;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;



public class GetiniFile
{
    public Date date;
    String HOMEDIR = System.getProperty("user.home") + System.getProperty("file.separator") + "Trader";
   
    public GetiniFile()
    {
    }

    
    public void readinifile(GlobalVars gv){
    	
    	createworkdir();
    	
    	boolean exists = (new File(HOMEDIR + System.getProperty("file.separator") + "Trader.ini")).exists();
    	if (exists) {
    	    // File or directory exists
    		try {
				Wini ini = new Wini(new File(HOMEDIR + System.getProperty("file.separator") + "Trader.ini"));
				String dburl = ini.get("MySQL", "dburl");
				System.out.println(dburl);
				
				
				
			} catch (InvalidFileFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
    		
    		
    		
    		
    	} else {
    	    // File or directory does not exist
    		try {
				FileWriter fw = new FileWriter(HOMEDIR + System.getProperty("file.separator") + "Trader.ini",true);
				fw.write("[Version]\n");
				fw.write("Version = " + gv.VERSION+"\n");
				fw.write("\n");
				fw.write("[MySQL]\n");
				fw.write("dburl = jdbc:mysql://db.green:3306/scraper\n");
				fw.write("dbuser = trader\n");
				fw.write("dbpassword = DATABASEPASSWORD\n");
				fw.write("LIVE_TABLE = Trade_live_POC\n");
				fw.write("\n");
				fw.write("[TimeZones]\n");
				fw.write("FIXGW_TZ = America/New_York\n");
				fw.write("Local_TZ = Europe/Stockholm\n");
				fw.write("\n");
				
				fw.close();
				System.out.println("Please edit your " + HOMEDIR + System.getProperty("file.separator") + "Trader.ini");
				System.exit(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //st pekare
    	}
    	
    	
    	
    }
    
    public synchronized void writelog(String logentry)
    {
        date = new Date();

        //Initsiera loggfilen genom att hmta hemkatalog frn RunMe.class
        try{
            //String homedir = RunMe.HOMEDIR.trim();
            FileWriter fw = new FileWriter(HOMEDIR + System.getProperty("file.separator") + "Trader.log",true); //st pekare
            //FileWriter fw = new FileWriter(homedir + "\\mProbe.log",true); //s pekare
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            //fw.write(date.toString() +": "  + logentry + System.getProperty("line.separator")); //skriv till loggen
            fw.write(formatter.format(date) +": "  + logentry + System.getProperty("line.separator")); //skriv till loggen
            fw.close();
        }catch (Exception e) {System.out.println("Could not open the logfile!\n");}
    } //end public void writelog()

    public void createworkdir(){
        HOMEDIR = System.getProperty("user.home") + System.getProperty("file.separator") + "Trader";
        File filkoll = new File(HOMEDIR);

        if (!filkoll.exists())
        {
            System.out.println("Creating a homedirectory: " + HOMEDIR + "\n");
            try {
                File arbdir = new File(HOMEDIR);
                arbdir.mkdirs();
                //logger = new Logger();
                //logger.writelog("Created initial working directory and logfile.");

            } catch (Exception e)
            {
                System.out.println("Error creating homedir: " + e.toString());
            }
        }
    }
}
