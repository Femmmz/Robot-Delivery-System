package strategies;


import automail.CautiousRobot;
import automail.IMailDelivery;
import automail.Robot;

import java.util.Properties;

/**
 * Will use this class to refract Simulation to accomodate both caution behaviour and basic behaviour
 */
public class Factory {

    /**
     * Will contain all classes with multiple implementation.
     * Will decide which implementation best suites current Simulation
     * Reason: Decoupling, deligating, etc...
     */
    private Properties automailProperties;

    private static Factory instance=null;

    private Factory(Properties automailProperties){
        this.automailProperties =automailProperties;
    }

    public static void setInstance(Properties automailProperties){
        Factory.instance= new Factory(automailProperties);

    }

    public static Factory getInstance(){
        if (Factory.instance == null){
            Factory.instance = new Factory(null);
        }
        return Factory.instance;
    }



    public IMailPool  createMailPoolStrategy(int nRobot){

        if(Boolean.parseBoolean(automailProperties.getProperty("Caution") )){
            return createCautionModeStrategy(nRobot);

        }
        else{
            return createBasicStrategy(nRobot);
        }

    }

    private CautionMode createCautionModeStrategy(int nRobot){
        return new CautionMode(nRobot);
    }

    private MailPool createBasicStrategy(int nRobot){
        return new MailPool(nRobot);

    }

    public Robot createRobotObject(IMailDelivery delivery, IMailPool mailPool) {

        if (Boolean.parseBoolean(automailProperties.getProperty("Caution"))){
            return createCautiousRobot(delivery, mailPool);

        } else {
            return createNormalRobot(delivery, mailPool);

        }
    }


    private CautiousRobot createCautiousRobot(IMailDelivery delivery, IMailPool mailPool){
        return new CautiousRobot(delivery, (CautionMode) mailPool);
    }


    private Robot createNormalRobot(IMailDelivery delivery, IMailPool mailPool){
        return new Robot(delivery, mailPool);
    }

    public void CreateInformationBroker(Automail automail, int nRobots){
        if (Boolean.parseBoolean(automailProperties.getProperty("Caution"))){
            InformationBroker.setInstance(automail,nRobots);
        }
    }

}
