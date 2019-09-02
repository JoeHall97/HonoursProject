import org.junit.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DAGBenchmarkTest {
    @Test
    public void testParser()
            throws ParserConfigurationException, SAXException, IOException
    {
        DAGBenchmark testBenchmark = new DAGBenchmark();
        testBenchmark.parseDAG("XML_Files/TestDAG.xml");
        DAGNode[] dagOutput = testBenchmark.getDag();
        DAGNode[] expectedDAG = new DAGNode[5];
        int[] con1 = {2,3};
        int[] con2 = {4};
        int[] con3 = {5};
        expectedDAG[0] = new DAGNode(1,con1,15);
        expectedDAG[1] = new DAGNode(2,con2,20);
        expectedDAG[2] = new DAGNode(3,con3,35);
        expectedDAG[3] = new DAGNode(4,con3,15);
        expectedDAG[4] = new DAGNode(5,null,15);

        assert DAGEquals(expectedDAG,dagOutput);
    }

    ///Checks if the two given DAGs are the same (have the same number, connections and weighting)
    ///RETURNS: true if they are the same, false if they are not
    public boolean DAGEquals(DAGNode[] expectedDAGNode, DAGNode[] actualDAGNode) {
        if(expectedDAGNode.length!=actualDAGNode.length)
            return false;
        for(int i=0;i<expectedDAGNode.length;i++) {
            //check if node numbers are the same
            if(expectedDAGNode[i].getNodeNumber()!=actualDAGNode[i].getNodeNumber())
                return false;
            //check if the connections are the same
            if(expectedDAGNode[i].getConnections()!=null && actualDAGNode[i].getConnections()!=null) {
                if(expectedDAGNode[i].getConnections().length!=actualDAGNode[i].getConnections().length)
                    return false;
                for(int j=0;j<expectedDAGNode[i].getConnections().length;j++)
                    if(expectedDAGNode[i].getConnections()[j]!=actualDAGNode[i].getConnections()[j])
                        return false;
            }
            else if((expectedDAGNode[i].getConnections()==null && actualDAGNode[i].getConnections()!=null)
                    || (expectedDAGNode[i].getConnections()!=null && actualDAGNode[i].getConnections()==null))
                return false;
            //check if the node weights are the same
            if(expectedDAGNode[i].getWeight()!=actualDAGNode[i].getWeight())
                return false;
        }
        return true;
    }
}