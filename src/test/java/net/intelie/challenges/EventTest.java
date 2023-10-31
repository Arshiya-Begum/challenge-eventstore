package net.intelie.challenges;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class EventTest {
    /* Declaring onjects for logging purpose */
    private static MemoryAppender memoryAppender;
    private static final String LOGGER_NAME = "net.intelie.challenges";

    /* Methods to be executed before testcase, where instantiation of 
     * objects for logging purpose are done here
     */
    @Before
    public void setup() {
        Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.INFO);
        logger.addAppender(memoryAppender);
        memoryAppender.start();

    }

    /* This method is executed after test cases execution is completed
     * So as to free memory allocations done in methods indicated to be 
     * executed before.
     */
    @After
    public void cleanUp() {
        memoryAppender.reset();
        memoryAppender.stop();
    }

    @Test
    public void thisIsAWarning() throws Exception {
        Event event = new Event("some_type", 123L);
        
        //THIS IS A WARNING:
        //Some of us (not everyone) are coverage freaks.
        assertEquals(123L, event.timestamp());
        assertEquals("some_type", event.type());
    }
    
    /* This method is a test cases where we are testing Inserting
     * events in the event store
     */
    @Test
    public void addEvents() throws Exception {

        Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
        Event event1 = new Event("READ_EVENT", timestamp1.getTime());

        Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
        Event event2 = new Event("WRITE_EVENT", timestamp2.getTime());

        Timestamp timestamp3 = new Timestamp(System.currentTimeMillis());
        Event event3 = new Event("UPDATE_EVENT", timestamp3.getTime());

        EventStoreClass eventStore1 = new EventStoreClass();
        eventStore1.insert(event1);
        eventStore1.insert(event2);
        eventStore1.insert(event3);

        /* Since 3 events are inserted into the event store
         * so checking if size of the event store is 3 then 
         * test case is passed
         */
        assertEquals(3,eventStore1.EventStoreList.size());
        
    }

    /* This method is a test cases where we are testing removeAll
     * events of a certain type from the event store
     */
    @Test
    public void removeEvents() throws Exception
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Event event = new Event("WRITE_EVENT", timestamp.getTime());

        // Inserting WRITE_EVENT into event store 
        EventStoreClass eventStore = new EventStoreClass();
        eventStore.insert(event);

        /* Checking if the size of event store is 1, there by 
         * indicating the above inserted event is added into 
         * the event store
         */
        assertEquals(1, eventStore.EventStoreList.size());

        /* Removing events of type WRITE_EVENT from event store
         */
        eventStore.removeAll("WRITE_EVENT");

        /* Since there was only one WRITE_EVENT in the event store
         * making the event store empty. hence checking for size of 
         * event store if 0 then pass the test.
         */
        assertTrue(eventStore.EventStoreList.size() == 0);
    }

    /* This method is a test cases where we are testing query method of
     * EventStoreClass and all the methods of EventIteratorClass.
     * We tried inserting few events and quering methods of 
     */
    @Test
    public void queryEvents() throws Exception
    {
        // defining differnt events with same time stamp
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Event event1 = new Event("READ_EVENT", timestamp.getTime());
        Event event2 = new Event("WRITE_EVENT", timestamp.getTime());
        Event event3 = new Event("UPDATE_EVENT", timestamp.getTime());

        // defining events with different timetamps
        Event event4 = new Event("READ_EVENT", timestamp.getTime()+(600L));
        Event event5 = new Event("READ_EVENT", timestamp.getTime()+(1800L));
        Event event6 = new Event("READ_EVENT", timestamp.getTime()+(3600L));

        // Inserting all the events in event store
        EventStoreClass eventStore = new EventStoreClass();
        eventStore.insert(event1);
        eventStore.insert(event2);
        eventStore.insert(event3);
        eventStore.insert(event4);
        eventStore.insert(event5);
        eventStore.insert(event6);

        /* Quering events of type "READ_EVENT" by invoking query method
         * of EventStoreClass, this function returns EventIterator object
         */
        EventIterator queryOutput = eventStore.query("READ_EVENT", timestamp.getTime(), timestamp.getTime()+(3600L));

        /* Testing the EventIteratorClass methods such as moveNext,current
         * and remove using the queryOutput object.
         * This object should have list of 4 READ_EVENT if the above 
         * EventStoreClass query method if successfull 
         */

        /* Since we have 4 READ_EVENT type events inserted in the event store.
         * moveNext method should return true as it is now pointing
         * to first event of 4 READ_EVENT events. Hence test should pass
         * if moveNext returns true.
         */
        assertTrue(queryOutput.moveNext());
        /* checking and testing the timestamp of the first READ_EVENT
         * using current method of EventIteratorClass
         */ 
        assertEquals(timestamp.getTime(),queryOutput.current().timestamp());
        
        /* Removing the first READ_EVENT from the queryOutput
         */
        queryOutput.remove();

        /* Calling current method immediatety after remove() method of 
         * EventIteratorClass, This should throw an IllegalStateException
         * exception
         */
        Throwable exceptionObj = assertThrows(IllegalStateException.class, () -> {queryOutput.current();});
        assertEquals("Move function is not called to get the index or move function return false!!", exceptionObj.getMessage());

        /* Traversing through remaining events(READ_EVENTS) of queryOutput 
         * and removing all the events from evets from queryOutput
         */
        while(queryOutput.moveNext())
        {
            assertEquals("READ_EVENT", queryOutput.current().type());
            // Removing event
            queryOutput.remove();
        }

        // Closing the queryOutput to clear the memory space
        queryOutput.close();

        /* Since all the events are removed from queryOutput
         * moveNext will now retrun false
         */
        assertFalse(queryOutput.moveNext());
    }

    @Test
    public void largeNumberOfEventsOperations() throws Exception
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Integer val=0;
        EventStoreClass eventStore = new EventStoreClass();

        /* Creating 100 events of timestamp with 600 difference
         * accross events and type UPDATE_EVENT.
         * Inerting events into event store
         */
        for (int i=0; i<100; i++)
        {
            Event event = new Event("UPDATE_EVENT", timestamp.getTime()+(600*i));
            eventStore.insert(event);
        }
        assertEquals(100, eventStore.EventStoreList.size());

        /* query events of type UPDATE_EVENT but time stamp
         * startime : current timestamp and endtime :currenttimestamp + 6000
         * which should be upto first 11 events from the event store 
         * (0 to 10 "i" variable events from the above loop)
         */
        EventIterator eventIteratorObj = eventStore.query("UPDATE_EVENT", timestamp.getTime(), timestamp.getTime()+(6000));
        
        /* Traversing through the eventIteratorObj 11 events
         * checking the timestamp to validate the events
         * Removing the events from eventIteratorObj
         */
        while(eventIteratorObj.moveNext())
        {
            assertEquals(timestamp.getTime()+(600*val), eventIteratorObj.current().timestamp());
            eventIteratorObj.remove();
            val++;
        }
        //Clearing the eventIteratorObj
        eventIteratorObj.close();

        // checking there are no elemets in the eventIteratorObj
        assertFalse(eventIteratorObj.moveNext());

        /* Removing all events from event store of type UPDATE_EVENT
         * which is all the 100 events. There by removing all the events
         */
        eventStore.removeAll("UPDATE_EVENT");

        // checking if the event store is empty
        assertEquals(0,eventStore.EventStoreList.size());
    }

    @Test
    public void noEventsSatisfyingQueryCriteria() throws Exception
    {
        EventStoreClass eventStore = new EventStoreClass();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        assertEquals(0,eventStore.EventStoreList.size());
        EventIterator evtIteratorObj = eventStore.query("MOUSE_EVENT", 0, timestamp.getTime());
        assertFalse(evtIteratorObj.moveNext());
    }

    @Test
    public void removingFromEmptyEventStore() throws Exception
    {
        EventStoreClass eventStore = new EventStoreClass();
        assertEquals(0,eventStore.EventStoreList.size());
        eventStore.removeAll("KEYBOARD_EVENT");
        /* To verify this test case, we can use debug logs 
         * where the logger info statments gives the complete details.
         * "Event Store is empty. There are no events to be removed." this
         * statement is displayed
         */
    }

    /* This test case is to test the method 
     * EventStoreClass.queryWithoutEventIteratorMethods. 
     * So the test case is similar to queryEvents() test case. 
     * In queryEvents() test case EventStoreClass.query() is called
     * but in queryWithoutEventIteratorMethodsTestCase() we called
     * EventStoreClass.queryWithoutEventIteratorMethods.
     */
    @Test
    public void queryWithoutEventIteratorMethodsTestCase() throws Exception
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        EventStoreClass eventStore = new EventStoreClass();
        /* Creating 1000 events of timestamp with 600 difference
         * accross events and type WINDOW_EVENT.
         * Inerting events into event store
         */
        for (int i=0; i<1000; i++)
        {
            Event event = new Event("WINDOW_EVENT", timestamp.getTime()+(600*i));
            eventStore.insert(event);
        }
        assertEquals(1000, eventStore.EventStoreList.size());
        /* Invoking queryWithoutEventIteratorMethods. 
         * The Logs are used to verify number of events queried.
         * 101 events should be queried after the below function call.
         */
        EventIterator eventIteratorObj = eventStore.queryWithoutEventIteratorMethods("WINDOW_EVENT", timestamp.getTime(), timestamp.getTime()+(60000));

        eventIteratorObj.close();
    }
}