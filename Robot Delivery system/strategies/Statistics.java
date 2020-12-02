package strategies;

import automail.CautiousRobot;
import automail.ISpeicalArm;
import automail.MailItem;

public class Statistics {

    private int numberOfNormalDelivery = 0;

    private int weightOfNormalDelivery = 0;
    private int weightOfFragileDelivery = 0;
    private int numberOfFragileDelivery = 0;
    private int timeSpentWrapping = 0;
    private int timeSpentUnwrapping = 0;



    private static Statistics instance;

    public static Statistics getInstance() {
        if (instance == null){
            instance = new Statistics();
        }
        return instance;
    }

    /**
     * Increments the statistics that are being tracked for an item
     * @param deliveredItem
     */
    public void addToStatistic(MailItem deliveredItem) {
        if (deliveredItem.isFragile()) {
            weightOfFragileDelivery += deliveredItem.getWeight();
            numberOfFragileDelivery++;
            timeSpentUnwrapping += ISpeicalArm.UNWRAPPING_TIME;
            timeSpentWrapping += ISpeicalArm.WRAPPING_TIME;
        } else {
            numberOfNormalDelivery++;
            weightOfNormalDelivery += deliveredItem.getWeight();

        }
    }

    /**
     * Prints the stats
     * @return
     */
    @Override
    public String toString() {
        String s = new String();
        s += "Total number of packages delivered normally: " + numberOfNormalDelivery + "\n";

        s += "Total weight of packages delivered normally: " + weightOfNormalDelivery + "\n";
        s += "Total number of packages delivered fragile: " + numberOfFragileDelivery + "\n";
        s += "Total weight of packages delivered fragile: " + weightOfFragileDelivery + "\n";
        s += "Total time spent on wrapping/unwrapping fragile items: " + (timeSpentWrapping+timeSpentUnwrapping);
        return s;
    }

    public void print(){
        System.out.println(Statistics.getInstance().toString());
    }
}
