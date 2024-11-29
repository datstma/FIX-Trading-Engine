/* @author Stefan Månsby */

package trader;

/**
 *
 * @author stefanm
 */
public class RunMeLooping {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        // TODO code application logic here

        GlobalVars gvmaster = new GlobalVars();

        Logger logger = new Logger();
        logger.createworkdir();


        logger.writelog("Starting Trader version: " + gvmaster.VERSION);

        FIXmt fixThrd = new FIXmt(gvmaster);
        fixThrd.setName("The Fix client thread");
        //showThreadStatus(fixThrd);

        
       while(true){
        
        	
            System.out.println("Starting the Fix thread");
            fixThrd = new FIXmt(gvmaster);
            fixThrd.setName("The Fix client thread");
            showThreadStatus(fixThrd);
            try{
            fixThrd.run();
            } catch (Exception e){
            	logger.writelog("Exception: Got an error from the Fix thread: " + e.toString());
            }
           logger.writelog("The Fix thread has died, waiting for 180 secs, then restarting it.");
            Thread.currentThread();
			Thread.sleep(180000);
			logger.writelog("");
      }
        
    }


    static void showThreadStatus(Thread thrd) {
        System.out.println(thrd.getName()+"Alive:="+thrd.isAlive()+" State:=" + thrd.getState() );
    }


}
