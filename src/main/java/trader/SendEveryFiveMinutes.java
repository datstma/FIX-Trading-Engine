/* @author Stefan Månsby */
package trader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SendEveryFiveMinutes extends Thread{

    private GlobalVars gv;
    //private Socket forwardedClientSocket;
    private Connection dbcon = null;


    Logger logger = new Logger();



    public SendEveryFiveMinutes(GlobalVars gv) {
        this.gv = gv;
        //this.forwardedClientSocket = forwardedClientSocket;
    }


        public void run(){
            logger.writelog("Starting the SendEveryFiveMinutes process");
            dbConnect();


            while(true){

                try {

                //////////////////////////
                    //Sending MassStatus Request
                    if(gv.getreadytoTrade()){
                    gv.sendMSGToFIX(genMassStatusRequestFix());
                    } else{
                        logger.writelog("Send every five minutes detected that ready to trade was set to false, skipping sending massstatusrequest for 5 min.");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                //////////////////////////








                //Sleep for 5 minutes
                try {
                    sleep(300000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }


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

        //System.out.println("Date: " + datenow);
        //lastSequence++;
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

        //Attach checksum to the end of the final message
        FIX4=FIX4 +  "10="+ aa + '\01';

        logger.writelog("Built 5min MSR message: " + FIX4);
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


    ////////////////////////////////////////////////////////////////////////////

    private void dbConnect() {
        Logger logger = new Logger();
        logger.writelog("SendEveryFiveMinutes thread trying to load JDBC (org.gjt.mm.mysql.Driver) driver...");
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
            logger.writelog("SendEveryFiveMinutes Error: " + e.toString());
            //System.out.println("\nSendToFIX Error: " + e.toString());
            logger.writelog("SendEveryFiveMinutes Warning! The database connection has NOT been established!\n");
            //dbisConnected = false;
        }
    }
    ////////////////////////////////////////////////////////////////////////////

}
