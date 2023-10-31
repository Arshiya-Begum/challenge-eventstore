package net.intelie.challenges;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventIteratorClass implements EventIterator
{
    /* Declaring list for traversing through events 
     * following the same delaration as done 
     * for data structire in EventStoreClass 
     */
    public List<Event> eventList = null;
    public Integer indexVariable = -1;
    public Event currentEvent = null;

    /* For logging of prints statements in test cases instatiateing
    *  Logger object
    */
    private static Logger LOGGER = LoggerFactory.getLogger(EventIteratorClass.class);

    /* Construcor of EventIteratorClass 
     * it takes list of events as input argument
     * that is instatiated to above declared list variable 
     * in order to iterate over the events
     */
    public EventIteratorClass(List<Event> inputEventsList)
    {
        this.eventList = inputEventsList;
    }

    /* Overriding moveNext method from EventIterator Interface.
     * This function returns boolean value true if moved to move
     * next element of the list successfully otherwise returns false. 
     * This is a mandatory function to be called before calling current
     * and remove function of EventIterator Class.
     */
    @Override
    public boolean moveNext()
    {
        // If the event list is empty or null then return false
        if(eventList == null || eventList.isEmpty() )
        {
            LOGGER.error("There are no events in the Event Storage!!!");
            currentEvent=null;
            return false;
        }
        else 
        {
            /* If current index of the event list is less than length of
            * event list then move to next element and return true
            */
            if(indexVariable < (eventList.size()-1))
            {
                indexVariable++;
                currentEvent = eventList.get(indexVariable);
                LOGGER.info("Event Index is moved to : " + indexVariable);
                return true;
            }
            else
            {
                /* current index of event list is equal to lenth of event
                * list, hence we reached the end of the list return false.
                */
                LOGGER.error("We have reached end of the Event Storage. No more events to retrieve!!!");
                currentEvent = null;
                return false;
            }
        }
    }

    /* Overriding current method from EventIterator Interface.
     * This function doesnt take any input arguments, 
     * returns the event object that holds the event pointed by
     * current index of event list.
     * moveNext method should be called before current method
     * so as to populate currentEvent variable pointing to the event 
     * present at current index of the event list.
     * If moveNext is not called before current function, currentEvent 
     * will be null there by null is returned and exeption will be thrown.
     */
    @Override
    public Event current()
    {
        /* Only when moveNext function is called before current function
        * currentEvent will be populated, if populated the return the
        * currentEvent 
        */
        if(currentEvent != null)
        {
            LOGGER.info("Current Event : " + currentEvent.type() + " with timestamp : " + currentEvent.timestamp());
            return currentEvent;
        }
        else
        {
            /* Exception is raised if moveNext function is not called before 
             * current function, to populate currentEvent.
            */
            throw new IllegalStateException("Move function is not called to get the index or move function return false!!");
        }
    }

    /* Overriding remove method from EventIterator Interface.
     * This function doesnt take any input arguments and doesnt
     * return anything.
     * moveNext method should be called before current method
     * so as to populate currentEvent variable pointing to the event 
     * present at current index of the event list, and the current Event
     * will be removed from the event list.
     * If moveNext is not called before current function, currentEvent 
     * will be null there by nothing will be removed and exeption will be thrown.
     */
    @Override
    public synchronized void remove()
    {
        /* Only when moveNext function is called before current function
        * currentEvent will be populated, so when currentEvent is populated
        * and eventList is also not null then only remove the currentEvent
        * of the event list.
        */
        if (eventList != null && currentEvent != null )
        {
            LOGGER.info("Removing Event : " + currentEvent);
            eventList.remove(currentEvent);
            currentEvent = null;
            indexVariable=indexVariable-1;
            LOGGER.info("Removed Event successfully!!!");
        }
        else
        {
            /* Exception is raised if moveNext function is not called before 
             * current function, to populate currentEvent.
             */
            throw new IllegalStateException("Move function is not called to get the index or move function return false!!");
        }
    }

    /* EventIterator interface is inherting AutoCloseable interface
     * So explicitly Overriding close() function to clear the list
     * instatiated in the constructed of this class
     */
    @Override
    public void close()
    {
        /* Clearing the eventList */
        eventList = null;
        indexVariable=-1;
        currentEvent = null;
    }
}