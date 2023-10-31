package net.intelie.challenges;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventStoreClass implements EventStore 
{    
    /* 
     * First Step is to declaring storage for stroing events.
     * Using ArrayList data sturcure to store event objects.
     * As it stores its elements in ordered fashion, easily resizeable
     * and the operations required in this current assignement 
     * requires only insert and remove operations on the data storage 
     * so not using ArrayList.
     * Using synchronizedList to ensure thread safety 
     * only one thread to access the Event Store List 
     * at any given point of time.
     */
    public final List<Event> EventStoreList = Collections.synchronizedList(new ArrayList<Event>());

    /* For logging of prints statements in test cases instatiating
     * Logger object. Using slf4j for logging and verification of 
     * test cases using the print statements, which can be viewed in
     * Debug console.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(EventStoreClass.class);

    /* Overriding insert method from EventStore Interface.
     * Making it synchronized in order to ensure
     * only one thread at any given point of time insert 
     * new events into the Event Store List.
     * This function doesnt return anything 
     * adds the event passed as argument into the EventStore List
     */
    @Override
    public synchronized void insert(Event event)
    {
        LOGGER.info("Inserting event...");
        EventStoreList.add(event);
        LOGGER.info("Event : " + event.type() + " with timestamp : " + event.timestamp()+ " inserted successfully!");
    }

    /* Overriding removeAll method from EventStore Interface.
     * Making it synchronized in order to ensure
     * only one thread at any given point of time removes events 
     * from the Event Store List.
     * This function doesnt return anything 
     * removes all the event of given event typepassed as argument 
     * from the EventStore List.
     */
    @Override
    public synchronized void removeAll(String type)
    {
        if (EventStoreList.size() == 0)
        {
            LOGGER.info("Event Store is empty. There are no events to be removed.");
        }
        else {
            LOGGER.info("Number of events in Event Store before removing events of type " + type + " : " + EventStoreList.size());
            LOGGER.info("Removing all the events of type : " + type);
            EventStoreList.removeIf(event -> event.type().equals(type));
            LOGGER.info("Number of events in Event Storage after removing events of type " + type + " : " + EventStoreList.size());
        }
    }

    /* Overriding query method from EventStore Interface.
     * Here thread safety is implemented when using the
     * Event Store List, so that event store list is picked from
     * memory instead of thread cache for each thread.
     * No making sure no matter how many threads are accessing
     * the event store list but every threat accessing the list from 
     * memory, so that the list of events of specific type is correct 
     * accross all the threads.
     * This function doesnt returns EventIterator object that contains
     * List of event that satisfy the criteria of type and 
     * start and end timestamp input arguments
     */
    @Override
    public EventIterator query(String type, long startTime, long endTime)
    {
        EventIteratorClass eventIteratorClassObj = new EventIteratorClass(Collections.synchronizedList(EventStoreList));
        Event currentEvent=null;
        List<Event> givenTypeEventList = new ArrayList<Event>();

        /* Traversing through the event store list by using moveNext method of
         * EventIteratorClass
        */
        while(eventIteratorClassObj.moveNext())
        {
            /* Extracting event present at the current index using current method
             * of EventIteratorClass
             */
            currentEvent=eventIteratorClassObj.current();
            /* If type of the current event matches the given input argument type value
             * then check for timestamps
             */
            if(currentEvent.type() == type)
            {
                /* If timestamp of the current event is within the range of startTime 
                 * and endTime provided in the input arguments then add this current
                 * event in the output list
                 */
                if((currentEvent.timestamp() >= startTime) && (currentEvent.timestamp() <= endTime))
                {
                    givenTypeEventList.add(currentEvent);
                }
            }
        }
        // Clearing variables of EventIteratorClass 
        eventIteratorClassObj.close();

        /* EventStoreList which contains all the events in the event store are passed as
         * argument during instantiating EventIteratorClass object. There by instatiating 
         * eventlist of EventIteratorClass to EventStoreList. But now after segregating 
         * events of given input type, reassigning eventList to the segerated list.
         * So that we can return the same EventIteratorClass object. Avoiding creation of 
         * a new EventIteratorClass object or calling the EventIteratorClass constructor again.
         */
        eventIteratorClassObj.eventList = givenTypeEventList;

        LOGGER.info("Number of events of type : " + type + " are : " + eventIteratorClassObj.eventList.size());

        return eventIteratorClassObj;
    }
    /* NOTE : The above implementation of query method is not the efficient one.
     * As this method has while loop traversing through the entire EventStoreList.
     * Time complexity of O(n).
     * This method was written with the intention of utilizing all the 
     * methods implemented in EventIteratorClass such as moveNext, current and close.
     */

    /*
     * The intention of this method is same as above query method of this class,
     * to query events satisfying criteria of input arguments event type and 
     * timestamp within start and endTime. But with less time and space complexity 
     * compared to query method. 
     */
    public EventIterator queryWithoutEventIteratorMethods(String type, long startTime, long endTime)
    {
        EventIteratorClass eventIteratorClsObj = new EventIteratorClass(Collections.synchronizedList(EventStoreList
        .stream()
        .filter(event -> event.type().equals(type))
        .filter(event ->(event.timestamp() >= startTime && event.timestamp() <= endTime))
        .collect(Collectors.toList())));

        LOGGER.info("Number of events of type : " + type + " are : " + eventIteratorClsObj.eventList.size());

        return eventIteratorClsObj;
    }
}


