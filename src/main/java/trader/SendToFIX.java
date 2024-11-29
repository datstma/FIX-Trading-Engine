/* @author Stefan Månsby */
package trader;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



public class SendToFIX extends Thread{

	private GlobalVars gv;	
	private Socket forwardedClientSocket;
	private Connection dbcon = null;

    Logger logger = new Logger();

	public SendToFIX(GlobalVars gv, Socket forwardedClientSocket) {
		this.gv = gv;
		this.forwardedClientSocket = forwardedClientSocket;
	}

	
	public void run() {
		dbConnect();
		
		//Debugdax
        logger.writelog("SendToFIX GOT: " + gv.toString() + " " + forwardedClientSocket);
		
        //Wait for 0,5 sec before sending initial Logon message
        try {
			sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        //BufferedWriter writer;
        

        try {
			gv.writer = new BufferedWriter(new OutputStreamWriter(forwardedClientSocket.getOutputStream()));

            OrderChecker ocThrd = new OrderChecker(gv);
            ocThrd.setName("The OrderChecker thread");
			ocThrd.start();

            SendEveryFiveMinutes sefmThrd = new SendEveryFiveMinutes(gv);
            sefmThrd.setName("The SendEveryFiveMinutes thread");
            sefmThrd.start();

			try {
				gv.sendMSGToFIX(genLoginfix());
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

        
			while(forwardedClientSocket.isConnected())
			{

				//If we get a new message, then process it, and then set the LastMessageFromFIX String to "" to singal that it has been processed
				try {
					
					//Get the last message content from globalvars
					String THELINE = gv.getLastMessageFromFIX(); 
					
					//Check if the message contained anything, if yes, then process it.
					if (!THELINE.equalsIgnoreCase(""))
					{
					gv.setLastMessageFromFIX("");
					//Begin inside of new message processing loop
					logger.writelog("Got the message:" + THELINE);
					
					String THEMESSAGEARRAY[] = THELINE.split("\01");	
						
					//Begin processing the message, pick out the variables that found for later processing
                    String MSGTYPE = "";
                    String TSTMSGID = "";
                    String ORDSTATUS = "";
                    String CIOrdID = "";
                    String TXT = "";
                    String URGENCY = "";
                    String GOTAccount = "";
                    String MarginExcess = "";
                    String TotalNetValue = "";
                    String CollRptID = "";
                    String MorningBuyingPower = "";
                    String CIOrdIDtobeCancelled = "";
                    String LastShares = "";
                    String OrderQty = "";
                    String LastPrice = "";
                    String Commission = "";

                    for (int i = 0; i < THEMESSAGEARRAY.length; i++)
                    {

                        if (THEMESSAGEARRAY[i].startsWith("1=")){
                            GOTAccount = THEMESSAGEARRAY[i].substring(2, THEMESSAGEARRAY[i].length() );
                            //System.out.println("GOT FIX ver.: " + FIXVER);
                        }

                        if (THEMESSAGEARRAY[i].startsWith("8=")){
                            String FIXVER = THEMESSAGEARRAY[i].substring(2, THEMESSAGEARRAY[i].length() );
                            //System.out.println("GOT FIX ver.: " + FIXVER);
                        }

                        
                        //Check Orderstatus
                        if (THEMESSAGEARRAY[i].startsWith("11=")){
                            CIOrdID = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                            logger.writelog("GOT Order id: " + CIOrdID);
                        }

                        //Check Commission
                        if (THEMESSAGEARRAY[i].startsWith("12=")){
                            Commission = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                            logger.writelog("GOT Commission: " + Commission);
                        }

                        //Check LastPX, i.e. the last price that we bought/sold for, reported in via the execution report
                        if (THEMESSAGEARRAY[i].startsWith("31=")){
                            LastPrice = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                            double inDouble = Double.parseDouble(LastPrice);
                            inDouble = inDouble * 100;
                            //Remove everything after any "." i.e. decimals
                            LastPrice = Double.toString(inDouble);
                            LastPrice = StringUtils.substringBefore(LastPrice,".");
                            logger.writelog("GOT Last Price (converted from double): " + LastPrice);


                        }

                        //Check Last shared, i.e. how many shares are filled in a order, reported in via the execution report
                        if (THEMESSAGEARRAY[i].startsWith("32=")){
                            LastShares = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                            //gv.serverSequence = Integer.parseInt(SEQUENCE);
                            //System.out.println("GOT SEQUENCE: " + SEQUENCE);
                        }

                        //Check SEQUENCE
                        if (THEMESSAGEARRAY[i].startsWith("34=")){
                            String SEQUENCE = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                            gv.serverSequence = Integer.parseInt(SEQUENCE);
                            //System.out.println("GOT SEQUENCE: " + SEQUENCE);
                        }
                        
                        
                        //Check MSGTYPE
                        if (THEMESSAGEARRAY[i].startsWith("35=")){
                            MSGTYPE = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                            logger.writelog("GOT MSG type: " + MSGTYPE + " (A=Logon, h=Status, 5=Logout, 0=Heartbeat, 1=TestMessage)");
                        }

                        //Check Order Quantity, i.e. how many shares where requested in a order, reported in via the execution report
                        if (THEMESSAGEARRAY[i].startsWith("38=")){
                            OrderQty = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                            //gv.serverSequence = Integer.parseInt(SEQUENCE);
                            //System.out.println("GOT SEQUENCE: " + SEQUENCE);
                        }


                        //Check Orderstatus
                        if (THEMESSAGEARRAY[i].startsWith("39=")){
                            ORDSTATUS = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                            logger.writelog("GOT Order Status: " + ORDSTATUS);
                        }

                      //Check CIOrdOID which is in the process of being canceled
                        if (THEMESSAGEARRAY[i].startsWith("41=")){
                            CIOrdIDtobeCancelled = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                            logger.writelog("GOT Order to be Canceled: " + CIOrdIDtobeCancelled);
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
                            logger.writelog("GOT Text: " + TXT);
                        }
                        
                        //Check TXT
                        if (THEMESSAGEARRAY[i].startsWith("61=")){
                            URGENCY = THEMESSAGEARRAY[i].substring(3, THEMESSAGEARRAY[i].length() );
                            logger.writelog("GOT Urgency: " + URGENCY);
                        }

                        //Check SEQUENCE
                        if (THEMESSAGEARRAY[i].startsWith("112=")){
                            TSTMSGID = THEMESSAGEARRAY[i].substring(4, THEMESSAGEARRAY[i].length() );
                            logger.writelog("GOT Test MSG ID: " + TSTMSGID);
                        }

                        //Check Trade session status
                        if (THEMESSAGEARRAY[i].startsWith("340=")){
                            String TRDSTAT = THEMESSAGEARRAY[i].substring(4, THEMESSAGEARRAY[i].length() );
                            logger.writelog("GOT TradingSessionStatus: " + TRDSTAT +"(2=Open, 3=Closed, 0=Unknown)");
                        }
                        
                        //Check Trade session status
                        if (THEMESSAGEARRAY[i].startsWith("899=")){
                            MarginExcess = THEMESSAGEARRAY[i].substring(4, THEMESSAGEARRAY[i].length() );
                            logger.writelog("GOT MarginExcess: " + MarginExcess);
                        }
                        
                        //Check Trade session status
                        if (THEMESSAGEARRAY[i].startsWith("900=")){
                            TotalNetValue = THEMESSAGEARRAY[i].substring(4, THEMESSAGEARRAY[i].length() );
                            logger.writelog("GOT TotalNetValue: " + TotalNetValue);
                        }
                        
                        //Check Trade session status
                        if (THEMESSAGEARRAY[i].startsWith("908=")){
                            CollRptID = THEMESSAGEARRAY[i].substring(4, THEMESSAGEARRAY[i].length() );
                            logger.writelog("GOT CollRptID: " + CollRptID);
                        }
                        
                        //Check Trade Morning buying power (colateral message)
                        if (THEMESSAGEARRAY[i].startsWith("10002=")){
                            MorningBuyingPower = THEMESSAGEARRAY[i].substring(6, THEMESSAGEARRAY[i].length() );
                            logger.writelog("GOT MorningBuyingPower: " + MorningBuyingPower);
                        }
                        
                        

                    } //End processing of message
					
					//////////////////////////////////////////
                    if (MSGTYPE.equalsIgnoreCase("1")){
                        logger.writelog("Got a Test message, sending HeartBeat response");
                        gv.setreadytoTrade(false);
                        logger.writelog("Set Ready to Trade:" + gv.getreadytoTrade());
                        //writer.write(genHeartBeatResponseFix(TSTMSGID));
                        //writer.flush();
                        gv.sendMSGToFIX(genHeartBeatResponseFix(TSTMSGID));

                        gv.setreadytoTrade(true);
                        logger.writelog("Set Ready to Trade:" + gv.getreadytoTrade());

                        gv.HEARTBEATCOUNTER++;  
                    }
                    
                    if (gv.HEARTBEATCOUNTER == 1 && !gv.SENTMASSSTATUSREQ){
                    	logger.writelog("Got One heartbeat, sending Mass Status Order Request");                    	
                    	sleep(2000);                    	
                       // writer.write(genMassStatusRequestFix());
                       // writer.flush();
                        gv.sendMSGToFIX(genMassStatusRequestFix());
                        gv.SENTMASSSTATUSREQ = true;
                        
                    }
                                       
                    
                    if (MSGTYPE.equalsIgnoreCase("2")){
                    	logger.writelog("Got Resend request, sending confirmation");
                       // writer.write(genResendResponseFix());
                        gv.setreadytoTrade(false);
                        logger.writelog("Set Ready to Trade:" + gv.getreadytoTrade());
                      //  writer.flush();
                        gv.sendMSGToFIX(genResendResponseFix());
                        gv.setreadytoTrade(true);
                        logger.writelog("Set Ready to Trade:" + gv.getreadytoTrade());
                    }
					
                    if (MSGTYPE.equalsIgnoreCase("3")){
                    	logger.writelog("Got a REJECT! Reason: " + TXT);

                    }
                    
                  //* Handle Execution Reports*/
                    if (MSGTYPE.equalsIgnoreCase("8")){
                    	logger.writelog("Got a Execution Report: ");
                        if (ORDSTATUS.contentEquals("A")) {
                        	logger.writelog("Pending New");
                        	setOrderStatus(CIOrdID,"Pending New");
                            insertToOrderStatusLog(CIOrdID,"Pending New");
                            }
                        if (ORDSTATUS.contentEquals("B")) {
                        	logger.writelog("Calculated");
                        	setOrderStatus(CIOrdID,"Calculated");
                            insertToOrderStatusLog(CIOrdID,"Calculated");
                            }
                        if (ORDSTATUS.contentEquals("C")) {
                        	logger.writelog("Expired");
                        	setOrderStatus(CIOrdID,"Expired");
                            insertToOrderStatusLog(CIOrdID,"Expired");
                            }
                        if (ORDSTATUS.contentEquals("D")) {
                        	logger.writelog("Accepted for Bidding");
                        	setOrderStatus(CIOrdID,"Accepted for Bidding");
                            insertToOrderStatusLog(CIOrdID,"Accepted for Bidding");
                            }
                        if (ORDSTATUS.contentEquals("E")) {
                        	logger.writelog("Pending Replace (i.e. result of Order Cancel/Replace Request)");
                        	setOrderStatus(CIOrdID,"Pending Replace");
                            insertToOrderStatusLog(CIOrdID,"Pending Replace");
                            }
                        if (ORDSTATUS.contentEquals("0")) {
                        	logger.writelog("New");
                        	setOrderStatus(CIOrdID,"New");
                            insertToOrderStatusLog(CIOrdID,"New");
                            }
                        if (ORDSTATUS.contentEquals("1")) {
                        	logger.writelog("Partially filled " + LastShares + "/" + OrderQty + "updating the table with keyword Filled");
                        	//setOrderStatus(CIOrdID,"Partially filled " + LastShares + "/" + OrderQty);
                            setOrderStatus(CIOrdID,"Filled " + LastShares + "/" + OrderQty);
                            insertToOrderStatusLog(CIOrdID,"Partially filled " + LastShares + "/" + OrderQty);
                            setCommission(CIOrdID,Commission);
                            setLastPrice(CIOrdID,LastPrice);
                            }
                        if (ORDSTATUS.contentEquals("2")) {
                        	logger.writelog("Filled " + LastShares + "/" + OrderQty);
                        	setOrderStatus(CIOrdID,"Filled " + LastShares + "/" + OrderQty);
                            insertToOrderStatusLog(CIOrdID,"Filled " + LastShares + "/" + OrderQty);
                            setCommission(CIOrdID,Commission);
                            setLastPrice(CIOrdID,LastPrice);
                            }
                        if (ORDSTATUS.contentEquals("3")) {
                        	logger.writelog("Done for day");
                        	setOrderStatus(CIOrdID,"Done for day");
                            insertToOrderStatusLog(CIOrdID,"Done for day");
                            }
                        if (ORDSTATUS.contentEquals("4")) {
                        	logger.writelog("Canceled");
                        	setOrderStatus(CIOrdIDtobeCancelled,"Canceled");
                            insertToOrderStatusLog(CIOrdIDtobeCancelled,"Canceled");
                            }
                        if (ORDSTATUS.contentEquals("5")) {
                        	logger.writelog("Replaced (No longer used)");}
                        if (ORDSTATUS.contentEquals("6")) {
                        	logger.writelog("Pending Cancel (i.e. result of Order Cancel Request)");
                        	setOrderStatus(CIOrdID,"Pending Cancel");
                            insertToOrderStatusLog(CIOrdID,"Pending Cancel");
                            }
                        if (ORDSTATUS.contentEquals("7")) {
                        	logger.writelog("Stopped");
                        	setOrderStatus(CIOrdID,"Stopped");
                            insertToOrderStatusLog(CIOrdID,"Stopped");
                            }
                        if (ORDSTATUS.contentEquals("8")) {
                        	logger.writelog("Rejected");
                        	setOrderStatus(CIOrdID,"Rejected");
                            insertToOrderStatusLog(CIOrdID,"Rejected");
                            }
                        if (ORDSTATUS.contentEquals("9")) {
                        	logger.writelog("Suspended");
                        	setOrderStatus(CIOrdID,"Suspended");
                            insertToOrderStatusLog(CIOrdID,"Suspended");
                            }

                        if (ORDSTATUS.contentEquals("I")) {
                            //logger.writelog("End of MassStatus Request");
                            logger.writelog("Setting status READY TO TRADE true");
                            gv.setreadytoTrade(true);

                        }
                    }
                    
                    //If we get a cancel order reject (usually because the order doesn't exist)
                    if (MSGTYPE.equalsIgnoreCase("9")){
                    	logger.writelog("OrderCancelReject for "+ CIOrdIDtobeCancelled);
                    	 setOrderStatus(CIOrdIDtobeCancelled,TXT);
                        insertToOrderStatusLog(CIOrdIDtobeCancelled,TXT);
                    
                    }
                    
                    
                    if (MSGTYPE.contentEquals("B")){
                        String URGENCYLEVEL = "Normal";
                        if(URGENCY.contentEquals("1")){
                        URGENCYLEVEL = "FLash";
                        }
                    	logger.writelog("Got a News message: " + TXT + " with Ugency level: " + URGENCYLEVEL);

                    }
                    
                    if (MSGTYPE.contentEquals("BA")){
                        String URGENCYLEVEL = "Normal";
                        if(URGENCY.contentEquals("1")){
                        URGENCYLEVEL = "FLash";
                        }
                    	logger.writelog("Got a News message: " + TXT + " with Ugency level: " + URGENCYLEVEL);
                        //writer.write(genHeartBeatResponseFix(TSTMSGID));

                    }

                    //Process a collateral message
                    if (!TotalNetValue.equalsIgnoreCase("") && !MarginExcess.equalsIgnoreCase("")){
                       logger.writelog("Got a collateral message: ID " + CollRptID + " Total Net Value: " + TotalNetValue + " Margin excess: " + MarginExcess);
                       insertCollateral(CollRptID,TotalNetValue,MarginExcess);


                        }

					
					///////////////////////////////////////////
					//End inside of new message processing loop	
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //End of new message processing loop
				
			
				//Speed-brake
				try {
					sleep(gv.SpeedBrake);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
        
        
        
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       

		
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
	
////////////////////////////////////////////////////////////////////////////

public String genLoginfix(){

int k=0;
//int X=0;
String FIX49="49="+gv.SENDERCOMPID;

//String FIX554="554=fix";
String FIX4="8=FIX.4.4"+'\01'+"9=";



//Set/get date
DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
Date date = new Date();
String datenow = dateFormat.format(date);
ConvertlTime convertltime = new ConvertlTime();

String datenowUTC = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMdd-HH:mm:ss.SSS", datenow);

String FIX3="35=A" + '\01' + "141=Y" + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01'+"98=0"+'\01'+"108=28"+'\01' +"347=554_H1"+'\01'+ "554="+gv.PASSWORD+'\01';


int FIXLength = FIX3.length();

FIX4=FIX4 + FIXLength + '\01' + FIX3  ; // FIX Exempel 2

int FIX4Length = FIX4.length();

// Create a checksum
for ( int i = 0; i < FIX4Length; ++i )
{
char c = FIX4.charAt( i ); int j = (int) c; k=(k+j);
}

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

logger.writelog("Built LOGIN message: " + FIX4);
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

///////////////////////////////////

public String genHeartBeatResponseFix(String TestReqID){

    String AccountNumber = "12345678";

    int k=0;int X=0;
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
    String FIX3="35=0" + '\01' + "112="+TestReqID+'\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01';

    int FIXLength = FIX3.length();
    

    FIX4=FIX4 + FIXLength + '\01' + FIX3  ; // FIX Exempel 2


    int FIX4Length = FIX4.length();
    

    // Create a checksum
    for ( int i = 0; i < FIX4Length; ++i )
    {
        char c = FIX4.charAt( i ); int j = (int) c; k=(k+j);
    }

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

    //logger.writelog("Built Hearbeat response message: " + FIX4);
    logger.writelog("Built Hearbeat response message");

    //Write the message in the log_table for safe keeping
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

public String genResendResponseFix(){

    String AccountNumber = "12345678";

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
    String FIX3="35=4" + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01';

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

    logger.writelog("Built Drop Resend response message: " + FIX4);
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

public String genMassStatusRequestFix(){


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
    String datenowSerial = convertltime.convert("Europe/Stockholm", "yyyyMMdd-HH:mm:ss.SSS", "UTC", "yyyyMMddHHmmss", datenow);


    gv.mySequence++;
    //
    String FIX3="35=AF" + '\01' + "1="+gv.AccountNumber + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01'+"17=1"+'\01'+"584="+datenowSerial+'\01' +"585=7"+'\01';
   

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


public String genCollateralRequestRequestFix(){
    

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


    //Request snapshot + updates
    String FIX3="35=BB" + '\01' + "1="+gv.AccountNumber + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01'+ "263=1"+ '\01'+ "909=CollInquiryID"+ gv.mySequence + '\01';


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

    logger.writelog("Built COLREQ message: " + FIX4);
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

public String genLogoutFix(){

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

    
    
    String FIX3="35=5" + '\01' + "1="+gv.AccountNumber + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01';
   

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

    logger.writelog("Built Logout message: " + FIX4);
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

public String genCurrentPositionsRequestFix(){
    


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
    String FIX3="35=BB" + '\01' + "1="+gv.AccountNumber + '\01' + "34="+ gv.mySequence + '\01' + FIX49 + '\01' + "52="+datenowUTC +'\01'+"56="+gv.TargetCompID+'\01'+ "263=0"+ '\01'+ "710=CollInquiryID"+ gv.mySequence + '\01'+ "724=0"+ '\01';
   

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

    logger.writelog("Built PREQ message: " + FIX4);
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



public void setOrderStatus(String ID, String Status){

try{
Statement st2 = dbcon.createStatement();
//ResultSet rs2 = null;

logger.writelog("Setting STATUS for "+ ID +" to " + Status);
int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET STATUS='"+Status+"' WHERE ID='"+ID+"'") ;

st2.close();

}catch (Exception e){
System.out.println("setOrderStatus Db stuff crap");
System.out.println(e.toString());
//System.out.print(e.printStackTrace());
e.printStackTrace();
}


}
    ////////////////////////////////////////////////////////////////////////////

    public void insertCollateral(String thisCollRptID, String thisTotalNetValue, String thisMarginExcess){

        try{
            Statement st2 = dbcon.createStatement();
//ResultSet rs2 = null;

            logger.writelog("Inserting collateral values");

            int val = st2.executeUpdate("INSERT INTO collateral " + "(CollRptID, TotalNetValue, MarginExcess) "+ "values ('"+thisCollRptID+"', '"+thisTotalNetValue+"', '"+thisMarginExcess+"')") ;

            st2.close();

        }catch (Exception e){
            System.out.println("insertCollateral Db stuff crap");
            System.out.println(e.toString());

            e.printStackTrace();
        }




    }
    ////////////////////////////////////////////////////////////////////////////

    public void setCommission(String ID, String Commission){

        try{
            Statement st2 = dbcon.createStatement();

            logger.writelog("Setting Comission for "+ ID +" to " + Commission);
            int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET COMMISSION='"+Commission+"' WHERE ID='"+ID+"'") ;

            st2.close();

        }catch (Exception e){
            System.out.println("setCommission Db stuff crap");
            System.out.println(e.toString());
//System.out.print(e.printStackTrace());
            e.printStackTrace();
        }

    }


////////////////////////////////////////////////////////////////////////////

    public void setLastPrice(String ID, String Lastprice){

        try{
            Statement st2 = dbcon.createStatement();

            logger.writelog("Setting VARDE for "+ ID +" to " + Lastprice);
            int val = st2.executeUpdate("UPDATE "+gv.LIVE_TABLE+" SET VARDE='"+Lastprice+"' WHERE ID='"+ID+"'") ;

            st2.close();

        }catch (Exception e){
            System.out.println("setLastPrice Db stuff crap");
            System.out.println(e.toString());
//System.out.print(e.printStackTrace());
            e.printStackTrace();
        }

    }

////////////////////////////////////////////////////////////////////////////

    public void insertToOrderStatusLog(String ID, String Status){

        try{
            Statement st2 = dbcon.createStatement();
//ResultSet rs2 = null;


            int val = st2.executeUpdate("INSERT INTO trader_order_status_log " + "(trade_order_id, status) "+ "values ('"+ID+"', '"+Status+"')") ;

            st2.close();

        }catch (Exception e){
            System.out.println("insertToOrderStatusLog Db stuff crap");
            System.out.println(e.toString());
//System.out.print(e.printStackTrace());
            e.printStackTrace();
        }


    }
    ////////////////////////////////////////////////////////////////////////////

}
