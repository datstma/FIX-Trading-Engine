/* @author Stefan Månsby */
package trader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class OrderChecker extends Thread{

	private final GlobalVars gv;
	//private Socket forwardedClientSocket;
	private Connection dbcon = null;
	private final Logger logger = new Logger();
   
    //public OrderChecker(GlobalVars gv, BufferedWriter writer) {
	public OrderChecker(GlobalVars gv) {
		this.gv = gv;
		//this.writer = writer;
	}
	
	public void run(){
	
		logger.writelog("Starting the order checker process");
		dbConnect();
		
		while(true){
			
		try {
			
			if(gv.getreadytoTrade()){	
				//Check for new oldest unprocessed order
                String neworderdata;
                String neworderliveid;
                String newordertype;

                neworderdata = checkForNewOrder();
                neworderliveid = StringUtils.substringBefore(neworderdata, " ");
                newordertype = StringUtils.substringAfter(neworderdata, " ");




                //If the neworderliveid that got returned isn't "", then sent it to genneworder
                if (!neworderdata.equalsIgnoreCase("") && newordertype.equalsIgnoreCase("KOP")){
                	logger.writelog("Placing KOP for what's on db row id "+ neworderliveid);
                    gv.sendMSGToFIX(genOrderfix(neworderliveid));

                }
                if (!neworderdata.equalsIgnoreCase("") && newordertype.equalsIgnoreCase("SALJ")){
                	logger.writelog("Placing SALJ for what's on db row id "+ neworderliveid);
                    gv.sendMSGToFIX(genOrderfix(neworderliveid));
               

                }
                if (!neworderdata.equalsIgnoreCase("") && newordertype.equalsIgnoreCase("CANCEL")){
                	logger.writelog("Placing CANCEL for what's on db row id "+ neworderliveid);               
                    gv.sendMSGToFIX(genCancelOrderfix(neworderliveid));
             

                }

                if (!neworderdata.equalsIgnoreCase("") && newordertype.equalsIgnoreCase("COLLATERAL")){
                    logger.writelog("Placing COLLATERAL for what's on db row id "+ neworderliveid);
                    gv.sendMSGToFIX(genCollateralRequestRequestFix(neworderliveid));


                }

                if (!neworderdata.equalsIgnoreCase("") && newordertype.equalsIgnoreCase("POSITIONS")){
                    logger.writelog("Placing POSITIONS for what's on db row id "+ neworderliveid);
                    gv.sendMSGToFIX(genCurrentPositionsRequestFix(neworderliveid));


                }
			
	
			
			}else {
                logger.writelog("Orderchecker detected that ready to trade was set to false, waiting 1 sec before retry.");
            }
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            logger.writelog("Orderchecker error: " + e);
		}
		
		//Speed-brake
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}
	}
	
	
	public String checkForNewOrder(){

        String returnliveid = "";

        try{
            Statement st2 = dbcon.createStatement();
            //ResultSet rs2 = null;

            Statement st;
            ResultSet rs;

            st = dbcon.createStatement();
            //Get the oldest order row
            //rs = st.executeQuery("SELECT * FROM "+gv.LIVE_TABLE+" WHERE STATUS = 'ORDER' AND Side IS NOT null ORDER BY idTrade_live ASC LIMIT 1");
            rs = st.executeQuery("SELECT * FROM "+gv.LIVE_TABLE+" WHERE STATUS = 'ORDER' ORDER BY idTrade_live ASC LIMIT 1");


            while (rs.next ()) {

                String ID = "NULL";
                String TYP;
                String OTYP = "NULL";
                String Side;
                String OrdType = "NULL";
                String OrderQty = "NULL";
                String VARDE = "NULL";
                String KONTRAKT = "NULL";
                String idTrade_live;

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
                	logger.writelog("Starting KOP process for: " + ID);
                	logger.writelog("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;
                    val = st2.executeUpdate("INSERT INTO trader_order_status_log " + "(trade_order_id, status) "+ "values ('"+ID+"', 'PROC')") ;
                    //return ID,TYP,OTYP,Side,OrdType,OrderQty,VARDE,KONTRAKT,idTrade_live;
                    returnliveid = idTrade_live + " KOP";
                    //return returnArray;


                }

                if (StringUtils.equalsIgnoreCase(TYP, "SALJ") && StringUtils.isNumeric(ID) && !StringUtils.equalsIgnoreCase(ID, "NULL") && !StringUtils.equalsIgnoreCase(ID, null) && !StringUtils.equalsIgnoreCase(Side, "NULL") && !StringUtils.equalsIgnoreCase(Side, null)       )
                {
                	logger.writelog("Starting SALJ process for: " + ID);
                	logger.writelog("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;
                    val = st2.executeUpdate("INSERT INTO trader_order_status_log " + "(trade_order_id, status) "+ "values ('"+ID+"', 'PROC')") ;
                    //return ID,TYP,OTYP,Side,OrdType,OrderQty,VARDE,KONTRAKT,idTrade_live;
                    //return returnArray;
                    returnliveid = idTrade_live + " SALJ";

                }

                if (StringUtils.equalsIgnoreCase(TYP, "CANCEL") && StringUtils.isNumeric(ID) && !StringUtils.equalsIgnoreCase(ID, "NULL") && !StringUtils.equalsIgnoreCase(ID, null) && !StringUtils.equalsIgnoreCase(Side, "NULL") && !StringUtils.equalsIgnoreCase(Side, null)       )
                {
                	logger.writelog("Starting CANCEL process for: " + ID);
                	logger.writelog("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;
                    val = st2.executeUpdate("INSERT INTO trader_order_status_log " + "(trade_order_id, status) "+ "values ('"+ID+"', 'PROC')") ;
                    //return ID,TYP,OTYP,Side,OrdType,OrderQty,VARDE,KONTRAKT,idTrade_live;
                    //return returnArray;
                    returnliveid = idTrade_live + " CANCEL";

                }

                if (StringUtils.equalsIgnoreCase(TYP, "COLLATERAL") && StringUtils.isNumeric(ID) && !StringUtils.equalsIgnoreCase(ID, "NULL") && !StringUtils.equalsIgnoreCase(ID, null)  )
                {
                    logger.writelog("Starting COLLATERAL process for: " + ID);
                    logger.writelog("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;
                    val = st2.executeUpdate("INSERT INTO trader_order_status_log " + "(trade_order_id, status) "+ "values ('"+ID+"', 'PROC')") ;
                    //return ID,TYP,OTYP,Side,OrdType,OrderQty,VARDE,KONTRAKT,idTrade_live;
                    //return returnArray;
                    returnliveid = idTrade_live + " COLLATERAL";

                }

                if (StringUtils.equalsIgnoreCase(TYP, "POSITIONS") && StringUtils.isNumeric(ID) && !StringUtils.equalsIgnoreCase(ID, "NULL") && !StringUtils.equalsIgnoreCase(ID, null)  )
                {
                    logger.writelog("Starting POSITIONS process for: " + ID);
                    logger.writelog("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;
                    val = st2.executeUpdate("INSERT INTO trader_order_status_log " + "(trade_order_id, status) "+ "values ('"+ID+"', 'PROC')") ;
                    //return ID,TYP,OTYP,Side,OrdType,OrderQty,VARDE,KONTRAKT,idTrade_live;
                    //return returnArray;
                    returnliveid = idTrade_live + " POSITIONS";

                }



            }
            st.close();
            rs.close();
            st2.close();
        }catch (Exception e){
        	logger.writelog("checkForNewOrder Db stuff crap");
        	logger.writelog(e.toString());
            //System.out.print(e.printStackTrace());
            e.printStackTrace();
        }


        return returnliveid;

    }

    ////////////////////////////////////////////////////////////////////////////

    public String genCurrentPositionsRequestFix(String neworderliveid){

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


        //Request a snapshot
        String FIX3="35=AN" + '\01' + "1="+gv.AccountNumber + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01'+ "263=0"+ '\01'+ "710=CollInquiryID"+ gv.mySequence + '\01'+ "724=0"+ '\01';


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

        if (aa.length() == 1){
            aa = "00"+ aa;
        }

        if (aa.length() == 2){
            aa = "0"+ aa;
        }

        //Attach checksum to the end of the final message
        FIX4=FIX4 +  "10="+ aa + '\01';

        logger.writelog("Built MSR message: " + FIX4);
        //System.out.println("Our last SEQ:" + mySequence);

        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+gv.mySequence+"', '"+FIX4+"')") ;

            log_st.close();

        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }

        return (FIX4);


    }


    ////////////////////////////////////////////////////////////////////////////

    public String genCollateralRequestRequestFix(String TradeLiveRowID){

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


        //Request a snapshot
        String FIX3="35=BB" + '\01' + "1="+gv.AccountNumber + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01'+ "263=0"+ '\01'+ "909=CollInquiryID"+ gv.mySequence + '\01';


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

        if (aa.length() == 1){
            aa = "00"+ aa;
        }

        if (aa.length() == 2){
            aa = "0"+ aa;
        }

        //Attach checksum to the end of the final message
        FIX4=FIX4 +  "10="+ aa + '\01';

        logger.writelog("Built MSR message: " + FIX4);
        //System.out.println("Our last SEQ:" + mySequence);

        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+gv.mySequence+"', '"+FIX4+"')") ;

            log_st.close();

        } catch (SQLException ex) {
            System.out.println("Failed to write database: "+ ex);
        }

        return (FIX4);


    }



	 ////////////////////////////////////////////////////////////////////////////

    private void dbConnect() {
        Logger logger = new Logger();
        logger.writelog("SendToFIX thread trying to load JDBC (org.gjt.mm.mysql.Driver) driver...");
        try{            //full path to JDBC Driver
            Class.forName("org.gjt.mm.mysql.Driver").newInstance();
            logger.writelog("SendToFIX thread Loading of JDBC driver went fine.");
         //   logger.writelog("Fix thread Connecting to the database: " + gv.dburl);
            dbcon = DriverManager.getConnection(gv.dburl, gv.dbuser, gv.dbpassword);
            logger.writelog("SendToFIX thread Database connection went fine.");
           // logger.writelog("Fix db connection proof: "+dbcon.toString());
            //dbisConnected = true;

            //System.out.println("done!");
        } catch (Exception e){
            logger.writelog("SendToFIX Error: " + e.toString());
            //System.out.println("\nSendToFIX Error: " + e.toString());
            logger.writelog("SendToFIX Warning! The database connection has NOT been established!\n");
            //dbisConnected = false;
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    public String genOrderfix(String TradeLiveRowID){
        //8=FIX.4.49=15535=A141=Y34=149=DEMOZGBXFIX52=20120807-17:54:48.68856=MBT98=0108=30347=554_H1554=MASSAHASH10=161

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


            Statement st = null;
            ResultSet rs = null;

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
            st.close();
            rs.close();

        }catch (Exception e){
        	logger.writelog("genOrderfix Db stuff crap");
        	logger.writelog(e.toString());
            //System.out.print(e.printStackTrace());
            e.printStackTrace();
        }


        Double ActualVarde = Double.parseDouble(VARDE);
        ActualVarde = ActualVarde / 100;



        int k=0;
        int X=0;
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
        String FIX3="35=D" + '\01' + "1="+gv.AccountNumber + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01'+"11="+ID+'\01'+"21=1"+'\01' +"38="+OrderQty+'\01'+"40="+OrdType+'\01'+"44="+ActualVarde+'\01'+"54="+Side+'\01'+"55="+KONTRAKT+'\01'+"59=0"+'\01'+"60="+datenowUTC+'\01'+"100=MBTX"+'\01'+"553="+gv.SENDERCOMPID+'\01'+"47=A"+'\01';

        int FIXLength = FIX3.length();



        FIX4=FIX4 + FIXLength + '\01' + FIX3  ; // FIX Exempel 2



        int FIX4Length = FIX4.length();


        // Create a checksum
        for ( int i = 0; i < FIX4Length; ++i )
        {
            char c = FIX4.charAt( i ); int j = (int) c; k=(k+j);
        }

        //System.out.println("Key: " + k);
        int a = k % 256 ;

        String aa = Integer.toString(a);

        if (aa.length() == 1){
        	aa = "00"+ aa;
        }
        
        if (aa.length() == 2){
        	aa = "0"+ aa;
        }

        //Attach checksum to the end of the final message
        FIX4=FIX4 +  "10="+ aa + '\01';

        logger.writelog("Built ORDER message: " + FIX4);
        //System.out.println("Our last SEQ:" + mySequence);
        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+gv.mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
        	logger.writelog("Failed to write database: "+ ex);
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


            Statement st = null;
            ResultSet rs = null;

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
                //idTrade_live = rs.getString("idTrade_live");
                //  System.out.println("idTrade_live:"+idTrade_live);


            }

            rs.close();
            st.close();


        }catch (Exception e){
        	logger.writelog("genCancelOrderfix Db stuff crap");
        	logger.writelog(e.toString());
            //System.out.print(e.printStackTrace());
            e.printStackTrace();
        }


        Double ActualVarde = Double.parseDouble(VARDE);
        ActualVarde = ActualVarde / 100;

        int k=0;
        int X=0;
        String FIX49="49="+gv.SENDERCOMPID;

        String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";
        //int FIXSequence = 1;

        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);

        gv.mySequence++;
        //
        String FIX3="35=F" + '\01' + "1="+gv.AccountNumber + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01'+"11="+ID+"C"+'\01'+"41="+ID+'\01'+"54="+Side+'\01'+"55="+KONTRAKT+'\01'+"60="+datenowUTC+'\01'+"553="+gv.SENDERCOMPID+'\01';


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

        if (aa.length() == 1){
        	aa = "00"+ aa;
        }
        
        if (aa.length() == 2){
        	aa = "0"+ aa;
        }
        
        //System.out.println("Checksum: " + a);



        //Attach checksum to the end of the final message
        FIX4=FIX4 +  "10="+ aa + '\01';

        logger.writelog("Built CANCEL message: " + FIX4);
        //System.out.println("Our last SEQ:" + mySequence);

        Statement log_st;
        try {
            log_st = dbcon.createStatement();
            int val = log_st.executeUpdate("INSERT INTO trader_sequence_log " + "(mysequence, message_was) "+ "values ('"+gv.mySequence+"', '"+FIX4+"')") ;
        } catch (SQLException ex) {
        	logger.writelog("Failed to write database: "+ ex);
        }


        return (FIX4);


    }





    ////////////////////////////////////////////////////////////////////////////





}
