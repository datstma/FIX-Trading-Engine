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


public class Fix extends Thread{

    private GlobalVars gv;
    Connection dbcon = null;
    Statement st = null;
    ResultSet rs = null;
    boolean dbisConnected = false;
    Logger logger = new Logger();



    //Inherit super objects
    Fix(GlobalVars gv){
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

    static String QuoteHost = "YOUR_GATE_HOST_OR_IP_HERE";
    static int QuotePort = 11506;

    static String OrderHost = "123.123.123.132/HOSTNAME";
    static int OrderPort = 5679;

    static String TargetCompID = "TRADINGHOUSEID";
    static String SENDERCOMPID = "YOURCOMPANYFIXID";
    static String PASSWORDclear = "12345678";
    static String PASSWORD = "INSERT_PASSWORD_HERE_BUT_MOVE_TO_SAFE_PLACE_WHEN_IN_PROD";
    static int serverSequence = 1;
    static int mySequence = 1;



    public String genLoginfix(){

        int k=0;
    	//int X=0;
        String FIX49="49="+SENDERCOMPID;

        //String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";


        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);


        String FIX3="35=A" + '\01' + "141=Y" + '\01' + "34="+ mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+TargetCompID+'\01'+"98=0"+'\01'+"108=28"+'\01' +"347=554_H1"+'\01'+ "554="+PASSWORD+'\01';


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

        System.out.println("Built LOGIN message: " + FIX4);

        //Write the message in the log_table for safe keeping
        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }

        return (FIX4);


    }

    public String genTestOrderfix(){

        String AccountNumber = "12345678";

        int k=0;
        //int X=0;
        String FIX49="49="+SENDERCOMPID;

        //String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";


        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);

        mySequence++;
        //
        String FIX3="35=D" + '\01' + "1="+AccountNumber + '\01' + "34="+ mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+TargetCompID+'\01'+"11=2012080701"+'\01'+"21=1"+'\01' +"38=1"+'\01'+"40=2"+'\01'+"44=1390.00"+'\01'+"54=1"+'\01'+"55=/ESU2"+'\01'+"59=0"+'\01'+"60="+datenowUTC+'\01'+"100=MBTX"+'\01'+"553="+SENDERCOMPID+'\01'+"47=A"+'\01';


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

        System.out.println("Built TEST ORDER message: " + FIX4);
        //System.out.println("Our last SEQ:" + mySequence);

        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }

        return (FIX4);


    }



    public String genOrderfix(String TradeLiveRowID){

        String ID = "NULL";
        String TYP = "NULL";
        String OTYP = "NULL";
        String Side = "NULL";
        String OrdType = "NULL";
        String OrderQty = "NULL";
        String VARDE = "NULL";
        String KONTRAKT = "NULL";
        String idTrade_live = "NULL";

        try{
            Statement st2 = dbcon.createStatement();
            ResultSet rs2 = null;

            st = dbcon.createStatement();
            //Get the oldest order row
            rs = st.executeQuery("SELECT * FROM "+gv.LIVE_TABLE+" WHERE idTrade_live = '"+TradeLiveRowID+"'");


            while (rs.next ()) {

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

            }

        }catch (Exception e){
            System.out.println("genOrderfix Db stuff crap");
            System.out.println(e.toString());
            e.printStackTrace();
        }


        Double ActualVarde = Double.parseDouble(VARDE);
        ActualVarde = ActualVarde / 100;

        String AccountNumber = "12345678";

        int k=0;int X=0;
        String FIX49="49="+SENDERCOMPID;

        String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";

        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);

        mySequence++;
        //
        String FIX3="35=D" + '\01' + "1="+AccountNumber + '\01' + "34="+ mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+TargetCompID+'\01'+"11="+ID+'\01'+"21=1"+'\01' +"38="+OrderQty+'\01'+"40="+OrdType+'\01'+"44="+ActualVarde+'\01'+"54="+Side+'\01'+"55="+KONTRAKT+'\01'+"59=0"+'\01'+"60="+datenowUTC+'\01'+"100=MBTX"+'\01'+"553="+SENDERCOMPID+'\01'+"47=A"+'\01';


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

        System.out.println("Built ORDER message: " + FIX4);
        //System.out.println("Our last SEQ:" + mySequence);
        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }

        return (FIX4);


    }

    ////////////////////////////////////////////////////////////////////////////

    public String genCancelOrderfix(String TradeLiveRowID){
        //8=FIX.4.49=15535=A141=Y34=149=DEMOZGBXFIX52=20120807-17:54:48.68856=MBT98=0108=30347=554_H1554=MASSAHASH10=161

        String ID = "NULL";
        String TYP = "NULL";
        String OTYP = "NULL";
        String Side = "NULL";
        String OrdType = "NULL";
        String OrderQty = "NULL";
        String VARDE = "NULL";
        String KONTRAKT = "NULL";
        //String idTrade_live = "NULL";

        try{
            Statement st2 = dbcon.createStatement();
            ResultSet rs2 = null;


            st = dbcon.createStatement();
            //Get the oldest order row
            rs = st.executeQuery("SELECT * FROM "+gv.LIVE_TABLE+" WHERE idTrade_live = '"+TradeLiveRowID+"'");


            while (rs.next ()) {


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

            }

        }catch (Exception e){
            System.out.println("genOrderfix Db stuff crap");
            System.out.println(e.toString());
            //System.out.print(e.printStackTrace());
            e.printStackTrace();
        }

        Double ActualVarde = Double.parseDouble(VARDE);
        ActualVarde = ActualVarde / 100;

        String AccountNumber = "12345678";

        int k=0;int X=0;
        String FIX49="49="+SENDERCOMPID;

        String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";


        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);


        mySequence++;
        //
        String FIX3="35=F" + '\01' + "1="+AccountNumber + '\01' + "34="+ mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+TargetCompID+'\01'+"11="+ID+"C"+'\01'+"41="+ID+'\01'+"54="+Side+'\01'+"55="+KONTRAKT+'\01'+"60="+datenowUTC+'\01'+"553="+SENDERCOMPID+'\01';
        //String FIX3="35=D" + '\01' + "1="+AccountNumber + '\01' +  FIX49 + '\01' + "52="+datenow +'\01'+"56="+TargetCompID+'\01'+"11=2012080701"+'\01'+"21=1"+'\01' +"38=1"+'\01'+"40=2"+'\01'+"44=1390.00"+'\01'+"54=1"+'\01'+"55=/ESU2"+'\01'+"59=0"+'\01'+"60="+datenowUTC+'\01'+"100=MBTX"+'\01'+"553=TSH77OS"+'\01';


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

        System.out.println("Built CANCEL message: " + FIX4);

        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }

        return (FIX4);


    }

    ////////////////////////////////////////////////////////////////////////////

    public String genTradingSessionStatusFix(){

        String AccountNumber = "12345678";

        int k=0;int X=0;
        String FIX49="49="+SENDERCOMPID;

        String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";


        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);

        mySequence++;
        //
        String FIX3="35=g" + '\01' + "1="+AccountNumber + '\01' + "34="+ mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+TargetCompID+'\01'+"263=0"+'\01'+"335=123456"+'\01' +"336=GTC"+'\01';


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
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }

        return (FIX4);

    }

    ////////////////////////////////////////////////////////////////////////////

    public String genMassStatusRequestFix(){

        String AccountNumber = "12345678";

        int k=0;int X=0;
        String FIX49="49="+SENDERCOMPID;

        String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";


        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);


        mySequence++;
        //
        String FIX3="35=AF" + '\01' + "1="+AccountNumber + '\01' + "34="+ mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+TargetCompID+'\01'+"17=1"+'\01'+"584=123456"+'\01' +"585=7"+'\01';


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

        System.out.println("Built MSR message: " + FIX4);
        //System.out.println("Our last SEQ:" + mySequence);

        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }

        return (FIX4);

    }

    ////////////////////////////////////////////////////////////////////////////

    public String genResendResponseFix(){

        String AccountNumber = "12345678";

        int k=0;int X=0;
        String FIX49="49="+SENDERCOMPID;

        String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";

        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);

        mySequence++;
        //
        String FIX3="35=4" + '\01' + "34="+ mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+TargetCompID+'\01';



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

        System.out.println("Built Type2 response message: " + FIX4);
        //System.out.println("Our last SEQ:" + mySequence);

        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }

        return (FIX4);


    }



    public String genHeartBeatResponseFix(String TestReqID){

        String AccountNumber = "12345678";

        int k=0;int X=0;
        String FIX49="49="+SENDERCOMPID;

        String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";
        //int FIXSequence = 1;

        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);


        mySequence++;
        //
        String FIX3="35=0" + '\01' + "112="+TestReqID+'\01' + "34="+ mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+TargetCompID+'\01';


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

        System.out.println("Built Hearbeat response message: " + FIX4);
        //System.out.println("Our last SEQ:" + mySequence);

        //Write the message in the log_table for safe keeping
        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }

        return (FIX4);


    }
    ////////////////////////////////////////////////////////////////////////////

    public String genHeartBeatFix(){

        String AccountNumber = "12345678";

        int k=0;int X=0;
        String FIX49="49="+SENDERCOMPID;

        String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";


        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);


        mySequence++;
        //
        String FIX3="35=0" + '\01' + "34="+ mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+TargetCompID+'\01';



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


        //System.out.println("Checksum: " + a);

        //Attach checksum to the end of the final message
        FIX4=FIX4 +  "10="+ aa + '\01';

        System.out.println("Built Hearbeat response message: " + FIX4);
        //System.out.println("Our last SEQ:" + mySequence);

        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }

        return (FIX4);

    }


    public void dbConnect() {
        Logger logger = new Logger();
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
                    returnliveid = idTrade_live + " KOP";


                }

                if (StringUtils.equalsIgnoreCase(TYP, "SALJ") && StringUtils.isNumeric(ID) && !StringUtils.equalsIgnoreCase(ID, "NULL") && !StringUtils.equalsIgnoreCase(ID, null) && !StringUtils.equalsIgnoreCase(Side, "NULL") && !StringUtils.equalsIgnoreCase(Side, null)       )
                {
                    System.out.println("Starting SALJ process for: " + ID);
                    System.out.println("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;

                    returnliveid = idTrade_live + " SALJ";

                }

                if (StringUtils.equalsIgnoreCase(TYP, "CANCEL") && StringUtils.isNumeric(ID) && !StringUtils.equalsIgnoreCase(ID, "NULL") && !StringUtils.equalsIgnoreCase(ID, null) && !StringUtils.equalsIgnoreCase(Side, "NULL") && !StringUtils.equalsIgnoreCase(Side, null)       )
                {
                    System.out.println("Starting CANCEL process for: " + ID);
                    System.out.println("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;

                    returnliveid = idTrade_live + " CANCEL";

                }


            }

        }catch (Exception e){
            System.out.println("checkForNewOrder Db stuff crap");
            System.out.println(e.toString());

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



}


    
    
    public String FixLookup(String Req){
        String Response = "";

        //I just realized that this one is probably pointless :-)



        return Response;
    }



    public void run() {
        // TODO code application logic here
        System.out.println("Starting Fix thread named: " + Thread.currentThread().getName());
        dbConnect();

        String Hello = genLoginfix();


        try{

            System.out.println("Keystore: " +System.getProperty("javax.net.ssl.keyStore"));


            System.out.println("Trying to connect to " + OrderHost + " on port " + OrderPort);
            gv.client=new Socket(OrderHost, OrderPort);


            //gv.client.setKeepAlive(true);


            System.out.println("Connected to server " + gv.client.getInetAddress()
                    + ": " + gv.client.getPort());
            System.out.println("local port is " + gv.client.getLocalPort());

            BufferedReader kbreader;
            BufferedWriter writer;
            BufferedReader reader;

            kbreader = new BufferedReader(new InputStreamReader(System.in));
            writer = new BufferedWriter(new OutputStreamWriter(gv.client.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(gv.client.getInputStream()));

            String data = "", datakb, line=null;

            System.out.println("Sending Logon message to server");
            writer.write(Hello);
            writer.newLine();
            writer.flush();

            System.out.println("Sent intial hello to the server...");

            System.out.println("Received from the Server:  ");

            int c=0;

            boolean ENDOFLINEfound = false;
            boolean HEADERfound = false;
            int LASTChar = 0;
            String LASTSEVEN = "";
            String THELINE = "";
            boolean SENTMASSSTATUSREQ = false;
            int HEARTBEATCOUNTER = 0;


            //The reader loop
            while ((c = reader.read()) != -1){


                THELINE = THELINE + (char)c;


                if (THELINE.length() > 7){
                    String MSG = "";
                    LASTSEVEN = THELINE.substring(THELINE.length() - 7);
                    //  System.out.println("Last 7: " + LASTSEVEN);
                    MSG = LASTSEVEN.substring(0,3);
                    // System.out.println("MSG: " + MSG);


                    //If the last 7 chars in message begins with 10=, that means that we have a complete message
                    if (MSG.equalsIgnoreCase("10=")){


                        String THEMESSAGEARRAY[] = THELINE.split("\01");

                        //System.out.println(THEMESSAGE);
                        System.out.println(Arrays.toString(THEMESSAGEARRAY));


                        //Begin processing
                        String MSGTYPE = "";
                        String TSTMSGID = "";
                        String ORDSTATUS = "";
                        String CIOrdID = "";
                        String TXT = "";
                        String CIOrdIDtobeCancelled = "";
                        for (int i = 0; i < THEMESSAGEARRAY.length; i++)
                        {


                            //Check FIX Version
                            if (THEMESSAGEARRAY[i].startsWith("8=")){
                                String FIXVER = THEMESSAGEARRAY[i].substring(2, THEMESSAGEARRAY[i].length() );
                                //System.out.println("GOT FIX ver.: " + FIXVER);
                            }

                            //Check MSGTYPE
                            if (THEMESSAGEARRAY[i].startsWith("35=")){
                                MSGTYPE = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                                System.out.println("GOT MSG type: " + MSGTYPE + " (A=Logon, h=Status, 5=Logout, 0=Heartbeat, 1=TestMessage)");

                            }

                            //Check SEQUENCE
                            if (THEMESSAGEARRAY[i].startsWith("34=")){
                                String SEQUENCE = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                                serverSequence = Integer.parseInt(SEQUENCE);
                                //System.out.println("GOT SEQUENCE: " + SEQUENCE);
                            }

                            //Check SEQUENCE
                            if (THEMESSAGEARRAY[i].startsWith("52=")){
                                String GOTTIMESTAMP = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                                ConvertlTime convertltime = new ConvertlTime();
                                String inLocalTime = convertltime.convert("UTC", "yyyyMMdd-HH:mm:ss.SSS", "Europe/Stockholm", "yyyy-MM-dd HH:mm:ss", GOTTIMESTAMP);
                                //System.out.println("GOT TIMESTAMP: " + GOTTIMESTAMP + " (L:" + inLocalTime);
                            }

                            //Check TXT
                            if (THEMESSAGEARRAY[i].startsWith("58=")){
                                TXT = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                                System.out.println("GOT Text: " + TXT);
                            }

                            //Check Orderstatus
                            if (THEMESSAGEARRAY[i].startsWith("39=")){
                                ORDSTATUS = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                                System.out.println("GOT Order Status: " + ORDSTATUS);
                            }

                          //Check CIOrdOID which is in the process of being canceled
                            if (THEMESSAGEARRAY[i].startsWith("41=")){
                                CIOrdIDtobeCancelled = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                                System.out.println("GOT Order to be Canceled: " + ORDSTATUS);
                            }
                            
                            //Check Orderstatus
                            if (THEMESSAGEARRAY[i].startsWith("11=")){
                                CIOrdID = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                                System.out.println("GOT Order id: " + CIOrdID);
                            }

                            //Check SEQUENCE
                            if (THEMESSAGEARRAY[i].startsWith("112=")){
                                TSTMSGID = THEMESSAGEARRAY[i].substring(4, THEMESSAGEARRAY[i].length() );
                                System.out.println("GOT Test MSG ID: " + TSTMSGID);
                            }

                            //Check Trade session status
                            if (THEMESSAGEARRAY[i].startsWith("340=")){
                                String TRDSTAT = THEMESSAGEARRAY[i].substring(4, THEMESSAGEARRAY[i].length() );
                                System.out.println("GOT TradingSessionStatus: " + TRDSTAT +"(2=Open, 3=Closed, 0=Unknown)");
                            }


                        } //End processing of message



                        //Clear the THELINE variable to start working on the next message
                        THELINE = "";


                        if (MSGTYPE.equalsIgnoreCase("2")){
                            System.out.println("Got Resend request, sending confirmation");
                            writer.write(genResendResponseFix());
                            writer.newLine();
                            writer.flush();
                        }

                        if (MSGTYPE.equalsIgnoreCase("1")){
                            System.out.println("Got a Test message, sending HeartBeat response");
                            writer.write(genHeartBeatResponseFix(TSTMSGID));
                            writer.newLine();
                            writer.flush();
                            HEARTBEATCOUNTER++;
                        }

                        if (MSGTYPE.equalsIgnoreCase("3")){
                            System.out.println("Got a REJECT! Reason: " + TXT);

                        }

                        //* Handle Execution Reports*/
                        if (MSGTYPE.equalsIgnoreCase("8")){
                            System.out.print("Got a Execution Report: ");
                            if (ORDSTATUS.contentEquals("A")) {
                            	System.out.println("Pending New");
                            	setOrderStatus(CIOrdID,"Pending New");}
                            if (ORDSTATUS.contentEquals("B")) {
                            	System.out.println("Calculated");
                            	setOrderStatus(CIOrdID,"Calculated");}
                            if (ORDSTATUS.contentEquals("C")) {
                            	System.out.println("Expired");
                            	setOrderStatus(CIOrdID,"Expired");}
                            if (ORDSTATUS.contentEquals("D")) {
                            	System.out.println("Accepted for Bidding");
                            	setOrderStatus(CIOrdID,"Accepted for Bidding");}
                            if (ORDSTATUS.contentEquals("E")) {
                            	System.out.println("Pending Replace (i.e. result of Order Cancel/Replace Request)");
                            	setOrderStatus(CIOrdID,"Pending Replace");}
                            if (ORDSTATUS.contentEquals("0")) {
                            	System.out.println("New");
                            	setOrderStatus(CIOrdID,"New");}
                            if (ORDSTATUS.contentEquals("1")) {
                            	System.out.println("Partially filled");
                            	setOrderStatus(CIOrdID,"Partially filled");}
                            if (ORDSTATUS.contentEquals("2")) {
                            	System.out.println("Filled"); 
                            	setOrderStatus(CIOrdID,"Filled");}
                            if (ORDSTATUS.contentEquals("3")) {
                            	System.out.println("Done for day");
                            	setOrderStatus(CIOrdID,"Done for day");}
                            if (ORDSTATUS.contentEquals("4")) {
                            	System.out.println("Canceled");
                            	setOrderStatus(CIOrdIDtobeCancelled,"Canceled");}
                            if (ORDSTATUS.contentEquals("5")) {
                            	System.out.println("Replaced (No longer used)");}
                            if (ORDSTATUS.contentEquals("6")) {
                            	System.out.println("Pending Cancel (i.e. result of Order Cancel Request)");
                            	setOrderStatus(CIOrdID,"Pending Cancel");}
                            if (ORDSTATUS.contentEquals("7")) {
                            	System.out.println("Stopped");
                            	setOrderStatus(CIOrdID,"Stopped");}
                            if (ORDSTATUS.contentEquals("8")) {
                            	System.out.println("Rejected");
                            	setOrderStatus(CIOrdID,"Rejected");}
                            if (ORDSTATUS.contentEquals("9")) {
                            	System.out.println("Suspended");
                            	setOrderStatus(CIOrdID,"Suspended");}

                            if (ORDSTATUS.contentEquals("I")) {
                                System.out.println("End of MassStatus Request");
                                System.out.println("Setting status READY TO TRADE true");
                                gv.setreadytoTrade(true);

                            }

                        }


                        //If the system is ready for trade, check for next order to process
                        if (gv.getreadytoTrade())
                        {
                            //Check for new oldest unprocessed order
                            String neworderdata = checkForNewOrder();
                            String neworderliveid = StringUtils.substringBefore(neworderdata, " ");
                            String newordertype = StringUtils.substringAfter(neworderdata, " ");
                            
                            
                            //If the neworderliveid that got returned isn't "", then sent it to genneworder
                            if (!neworderdata.equalsIgnoreCase("") && newordertype.equalsIgnoreCase("KOP")){
                                System.out.println("Placing KOP for what's on db row id "+ neworderliveid);
                                writer.write(genOrderfix(neworderliveid));
                                writer.newLine();
                                writer.flush();
                                //TODO here call genorderfixmessage -thingy

                            }
                            if (!neworderdata.equalsIgnoreCase("") && newordertype.equalsIgnoreCase("SALJ")){
                                System.out.println("Placing KOP for what's on db row id "+ neworderliveid);
                                writer.write(genOrderfix(neworderliveid));
                                writer.newLine();
                                writer.flush();
                                //TODO here call genorderfixmessage -thingy

                            }
                            if (!neworderdata.equalsIgnoreCase("") && newordertype.equalsIgnoreCase("CANCEL")){
                                System.out.println("Placing KOP for what's on db row id "+ neworderliveid);
                                writer.write(genCancelOrderfix(neworderliveid));
                                writer.newLine();
                                writer.flush();
                                //TODO here call genorderfixmessage -thingy

                            }
                        }


                        // We get our first heartbeat, hence sending our first Mass Order Status request in order to check if there are any unprocessed orders.
                        if (HEARTBEATCOUNTER == 1 && !SENTMASSSTATUSREQ){
                            System.out.println("Got One heartbeat, sending Mass Status Order Request");
                            writer.write(genMassStatusRequestFix());
                            writer.newLine();
                            writer.flush();

                            SENTMASSSTATUSREQ = true;
                            
                        }



                    }

                }

                //After this line, everthing get run every cpu-cycle, beware!

            } //end of READER loop


            gv.client.close();
            System.out.println("Fix: Ended session with host properly.");
            logger.writelog("Fix: Ended session with host properly.");
            gv.setreadytoTrade(false);
            //System.exit(0);

        }

        catch(Exception e){
            //System.out.println("Exception: " + e.toString());
            System.out.println("Connection to FIX server "+OrderHost+" failed, reason: "  +e.getMessage());
            logger.writelog("Connection to FIX server "+OrderHost+" failed, reason: "  +e.getMessage());
            //System.out.println(e.getCause().toString());


        }



    }

    ////////////////////////////////////////////////////////////////////////////


}
