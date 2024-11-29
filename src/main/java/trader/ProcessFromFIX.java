/* @author Stefan Månsby */
package trader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.lang3.StringUtils;

public class ProcessFromFIX extends Thread{

	private GlobalVars gv;	
	private Socket forwardedClientSocket;
	private Connection dbcon = null;
	private Logger logger = new Logger();
	
	public ProcessFromFIX(GlobalVars gv, Socket forwardedClientSocket) {
		this.gv = gv;
		this.forwardedClientSocket = forwardedClientSocket;
	}
	
	
////////////////////////////////////////////////////////////////////////////
	
	public void run() {
		//dbConnect();
		
		//Debugdax
		logger.writelog("ProcessFromFIX GOT: " + gv.toString() + " " + forwardedClientSocket);
		
        BufferedReader reader;

       //Beginning external read loop
        try {
			reader = new BufferedReader(new InputStreamReader(forwardedClientSocket.getInputStream()));
			
			int c;
			
            String LASTSEVEN = "";
            String THELINE = "";
            
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

                        //String THEMESSAGEARRAY[] = THELINE.split("\01");
                       
                        try {
							
                        	gv.setLastMessageFromFIX(THELINE);
						
                        } catch (InterruptedException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
                            logger.writelog("ProcessFromFIX error: " + e.toString());
						}

                        //System.out.println(THEMESSAGE);
                        //System.out.println(Arrays.toString(THEMESSAGEARRAY));

                        //Clear the THELINE variable to start working on the next message
                        THELINE = "";
                 
                        }                       
                   }

                
                //speed-brake
                /*
                try {
					sleep(gv.SpeedBrake);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                */
                
          }
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//Ending external read loop
        
        
        
        
		
	}
	
	
	 ////////////////////////////////////////////////////////////////////////////

    private void dbConnect() {
        Logger logger = new Logger();
        logger.writelog("ProcessFromFIX thread trying to load JDBC (org.gjt.mm.mysql.Driver) driver...");
        try{            //full path to JDBC Driver
            Class.forName("org.gjt.mm.mysql.Driver").newInstance();
            logger.writelog("ProcessFromFIX thread Loading of JDBC driver went fine.");
         //   logger.writelog("Fix thread Connecting to the database: " + gv.dburl);
            dbcon = DriverManager.getConnection(gv.dburl, gv.dbuser, gv.dbpassword);
            logger.writelog("ProcessFromFIX thread Database connection went fine.");
           // logger.writelog("Fix db connection proof: "+dbcon.toString());
            //dbisConnected = true;

            //System.out.println("done!");
        } catch (Exception e){
            logger.writelog("Error: " + e.toString());
            //System.out.println("\nError: " + e.toString());
            logger.writelog("Warning! The database connection has NOT been established!\n");
            //dbisConnected = false;
        }
    }
    ////////////////////////////////////////////////////////////////////////////
	
	
	

}
