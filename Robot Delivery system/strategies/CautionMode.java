package strategies;

import automail.CautiousRobot;
import automail.MailItem;
import automail.Robot;
import exceptions.BreakingFragileItemException;
import exceptions.ItemTooHeavyException;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

public class CautionMode extends MailPool{

    public CautionMode(int nRobots){
        super(nRobots);
    }


    @Override
    public void step() throws ItemTooHeavyException, BreakingFragileItemException {
        try{
            ListIterator<Robot> i = getRobots().listIterator();
            while (i.hasNext()) loadRobot(i);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    /**
     * loads the robot by placing normal items in hand and tube and special item in special arm and dispatch robot once
     * its done
     */
    protected void loadRobot(ListIterator<Robot> i) throws ItemTooHeavyException, BreakingFragileItemException {
        CautiousRobot robot = (CautiousRobot) i.next();
        assert (robot.isEmpty());
        int j = 0;
        while(getPool().size()> j && !robot.isFull()){
            if(getPool().get(j).mailItem.isFragile()){

                if (robot.getFragileDeliveryItems() == null){
                    robot.addToHand(getPool().remove(j).mailItem);
                }
                else{
                    j++;
                }
            }
            else{
                if (robot.getDeliveryItem()==null){
                    robot.addToHand(getPool().remove(j).mailItem);
                }
                else if (robot.getTube() == null){
                    robot.addToTube(getPool().remove(j).mailItem);
                }
                else{
                    j++;
                }

            }

        }

        if (!robot.isEmpty()) {
            robot.dispatch();
            i.remove();
        }
    }
}
