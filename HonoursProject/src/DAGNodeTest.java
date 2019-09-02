import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.Assert.assertEquals;

public class DAGNodeTest {
    ///TODO: USE BEFORE TO initialise A THREAD POOL
//    DAGNode node;
//
//    @Before
//    public void setThreadPool() {
//        node = new DAGNode;
//    }

    @Test
    public void initalisedThreadPoolTest(){
        //expected values
        Queue<Integer> input = new LinkedList<>();
        Queue<Integer>[] output = new Queue[2];
        int taskTime = 350;
        //result
        DAGNode node = new DAGNode();
        node.setWeight(35);
        node.InitialiseThreadPool(input,output);

        assertEquals(input,node.getInputQueue());   //check input queues match
        assertEquals(output,node.getOutputQueue()); //check output queue arrays match
        assertEquals(taskTime,node.getWeight()*10);  //check task times match
        assertEquals(0,node.getThreads().size());   //check that thread there are no threads
    }

    @Test
    public void resizeThreadpoolTest(){
        DAGNode node = new DAGNode();
        node.InitialiseThreadPool(new LinkedList<>(), new Queue[2]);
        node.resizeThreadpool(2);
        assertEquals(2,node.getThreads().size());
        int maxThreads = Runtime.getRuntime().availableProcessors();
        node.resizeThreadpool(maxThreads+1);
        assertEquals(maxThreads,node.getThreads().size());
        node.resizeThreadpool(-1);
        assertEquals(0,node.getThreads().size());
    }
}