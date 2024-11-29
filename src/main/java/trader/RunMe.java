/* @author Stefan Månsby */

package trader;


public class RunMe {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        // TODO code application logic here

        GlobalVars gvmaster = new GlobalVars();

        Logger logger = new Logger();
        logger.createworkdir();


        System.out.println("Starting Trader version: " + gvmaster.VERSION);

        FIXmt fixThrd = new FIXmt(gvmaster);
        fixThrd.setName("The Fix client thread");

        	 fixThrd.run();

        
    }

    static void showThreadStatus(Thread thrd) {
        System.out.println(thrd.getName()+"Alive:="+thrd.isAlive()+" State:=" + thrd.getState() );
    }


}
