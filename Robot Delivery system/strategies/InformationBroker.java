package strategies;

import automail.CautiousRobot;
import automail.Robot;

public class InformationBroker {

    private static InformationBroker instance= null;

    private Automail automail;
    private int nRobot;
    private InformationBroker(Automail automail, int nRobot){
        this.automail = automail;
        this.nRobot = nRobot;
    }

    public static InformationBroker getInstance(){
        return InformationBroker.instance;
    }

    public static void setInstance(Automail automail, int nRobot){
        InformationBroker.instance=new InformationBroker(automail, nRobot);
    }

    /**
     * Checks if there is no unwrapping going on in nextFloor hence safe to move
     * @param nextFloor the floor it checks
     * @return true if it is safe to move
     */
    public boolean canMoveToNextLevel(int nextFloor){
        CautiousRobot r;
        for (int i = 0; i <nRobot ; i++) {
            r = (CautiousRobot) automail.robots[i];
            if (r.getCurrent_floor() == nextFloor && r.getSpecialRobotState() == CautiousRobot.SpecialRobotState.UNWRAPPING ){
                System.out.printf("Cannot be accessed because %s is %s at floor %3d%n",r.getIdTube(), r.getSpecialRobotState(),r.getCurrent_floor());
                return false;
            }

        }
        return true;
    }


    /**
     * checks if no other robots in current floor so robot can unwrap fragile item
     * @param floor
     * @return true if floor is safe to unwrap
     */
    public boolean isUnwrappingSafe(int floor){
        CautiousRobot r;
        int count = 0;
        for (int i = 0; i <nRobot ; i++) {
            r = (CautiousRobot) automail.robots[i];
            if (r.getCurrent_floor() == floor) count++;
        }
        if (count>1) return false;
        return true;
    }

    /**
     * checks if other robots are trying to deliver fragile item in current floor
     * @param floor
     * @return true if other robots are trying to deliver in current floor
     */
    public boolean waitingToDeliverFragileItem(int floor){
        int count = 0;
        for (int i = 0; i < nRobot; i++) {
            CautiousRobot r = (CautiousRobot) automail.robots[i];

            if (r.getDestination_floor() == floor && r.getCurrent_floor() == r.getDestination_floor() && r.isDeliveryItemFragile()){
                count++;
            }


        }
        if (count >1){
            return  true;
        }
        else{
            return false;
        }
    }

}
