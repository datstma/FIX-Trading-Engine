/* @author Stefan Månsby */
package trader;


import java.net.*;   // Contains Socket classes
import java.io.*;    // Contains Input/Output classes
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
//import java.util.logging.Level;
//import javax.net.SocketFactory;
//import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.lang3.StringUtils;


public class FIXmt extends Thread implements Runnable{

    private GlobalVars gv;
    Connection dbcon = null;
    Statement st = null;
    ResultSet rs = null;
    boolean dbisConnected = false;
    Logger logger = new Logger();



    //Inherit super objects
    FIXmt(GlobalVars gv){
        this.gv = gv;


    }

    /**
     * @param args
     */
    /**
    // * @param args the command line arguments
     */
    
    /**  The application processes ORDERS that are placed in table named by gv.LIVE_TABLE. 
     *   In that table it looks for the value "ORDER" in the 'STATUS' column.
     *   Once such a row is found and sanity checked, in changes the STATUS value to "PROC".
     *   It then places a "KOP","SALJ" or "CANCEL" order with the FIX system.
     *   
     *   As order is being processes by the FIX server, the STATUS value is updated by the values given by the FIX server (based on the fix-protocol standards)
     *   A successfull order will end up with the STATUS value of "Filled". 
     *   The prefered STATUS order value chain is: ORDER->PROC->Pending New->New->Filled
     *   
     *   Cancelations are a bit special, we create a new orderid with the same name as the old one that is to be canceled, but we add on a "C" at the end,
     *   just for in-memory tracking. A successfull cancelation has the STATUS value of "Canceled".
     *  
     *  
     *  */


    static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }


    
    ////////////////////////////////////////////////////////////////////////////

    public String genTradingSessionStatusFix(){

        String AccountNumber = "12345678";

        int k=0;int X=0;
        String FIX49="49="+gv.SENDERCOMPID;

        String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";


        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);


        gv.mySequence++;
        //
        String FIX3="35=g" + '\01' + "1="+AccountNumber + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01'+"263=0"+'\01'+"335=123456"+'\01' +"336=GTC"+'\01';


        int FIXLength = FIX3.length();



        FIX4=FIX4 + FIXLength + '\01' + FIX3  ; // FIX Exempel 2



        int FIX4Length = FIX4.length();
        //FIX4Length++;d

        // Create a checksum
        for ( int i = 0; i < FIX4Length; ++i )
        {
            char c = FIX4.charAt( i ); int j = (int) c; k=(k+j);
        }

        //System.out.println("Key: " + k);
        int a = k % 256 ;
        String aa = Integer.toString(a);

        if (a < 100)
        {
            //System.out.println("Fixing Checksum to THREE bytes");
            aa = "0"+ aa;
        }


        //Attach checksum to the end of the final message
        FIX4=FIX4 +  "10="+ aa + '\01';

        System.out.println("Built TSR message: " + FIX4);
        //System.out.println("Our last SEQ:" + mySequence);

        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+gv.mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }


        return (FIX4);


    }

    ////////////////////////////////////////////////////////////////////////////


    public String genHeartBeatFix(){

        String AccountNumber = "12345678";

        int k=0;int X=0;
        String FIX49="49="+gv.SENDERCOMPID;

        String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";


        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);


        gv.mySequence++;
        //
        String FIX3="35=0" + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01';





        int FIXLength = FIX3.length();


        FIX4=FIX4 + FIXLength + '\01' + FIX3  ; // FIX Exempel 2

        int FIX4Length = FIX4.length();
        //FIX4Length++;d

        // Create a checksum
        for ( int i = 0; i < FIX4Length; ++i )
        {
            char c = FIX4.charAt( i ); int j = (int) c; k=(k+j);
        }

        int a = k % 256 ;
        String aa = Integer.toString(a);

        if (a < 100)
        {
            //System.out.println("Fixing Checksum to THREE bytes");
            aa = "0"+ aa;
        }

        //Attach checksum to the end of the final message
        FIX4=FIX4 +  "10="+ aa + '\01';

        System.out.println("Built Hearbeat response message: " + FIX4);
        //System.out.println("Our last SEQ:" + mySequence);

        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+gv.mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }


        return (FIX4);


    }

    ////////////////////////////////////////////////////////////////////////////

    public void dbConnect() {
        Logger logger = new Logger();
        logger.writelog("");
        logger.writelog("Fix thread trying to load JDBC (org.gjt.mm.mysql.Driver) driver...");
        try{            //full path to JDBC Driver
            Class.forName("org.gjt.mm.mysql.Driver").newInstance();
            logger.writelog("Fix thread Loading JDBC driver went fine.");
            logger.writelog("Fix thread Connecting to the database: " + gv.dburl);
            dbcon = DriverManager.getConnection(gv.dburl, gv.dbuser, gv.dbpassword);
            logger.writelog("Fix thread Database connection went fine.");
            logger.writelog("Fix db connection proof: "+dbcon.toString());
            dbisConnected = true;

            //System.out.println("done!");
        } catch (Exception e){
            logger.writelog("Error: " + e.toString());
            System.out.println("\nError: " + e.toString());
            logger.writelog("Warning! The database connection has NOT been established!\n");
            dbisConnected = false;
        }


    }

    ////////////////////////////////////////////////////////////////////////////


    public String checkForNewOrder(){

        String returnliveid = "";

        try{
            Statement st2 = dbcon.createStatement();
            ResultSet rs2 = null;


            st = dbcon.createStatement();
            //Get the oldest order row
            rs = st.executeQuery("SELECT * FROM "+gv.LIVE_TABLE+" WHERE STATUS = 'ORDER' AND Side IS NOT null ORDER BY idTrade_live ASC LIMIT 1");


            while (rs.next ()) {

                String ID = "NULL";
                String TYP = "NULL";
                String OTYP = "NULL";
                String Side = "NULL";
                String OrdType = "NULL";
                String OrderQty = "NULL";
                String VARDE = "NULL";
                String KONTRAKT = "NULL";
                String idTrade_live = "NULL";

                //  System.out.println("Order found:");
                ID = rs.getString("ID");
                //  System.out.println("ID:"+ID);
                TYP = rs.getString("TYP");
                //  System.out.println("TYP:"+TYP);
                OTYP = rs.getString("OTYP");
                //  System.out.println("OTYP:"+OTYP);
                Side = rs.getString("Side");
                //  System.out.println("Side:"+Side);
                OrdType = rs.getString("OrdType");
                //  System.out.println("OrdType:"+OrdType);
                OrderQty = rs.getString("OrderQty");
                //  System.out.println("OrderQty:"+OrderQty);
                VARDE = rs.getString("VARDE");
                //  System.out.println("VARDE:"+VARDE);
                KONTRAKT = rs.getString("KONTRAKT");
                //  System.out.println("KONTRAKT:"+KONTRAKT);
                idTrade_live = rs.getString("idTrade_live");
                //  System.out.println("idTrade_live:"+idTrade_live);


                if (StringUtils.equalsIgnoreCase(TYP, "KOP") && StringUtils.isNumeric(ID) && !StringUtils.equalsIgnoreCase(ID, "NULL") && !StringUtils.equalsIgnoreCase(ID, null) && !StringUtils.equalsIgnoreCase(Side, "NULL") && !StringUtils.equalsIgnoreCase(Side, null)       )
                {
                    System.out.println("Starting KOP process for: " + ID);
                    System.out.println("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;
                    //return ID,TYP,OTYP,Side,OrdType,OrderQty,VARDE,KONTRAKT,idTrade_live;
                    returnliveid = idTrade_live + " KOP";
                    //return returnArray;


                }

                if (StringUtils.equalsIgnoreCase(TYP, "SALJ") && StringUtils.isNumeric(ID) && !StringUtils.equalsIgnoreCase(ID, "NULL") && !StringUtils.equalsIgnoreCase(ID, null) && !StringUtils.equalsIgnoreCase(Side, "NULL") && !StringUtils.equalsIgnoreCase(Side, null)       )
                {
                    System.out.println("Starting SALJ process for: " + ID);
                    System.out.println("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;
                    //return ID,TYP,OTYP,Side,OrdType,OrderQty,VARDE,KONTRAKT,idTrade_live;
                    //return returnArray;
                    returnliveid = idTrade_live + " SALJ";

                }

                if (StringUtils.equalsIgnoreCase(TYP, "CANCEL") && StringUtils.isNumeric(ID) && !StringUtils.equalsIgnoreCase(ID, "NULL") && !StringUtils.equalsIgnoreCase(ID, null) && !StringUtils.equalsIgnoreCase(Side, "NULL") && !StringUtils.equalsIgnoreCase(Side, null)       )
                {
                    System.out.println("Starting CANCEL process for: " + ID);
                    System.out.println("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;
                    //return ID,TYP,OTYP,Side,OrdType,OrderQty,VARDE,KONTRAKT,idTrade_live;
                    //return returnArray;
                    returnliveid = idTrade_live + " CANCEL";

                }



            }

        }catch (Exception e){
            System.out.println("checkForNewOrder Db stuff crap");
            System.out.println(e.toString());
            //System.out.print(e.printStackTrace());
            e.printStackTrace();
        }


        return returnliveid;

    }


public void setOrderStatusToLive(String ID){

try{
Statement st2 = dbcon.createStatement();


System.out.println("Setting STATUS for "+ ID +" to LIVE");
int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='LIVE' WHERE ID='"+ID+"'") ;

}catch (Exception e){
System.out.println("checkForNewOrder Db stuff crap");
System.out.println(e.toString());
//System.out.print(e.printStackTrace());
e.printStackTrace();
}

}



public void setOrderStatus(String ID, String Status){

try{
Statement st2 = dbcon.createStatement();


System.out.println("Setting STATUS for "+ ID +" to " + Status);
int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='"+Status+"' WHERE ID='"+ID+"'") ;

}catch (Exception e){
System.out.println("setOrderStatus Db stuff crap");
System.out.println(e.toString());
//System.out.print(e.printStackTrace());
e.printStackTrace();
}


//return returnliveid;

}


    
    public String FixLookup(String Req){
        String Response = "";

        //I just realized that this one is probably pointless :-)



        return Response;
    }



    public void run() {
        // TODO code application logic here
        logger.writelog("Starting Fix thread named: " + Thread.currentThread().getName());
        dbConnect();

        try{
//



            logger.writelog("Connecting to " + gv.OrderHost + " on port " + gv.OrderPort);


            gv.client=new Socket(gv.OrderHost, gv.OrderPort);


            //gv.client.setKeepAlive(true);


            ProcessFromFIX pffThrd = new ProcessFromFIX(gv, gv.client);
            pffThrd.setName("The ProcessFromFIX thread");
            SendToFIX stfThrd = new SendToFIX(gv, gv.client);
            stfThrd.setName("The SendToFIX thread");


            //Split read and write into two separate threads
            pffThrd.start();
            stfThrd.start();
            

            pffThrd.join();


        }

        catch(Exception e){

            logger.writelog("FIXmt Connection to FIX server "+gv.OrderHost+" failed, reason: "  +e.getMessage());




        }




    }



}


 



