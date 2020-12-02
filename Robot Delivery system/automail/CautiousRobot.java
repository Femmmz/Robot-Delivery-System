package automail;

import exceptions.BreakingFragileItemException;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import strategies.*;

/**
 * Parts of step() function have been copied from Robot.step().
 */

public class CautiousRobot extends Robot implements ISpeicalArm {


    public enum SpecialRobotState {NOT_HANDLING_FRAGILE_ITEM, UNWRAPPED, WRAPPING, WRAPPED, UNWRAPPING}

    // A enum stating the special robot state
    private SpecialRobotState specialRobotState;


    private Integer wrappingCompletedBy;
    private Integer unwrappingCompletedBy;

    private MailItem fragileDeliveryItems;
    private boolean isDeliveryItemFragile;

    public CautiousRobot(IMailDelivery delivery, CautionMode pool){
        super(delivery,pool);
        this.wrappingCompletedBy = null;
        this.unwrappingCompletedBy = null;
        this.fragileDeliveryItems = null;
        this.specialRobotState = SpecialRobotState.NOT_HANDLING_FRAGILE_ITEM;
        this.isDeliveryItemFragile = false;
    }

    /**
     * Checks if item is being wrapped
     * @return returns true if wrapping else false
     */
    @Override
    public boolean isWrapping() {
        if (wrappingCompletedBy != null){
            if (wrappingCompletedBy > Clock.Time()){
                return true;
            }

        }
        return false;
    }

    /**
     * Checks if fragile item is wrapped
     * @return returns true if wrapped else false
     */
    @Override
    public boolean isWrapped() {
        if (wrappingCompletedBy != null){
            if (wrappingCompletedBy <= Clock.Time()){
                return true;
            }
        }
        return false;
    }


    /**
     * Checks if fragile item is being unwrapped
     * @return returns true if unwrapping else false
     */
    @Override
    public boolean isUnwrapping() {
        if (unwrappingCompletedBy != null){
            if (unwrappingCompletedBy > Clock.Time()){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if fragile item is unwrapped
     * @return true if unwrapped else false
     */
    @Override
    public boolean isUnwrapped() {
        if (unwrappingCompletedBy != null){
            if (unwrappingCompletedBy <= Clock.Time()){
                return true;
            }
        }
        return false;
    }

    /**
     * tells cautious robot to start wrapping
     */
    @Override
    public void startWrapping() {
        assert(fragileDeliveryItems != null);
        this.wrappingCompletedBy = new Integer(Clock.Time()+WRAPPING_TIME);
        changeState(SpecialRobotState.WRAPPING);
    }

    /**
     * tells cautious robot to start unwrapping
     */
    @Override
    public void startUnwrapping() {
        assert(fragileDeliveryItems != null);
        assert(isUnwrapped());
        this.wrappingCompletedBy = null;
        this.unwrappingCompletedBy = new Integer(Clock.Time() + UNWRAPPING_TIME);
        changeState(SpecialRobotState.UNWRAPPING);
    }

    /**
     * A run function for robot that updates robot at each time step
     * @throws ExcessiveDeliveryException if delivery exceeds capacity which is 3 for special robot
     * @throws BreakingFragileItemException if there is a possibility of breaking fragile item
     */
    @Override
    public void step() throws ExcessiveDeliveryException, BreakingFragileItemException {
        switch(current_state) {
            /** This state is triggered when the robot is returning to the mailroom after a delivery */
            case RETURNING:
                /** If its current position is at the mailroom, then the robot should change state */
                if(getCurrent_floor() == Building.MAILROOM_LOCATION){
                    if (getTube() != null) {
                        ((CautionMode) getMailPool()).addToPool(getTube());
                        System.out.printf("T: %3d >  +addToPool [%s]%n", Clock.Time(), getTube().toString());
                        setTube(null);
                    }

                    if (this.fragileDeliveryItems != null) {
                        //  Return fragile item to mailPool
                    }

                    if (this.getDeliveryItem() != null){
                        //  Return delivery item to mailPool
                    }

                    /** Tell the sorter the robot is ready */
                    ((CautionMode)getMailPool()).registerWaiting(this);
                    changeState(RobotState.WAITING);
                } else {
                    /** If the robot is not at the mailroom floor yet, then move towards it! */
                    moveTowards(Building.MAILROOM_LOCATION);
                    break;
                }
            case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if(!isEmpty() && isReceivedDispatch()){
                    assert(this.specialRobotState == SpecialRobotState.NOT_HANDLING_FRAGILE_ITEM);
                    setDeliveryCounter(0); // reset delivery counter
                    setDeliveryItem();
                    setRoute();
                    changeState(RobotState.DELIVERING);
                    if (this.fragileDeliveryItems == null){
                        dispatched();


                    }
                } else{
                    if (InformationBroker.getInstance().waitingToDeliverFragileItem(Building.MAILROOM_LOCATION)){
                        moveTowards(Building.MAILROOM_LOCATION+1);
                        changeState(RobotState.RETURNING);
                    }
                }
                break;

            case DELIVERING:
                //System.out.println("InsideDelivery:\n" + this.toString());
                assert (RobotState.DELIVERING == current_state);
                switch (specialRobotState) {

                    case NOT_HANDLING_FRAGILE_ITEM:
                        assert (this.fragileDeliveryItems == null);
                        if (getCurrent_floor() == getDestination_floor()) { // If already here drop off either way
                            /** Delivery complete, report this to the simulator! */
                            delivery.deliver(getDeliveryItem());
                            setDeliveryItem(null);
                            incrementDeliveryCounter();
                            if (getDeliveryCounter() > 3) {  // Implies a simulation bug
                                throw new ExcessiveDeliveryException();
                            }
                            /** Check if want to return, i.e. if there is no item in the tube*/
                            if (getTube() == null) {
                                changeState(RobotState.RETURNING);
                            } else {
                                /** If there is another item, set the robot's route to the location to deliver the item */
                                setDeliveryItem(getTube());
                                setTube(null);
                                setRoute();
                                changeState(RobotState.DELIVERING);
                            }
                        } else {
                            /** The robot is not at the destination yet, move towards it! */
                            moveTowards(getDestination_floor());
                        }
                        break;

                    case WRAPPING:
                        assert (this.fragileDeliveryItems != null && !isWrapped());
                        if (getCurrent_floor() == Building.MAILROOM_LOCATION && isReceivedDispatch()){

                            if (isWrapped()){
                                changeState(SpecialRobotState.WRAPPED);
                                dispatched();
                            }
                            else{
                                break;
                            }
                        }
                        break;


                    case WRAPPED:
                        assert (this.fragileDeliveryItems != null && isWrapped() );

                        if (getCurrent_floor() == getDestination_floor()){

                            if (this.isDeliveryItemFragile){

                                //  Checks if ok to unwrap before unwrapping
                                if (InformationBroker.getInstance().isUnwrappingSafe(getCurrent_floor())) {
                                    startUnwrapping();
                                }
                                else if (InformationBroker.getInstance().waitingToDeliverFragileItem(getCurrent_floor())){
                                    moveTowards(getCurrent_floor()+1);
                                }

                                break;
                            }
                            else{

                                delivery.deliver(this.getDeliveryItem());
                                setDeliveryItem(null);
                                if (getTube() == null && this.fragileDeliveryItems == null){
                                    changeState(RobotState.RETURNING);
                                    break;
                                }
                                setDeliveryItem(getTube());
                                setTube(null);
                                setDeliveryItem();
                                setDestination_floor();
                                changeState(RobotState.DELIVERING);

                            }

                        }
                        else{
                            //  Checks if ok to move before moving

                            moveTowards(getDestination_floor());
                            break;
                        }
                        break;


                    case UNWRAPPING:

                        assert (this.fragileDeliveryItems != null && (isUnwrapped() || isUnwrapping()));

                        if (isUnwrapped()){
                            changeState(SpecialRobotState.UNWRAPPED);
                        }
                        break;

                    case UNWRAPPED:
                        assert (this.fragileDeliveryItems != null && isUnwrapped());

                        if (getCurrent_floor() == Building.MAILROOM_LOCATION && isReceivedDispatch()){

                            startWrapping();
                            break;
                        }

                        if (getCurrent_floor() == getDestination_floor()){

                            //  if not delivering fragile Item then fragile item cannot be unwrapped
                            if (!this.isDeliveryItemFragile){throw new BreakingFragileItemException();}

                            //  Deliver unwrapped fragile item
                            delivery.deliver(this.fragileDeliveryItems);
                            fragileDeliveryItems = null;
                            changeState(SpecialRobotState.NOT_HANDLING_FRAGILE_ITEM);
                            //  increase number of items delivered
                            incrementDeliveryCounter();

                            if (getDeliveryCounter() > 3) {  // Implies a simulation bug
                                throw new ExcessiveDeliveryException();
                            }
                            if (!isEmpty()) {
                                setDeliveryItem();
                                setRoute();
                                changeState(RobotState.DELIVERING);

                            }
                            else{
                                changeState(RobotState.RETURNING);
                            }

                        }
                        break;

                }
        }
    }

    /**
     * checks if robot is empty
     * @return true if not holding any item else false
     */
    @Override
    public boolean isEmpty() {
        return super.isEmpty() && (this.fragileDeliveryItems == null);
    }

    /**
     * sets delivery item to the closest item to the robot
     */
    protected void setDeliveryItem(){
        assert(!isEmpty());
        if (getDeliveryItem() == null){
            this.isDeliveryItemFragile = true;
        }

        else if (this.fragileDeliveryItems == null){
            this.isDeliveryItemFragile = false;
        }
        else {
            if (getDeliveryItem().destination_floor > fragileDeliveryItems.destination_floor) {
                this.isDeliveryItemFragile = true;
            } else {
                this.isDeliveryItemFragile = false;
            }
        }
    }

    /**
     * Changes special state to the next special state
     * @param nextState a special state
     */
    protected void changeState(SpecialRobotState nextState) {

        System.out.printf("T: %3d > %7s special arm changed from %s to %s%n", Clock.Time(), getIdTube(), this.specialRobotState, nextState);
        this.specialRobotState = nextState;
    }

    /**
     * sets route to the new destination
     */
    @Override
    protected void setRoute() {
        if (this.isDeliveryItemFragile){
            setDestination_floor(this.fragileDeliveryItems.destination_floor);
        }
        else{
            setDestination_floor(this.getDeliveryItem().destination_floor);
        }
    }

    /**
     * adds item to hand. If item is fragile adds to special arm else to normal arm
     * @param mailItem : item that needs to be added to hand
     * @throws ItemTooHeavyException if item is heavier than robot limit
     * @throws BreakingFragileItemException if special arm already holding fragile item
     */
    @Override
    public void addToHand(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {
        try{
            super.addToHand(mailItem);
        }
        catch (BreakingFragileItemException e){

            if (fragileDeliveryItems != null){ throw new BreakingFragileItemException();}
            this.fragileDeliveryItems = mailItem;
            changeState(SpecialRobotState.UNWRAPPED);
        }
    }

    /**
     * Robot moves towards the provided destination
     * @param destination the floor towards which the robot is moving
     */
    @Override
    protected void moveTowards(int destination) {

        if (InformationBroker.getInstance().canMoveToNextLevel(getNextFloor(destination))) {
            super.moveTowards(destination);
        }
        else{
            System.out.printf("T: %3d > %7s Cannot access %3d floor, %n%s", Clock.Time(), getIdTube(), getNextFloor(destination), this.toString());
        }
    }

    /**
     * Returns next floor in the robots path
     * @param destination the floor towards which the robot is moving
     * @return next floor
     */
    public int getNextFloor(int destination){
        assert(getCurrent_floor() != destination);
        if (getCurrent_floor() < destination) {
            return getCurrent_floor() + 1;
        }
        else{
            return getCurrent_floor() -1;
        }

    }


    /**
     * Sets destination floor for the robot
     */
    public void setDestination_floor() {
        if (this.isDeliveryItemFragile){
            super.setDestination_floor(this.fragileDeliveryItems.destination_floor);
        }
        else{
            super.setDestination_floor(this.getDeliveryItem().destination_floor);
        }
    }

    /**
     * checks if robot is holding items at max capacity
     * @return if holding max items returns true, else false
     */
    @Override
    public boolean isFull() {
        return super.isFull() && this.fragileDeliveryItems != null;
    }

    /**
     * gets item in special arm
     * @return item in specail arm
     */
    public MailItem getFragileDeliveryItems() {
        return fragileDeliveryItems;
    }

    /**
     * Converts robot to a printable string
     * @return string describing robot class
     */
    @Override
    public String toString() {
        String s = super.toString();
        s += "\n";
        s += "SpecialRobotState: ";
        switch (specialRobotState){
            case WRAPPED:
                s+= " WRAPPED";
                break;
            case UNWRAPPED:
                s+= " UNWRAPPED";
                break;
            case NOT_HANDLING_FRAGILE_ITEM:
                s+= " N/A";
                break;

            case UNWRAPPING:
                s+= " UNWRAPPING";
                break;
            case WRAPPING:
                s+= " WRAPPING";
        }
        s+="\n\tSpecialArm: " + this.fragileDeliveryItems + "\n";
        return s;
    }

    /**
     * prints out the change in the state (considers for special arm)
     * @param nextState the state to which the robot is transitioning
     */
    @Override
    protected void changeState(RobotState nextState) {
        assert(!(getDeliveryItem() == null && getTube() != null) && this.fragileDeliveryItems != null);
        if (current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), current_state, nextState);
        }
        current_state = nextState;
        if(nextState == RobotState.DELIVERING) {
            if (isDeliveryItemFragile) {
                System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdTube(), this.fragileDeliveryItems.toString());
            } else {
                System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdTube(), getDeliveryItem().toString());

            }
        }
    }

    /**
     * getter for srobot special state
     * @return special state of the robot
     */
    public SpecialRobotState getSpecialRobotState() {
        return specialRobotState;
    }

    /**
     * Checks if current item to be delivered is fragile
     * @return returns true if current delivery item is fragile else false
     */
    public boolean isDeliveryItemFragile() {
        return isDeliveryItemFragile;
    }

}
