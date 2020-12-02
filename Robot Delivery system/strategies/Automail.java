package strategies;

import automail.CautiousRobot;
import automail.Clock;
import automail.IMailDelivery;
import automail.Robot;

public class Automail {
	      
    public Robot[] robots;
    public IMailPool mailPool;
    
    public Automail(IMailPool mailPool, IMailDelivery delivery, int numRobots) {
    	// Swap between simple provided strategies and your strategies here

    	/** Initialize the MailPool */

    	this.mailPool = mailPool;

    	/** Factory creates the correct type of Robot */

    	Factory.getInstance().CreateInformationBroker(this,numRobots);

    	/** Initialize robots */
    	robots = new Robot[numRobots];
    	for (int i = 0; i < numRobots; i++){


    	    robots[i] = Factory.getInstance().createRobotObject(delivery,mailPool);
        }
    }

    
}
