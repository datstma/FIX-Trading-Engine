/* @author Stefan Månsby */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class GlobalVars {

    public String VERSION = "0.4.28";

    protected String DataBaseServername = "";
    protected String LastMessageFromFIX = "";

    //MBQuote variables

    public DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String MBusername = "YOUR_USERNAME";
    public static String MBpassword = "YOUR_PASSWORD";
    public static int MBquoteport  = 5020;
    
    public String OrderHost = "TRADING_HOUSE_HOSTNAME OR IP";
    public int OrderPort = 5679;
    public String TargetCompID = "TRADING_HOUSE_ID";
    public String SENDERCOMPID = "YOUR_TRADING_ID";
    public String PASSWORDclear = "12345678";
    public String PASSWORD = "SHA256 PASSWORD";
    public String AccountNumber = "12345678";
    public int serverSequence = 1;
    public int mySequence = 1;
    public int HEARTBEATCOUNTER = 0;
    public boolean SENTMASSSTATUSREQ = false;
    protected BufferedWriter writer;

    public boolean dbisConnected = false;
    public int messageCounter ;
    Socket client;
    protected boolean readytoTrade = false;
    protected boolean fixprocessingorder = false;

    protected String fixOrderQueue = "";
    public int SpeedBrake = 1;


    //MySQL variables
    Connection dbcon = null;
    Statement st = null;
    ResultSet rs = null;

    public String dburl = "jdbc:mysql://HOSTNAME:3306/DATABASENAME";
    public String dbuser = "trader";
    public String dbpassword = "DBPASSWORD";
    public String LIVE_TABLE = "Trade_live_POC";



    TimeZone est = TimeZone.getTimeZone("America/New_York");
    TimeZone cet = TimeZone.getTimeZone("Europe/Stockholm");





    public synchronized void setreadytoTrade(boolean input) throws InterruptedException
    {
        //DataBaseServername = input;
        readytoTrade = input;
    }

    public synchronized boolean getreadytoTrade() throws InterruptedException
    {
        //return DataBaseServername;
        return(readytoTrade);
    }
    
    public synchronized void setLastMessageFromFIX(String message) throws InterruptedException
    {
        
        LastMessageFromFIX = message;
    }

    public synchronized String getLastMessageFromFIX() throws InterruptedException
    {
        //return DataBaseServername;
        return(LastMessageFromFIX);
    }


    public synchronized String getfixOrderQueue() throws InterruptedException
    {
        //DataBaseServername = input;
        return fixOrderQueue;
    }

    public synchronized void addtofixOrderQueue(String input) throws InterruptedException
    {
        fixOrderQueue = fixOrderQueue + ";" + input;
    }

    public synchronized void removefromfixOrderQueue(String input) throws InterruptedException
    {
        fixOrderQueue = fixOrderQueue + ";" + input;
    }

    public synchronized void sendMSGToFIX(String message) throws InterruptedException
    {

        /* LastMessageFromFIX = message; */
        try {
			writer.write(message);
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


}
