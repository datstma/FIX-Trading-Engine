package trader.src.test.java.trader;

import org.apache.commons.lang3.StringUtils;
import trader.src.main.java.trader.ConvertlTime;
import trader.src.main.java.trader.GlobalVars;
import trader.src.main.java.trader.Logger;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderCheckerTEST extends Thread{

	private GlobalVars gv;
	//private Socket forwardedClientSocket;
	private Connection dbcon = null;
	private Logger logger = new Logger();
	//private BufferedWriter writer;
	//Statement st = null;
    //ResultSet rs = null;

    //public OrderChecker(GlobalVars gv, BufferedWriter writer) {
	public OrderCheckerTEST(GlobalVars gv) {
		this.gv = gv;
		//this.writer = writer;
	}

	public void run(){

		logger.writelog("Starting the order checker process");
		dbConnect();

		while(true){

		try {

			if(true){
				//Check for new oldest unprocessed order
                String neworderdata = "";
                String neworderliveid = "";
                String newordertype = "";

                neworderdata = checkForNewOrder();
                neworderliveid = StringUtils.substringBefore(neworderdata, " ");
                newordertype = StringUtils.substringAfter(neworderdata, " ");


                System.out.println("Neworderdata BEGIN:" + neworderdata+":END");

                //If the neworderliveid that got returned isn't "", then sent it to genneworder
                if (!neworderdata.equalsIgnoreCase("") && newordertype.equalsIgnoreCase("KOP")){
                	System.out.println("Placing KOP for what's on db row id "+ neworderliveid);
                   // gv.sendMSGToFIX(genOrderfix(neworderliveid));
                    System.out.println("Should have build message:" + genOrderfix(neworderliveid) );

                }
                if (!neworderdata.equalsIgnoreCase("") && newordertype.equalsIgnoreCase("SALJ")){
                    System.out.println("Placing KOP for what's on db row id "+ neworderliveid);
                   // gv.sendMSGToFIX(genOrderfix(neworderliveid));
                    System.out.println("Should have build message:" + genOrderfix(neworderliveid) );
               

                }
                if (!neworderdata.equalsIgnoreCase("") && newordertype.equalsIgnoreCase("CANCEL")){
                    System.out.println("Placing CANCEL for what's on db row id "+ neworderliveid);
                    // gv.sendMSGToFIX(genCancelOrderfix(neworderliveid));
                    System.out.println("Should have build message:" + genOrderfix(neworderliveid) );
             

                }
			
	
			
			}
			
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
            System.out.println("Orderchecker run error: " + e.toString());
			e.printStackTrace();
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
            Statement st = null;
            ResultSet rs = null;

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
                	logger.writelog("Starting KOP process for: " + ID);
                	logger.writelog("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;
                    //return ID,TYP,OTYP,Side,OrdType,OrderQty,VARDE,KONTRAKT,idTrade_live;
                    returnliveid = idTrade_live + " KOP";
                    //return returnArray;


                }

                if (StringUtils.equalsIgnoreCase(TYP, "SALJ") && StringUtils.isNumeric(ID) && !StringUtils.equalsIgnoreCase(ID, "NULL") && !StringUtils.equalsIgnoreCase(ID, null) && !StringUtils.equalsIgnoreCase(Side, "NULL") && !StringUtils.equalsIgnoreCase(Side, null)       )
                {
                	logger.writelog("Starting SALJ process for: " + ID);
                	logger.writelog("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;
                    //return ID,TYP,OTYP,Side,OrdType,OrderQty,VARDE,KONTRAKT,idTrade_live;
                    //return returnArray;
                    returnliveid = idTrade_live + " SALJ";

                }

                if (StringUtils.equalsIgnoreCase(TYP, "CANCEL") && StringUtils.isNumeric(ID) && !StringUtils.equalsIgnoreCase(ID, "NULL") && !StringUtils.equalsIgnoreCase(ID, null) && !StringUtils.equalsIgnoreCase(Side, "NULL") && !StringUtils.equalsIgnoreCase(Side, null)       )
                {
                	logger.writelog("Starting CANCEL process for: " + ID);
                	logger.writelog("Setting STATUS for "+ ID +" to PROC");
                    int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='PROC' WHERE idTrade_live='"+idTrade_live+"'") ;
                    //return ID,TYP,OTYP,Side,OrdType,OrderQty,VARDE,KONTRAKT,idTrade_live;
                    //return returnArray;
                    returnliveid = idTrade_live + " CANCEL";

                }



            }

            rs.close();
            st.close();
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

            rs.close();
            st.close();

        }catch (Exception e){
        	logger.writelog("genOrderfix Db stuff crap");
        	logger.writelog(e.toString());
            //System.out.print(e.printStackTrace());
            e.printStackTrace();
        }


        Double ActualVarde = Double.parseDouble(VARDE);
        ActualVarde = ActualVarde / 100;



        //String AccountNumber = "35003932";
        //String AccountNumber = "35004094";

        int k=0;
        int X=0;
        String FIX49="49="+gv.SENDERCOMPID;

        String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";
        //int FIXSequence = 1;


        //8,9,35,49,56,34,52,10

        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);

        //System.out.println("Date: " + datenow);
        //lastSequence++;
        gv.mySequence++;
        //
        String FIX3="35=D" + '\01' + "1="+gv.AccountNumber + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01'+"11="+ID+'\01'+"21=1"+'\01' +"38="+OrderQty+'\01'+"40="+OrdType+'\01'+"44="+ActualVarde+'\01'+"54="+Side+'\01'+"55="+KONTRAKT+'\01'+"59=0"+'\01'+"60="+datenowUTC+'\01'+"100=MBTX"+'\01'+"553="+gv.SENDERCOMPID+'\01'+"47=A"+'\01';
        //String FIX3="35=D" + '\01' + "1="+AccountNumber + '\01' +  FIX49 + '\01' + "52="+datenow +'\01'+"56="+TargetCompID+'\01'+"11=2012080701"+'\01'+"21=1"+'\01' +"38=1"+'\01'+"40=2"+'\01'+"44=1390.00"+'\01'+"54=1"+'\01'+"55=/ESU2"+'\01'+"59=0"+'\01'+"60="+datenowUTC+'\01'+"100=MBTX"+'\01'+"553=TSH77OS"+'\01';


        int FIXLength = FIX3.length();
        //System.out.println("FIX3 message: " + FIX3);
        //System.out.println("Fixlength: " +FIXLength);


        FIX4=FIX4 + FIXLength + '\01' + FIX3  ; // FIX Exempel 2

        //System.out.println("FIX4 message: " + FIX4);
        //System.out.println(FIX4);

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
/*
        if (a < 100)
        {
            //System.out.println("Fixing Checksum to THREE bytes");
            aa = "0"+ aa;
        }
*/
        if (aa.length() == 1){
        	aa = "00"+ aa;
        }
        
        if (aa.length() == 2){
        	aa = "0"+ aa;
        }
        
        
        //System.out.println("Checksum: " + a);



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
            Statement st2 = dbcon.createStatement();
            //ResultSet rs2 = null;

            Statement st = null;
            ResultSet rs = null;
            st = dbcon.createStatement();
            //Get the oldest order row
            //rs = st.executeQuery("SELECT * FROM "+gv.LIVE_TABLE+" WHERE ID = '"+ID+"'");
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
            st2.close();
        }catch (Exception e){
        	logger.writelog("genCancelOrderfix Db stuff crap");
        	logger.writelog(e.toString());
            //System.out.print(e.printStackTrace());
            e.printStackTrace();
        }


        Double ActualVarde = Double.parseDouble(VARDE);
        ActualVarde = ActualVarde / 100;



        //String AccountNumber = "35003932";
        //String AccountNumber = "35004094";

        int k=0;
        int X=0;
        String FIX49="49="+gv.SENDERCOMPID;

        String FIX554="554=fix";
        String FIX4="8=FIX.4.4"+'\01'+"9=";
        //int FIXSequence = 1;


        //8,9,35,49,56,34,52,10

        //Set/get date
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Date date = new Date();
        String datenow = dateFormat.format(date);
        ConvertlTime convertltime = new ConvertlTime();

        String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);

        //System.out.println("Date: " + datenow);
        //lastSequence++;
        gv.mySequence++;
        //
        String FIX3="35=F" + '\01' + "1="+gv.AccountNumber + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01'+"11="+ID+"C"+'\01'+"41="+ID+'\01'+"54="+Side+'\01'+"55="+KONTRAKT+'\01'+"60="+datenowUTC+'\01'+"553="+gv.SENDERCOMPID+'\01';
        //String FIX3="35=D" + '\01' + "1="+AccountNumber + '\01' +  FIX49 + '\01' + "52="+datenow +'\01'+"56="+TargetCompID+'\01'+"11=2012080701"+'\01'+"21=1"+'\01' +"38=1"+'\01'+"40=2"+'\01'+"44=1390.00"+'\01'+"54=1"+'\01'+"55=/ESU2"+'\01'+"59=0"+'\01'+"60="+datenowUTC+'\01'+"100=MBTX"+'\01'+"553=TSH77OS"+'\01';


        int FIXLength = FIX3.length();
        //System.out.println("FIX3 message: " + FIX3);
        //System.out.println("Fixlength: " +FIXLength);


        FIX4=FIX4 + FIXLength + '\01' + FIX3  ; // FIX Exempel 2

        //System.out.println("FIX4 message: " + FIX4);
        //System.out.println(FIX4);

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
/*
        if (a < 100)
        {
            //System.out.println("Fixing Checksum to THREE bytes");
            aa = "0"+ aa;
        }
*/
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
