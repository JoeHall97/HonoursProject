import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/// A thread that simulates a computation
class TaskThread implements Runnable {
    private int timeToRun;

    /* Constructor */
    public TaskThread(int timeToRun){
        this.timeToRun = timeToRun;
    }

    public void run()
    {
        try {
            Thread.sleep(timeToRun);
        } catch (InterruptedException ie){
            System.err.println(ie);
        }
    }
}

///https://www.jeejava.com/custom-thread-pool-in-java/
/// A Node in the Directed A-cyclic Graph
class DAGNode {
    /* DAG VARIABLES */
    //the computational weight of the node
    private int weight;
    //the node's number
    private int nodeNumber;
    //the nodes the node is connected to
    private int[] connections;
    //the longest connection length to node
    private int chainLength;
    /* THREADPOOL VARIABLES */
    private Queue<Integer> inputQueue;
    private Queue<Integer>[] outputQueue;
    //worker threads
    private List<TaskThread> threads;
    //has the thread pool finished or not
    private boolean isFinished = false;
    private int taskCount = 0;
    private int numInputs = 0;
    //Executor service for the thread pool
    private ExecutorService executor;
    //probability of error
    private int errorProb = 10;
    private Random rand = new Random();
    private static final int  MAX_THREADS = Runtime.getRuntime().availableProcessors();

    /* Constructors */
    public DAGNode(){        }

    public DAGNode(int number, int[] connections, int weight) {
        this.nodeNumber = number;
        this.connections = connections;
        this.weight = weight;
    }

    // Sets up the thread pool for execution
    public void InitialiseThreadPool(Queue<Integer> input, Queue<Integer>[] output){
        this.threads = new ArrayList<TaskThread>(0);
        this.inputQueue = input;
        this.outputQueue = output;
    }

    /// Executes all of the threads in the threadpool
    /// RETURNS: the size of the input queue
    public int executeThreadPool(){
        if(!isFinished) {    //if the threadpool hasn't finished all the tasks in the input queue
            if(threads.size()==0) { return inputQueue.size(); } //if there's no threads in the thread pool
            executor = Executors.newFixedThreadPool(threads.size());
            for (TaskThread t : threads) {
                int currProb = rand.nextInt(100)+1;  //generate a number between 1 and 100
                if(currProb<=errorProb) {   //if the generated value is less than the error probability, don't execute
                    continue;
                }
                //read from input queue
                for(int i=0;i<numInputs;i++) {
                    if(inputQueue.size()==0) {   //if the input queue is empty, error out
                        for(int j=0;j<i;j++)    //add popped off inputs back into the queue
                            inputQueue.add(10);
                        //wait for threads to finish, then return
                        executor.shutdown();
                        return inputQueue.size();
                    }
                    inputQueue.remove();    //pop input off queue
                }
                //execute the task
                executor.execute(t);
                //add to the output queue
                if(outputQueue!=null) {
                    for (int i = 0; i < outputQueue.length; i++)  //put input onto each output queue
                        outputQueue[i].add(10);
                }
                taskCount++;
                if (taskCount == 100) {  //if the threadpool has executed all of the required task, set finished and exit out
                    isFinished = true;
                    break;
                }
            }
            //shutdown the executor and return
            executor.shutdown();
            return inputQueue.size();
        }
        //if the thread pool has finished all of its tasks, return 0
        return 0;
    }

    // Print out the DAG's information
    public void printNode(){
        System.out.println("Node Number: " + nodeNumber);
        System.out.println("Connections: ");
        if(connections!=null)
            for(int i=0;i<connections.length;i++)
                System.out.println("    " + connections[i]);
        else
            System.out.println("    None");
        System.out.println("Computational Weight: " + weight + "%");
        System.out.println("Fail Rate: " + errorProb);
    }

    /// Changes the number of threads in the threadpool
    public void resizeThreadpool(int size) {
        //if the given number of threads is between 0 and the max number of threads, assign the given number
        if(size>=0 && size<=MAX_THREADS) {
            this.threads = new ArrayList<TaskThread>(size);
            for (int i = 0; i < size; i++)
                this.threads.add(new TaskThread(weight*40));
        }
        //if the given number of threads is larger than the max number of threads, assign the max
        else if(size>MAX_THREADS) {
            this.threads = new ArrayList<TaskThread>(MAX_THREADS);
            for(int i=0;i<MAX_THREADS;i++)
                this.threads.add(new TaskThread(weight*40));
        }
        //if the given number of threads is negative, assign zero
        else if(size<0)
            this.threads = new ArrayList<TaskThread>(0);
    }

    /* Gets and Sets */
    public int getChainLength() { return chainLength; }

    public void setChainLength(int chainLength) { this.chainLength = chainLength; }

    public int[] getConnections() { return connections; }

    public void setConnections(int[] connections) { this.connections = connections; }

    public int getWeight(){ return weight; }

    public void setWeight(int weight) { this.weight = weight; }

    public void setTaskCount(int count) { this.taskCount = count; }

    public int getNodeNumber() { return nodeNumber; }

    public void setNodeNumber(int number) { this.nodeNumber = number; }

    public void setFinished(boolean isFinished) { this.isFinished = isFinished; }

    public boolean getFinished() { return isFinished; }

    public Queue<Integer>[] getOutputQueue() { return outputQueue; }

    public List<TaskThread> getThreads() { return threads; }

    public Queue<Integer> getInputQueue() { return inputQueue; }

    public void setNumInputs(int inputs){ this.numInputs = inputs; }

    public int getNumInputs() { return numInputs; }

    public ExecutorService getExecutor() { return executor; }

    public int getErrorProb() { return errorProb; }

    public void setErrorProb(int prob) { this.errorProb = prob; }
}

public class DAGBenchmark {
    private static boolean debug = false;
    private static DAGNode[] dag;
    private static PIDController controller;
    private static int largestTime; //the longest time to execute out of all of the nodes in the DAG
    private static boolean[] finished;  //the nodes that have finished executing
    private static int numConnections = 1;
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();  //maximum number of threads

    /* Constructor */
    public DAGBenchmark() {    }

    public static void main(String[] args)
        throws ParserConfigurationException,SAXException,IOException
    {
        if(args.length!=5 && args.length!=6 && args.length!=8 && args.length!=9){
            System.err.println("Usage: java DAGBenchmark <debug> <fileName> <outputFolder> <numRuns> <executeStrat> <errorProb> <P-value> <I-value> <D-value>");
            return;
        }
        String fileName, outputFolder;
        int exeStrat, numRuns, errorProb;
        double kp, ki, kd;
        //if the debug option is set
        if(args.length==6 || args.length==9){
            if(args[0].compareTo("-d")!=0) {
                System.err.println("Usage: java DAGBenchmark -d <fileName> <outputFolder> <numRuns> <executeStrat> <errorProb> <P-value> <I-value> <D-value>");
                return;
            }
            debug = true;
            fileName = args[1];
            outputFolder = args[2];
            numRuns = Integer.parseInt(args[3]);
            exeStrat = Integer.parseInt(args[4]);
            errorProb = Integer.parseInt(args[5]);
            if(args.length==9) {
                kp = Double.parseDouble(args[6]);
                ki = Double.parseDouble(args[7]);
                kd = Double.parseDouble(args[8]);
            }
            else {
                kp = 1;
                ki = 1;
                kd = 0;
            }
        }
        //if the debug option wasn't set
        else {
            fileName = args[0];
            outputFolder = args[1];
            numRuns = Integer.parseInt(args[2]);
            exeStrat = Integer.parseInt(args[3]);
            errorProb = Integer.parseInt(args[4]);
            if(args.length==8) {
                kp = Double.parseDouble(args[5]);
                ki = Double.parseDouble(args[6]);
                kd = Double.parseDouble(args[7]);
            }
            else {
                kp = -1;
                ki = -1;
                kd = 0;
            }
        }
        parseDAG(fileName);
        if(debug)
            printDAG();
        //execute the DAG the given number of times
        for(int i=0;i<numRuns;i++) {
            System.out.println("EXECUTING DAG #" + i);
            controller = new PIDController(kp,ki,kd,dag.length);    //initialise a new controller for each run
            executeDAG(outputFolder, i, exeStrat, errorProb);
        }
    }

    /// Print out the information about the DAG
    private static void printDAG() {
        System.out.println("-------------- DAG --------------");
        System.out.println();   //Padding
        for(int i=0;i<dag.length;i++) {
            dag[i].printNode();     //print DAG information
            System.out.println();   //Padding
        }
        for(int i=0;i<dag.length;i++)
            System.out.println("Node " + dag[i].getNodeNumber() + " task length: " + (100*dag[i].getWeight()));
        System.out.println();   //Padding
        System.out.println("-------------- DAG --------------");
    }

    ///Executes the given Directed A-cyclic Graph, writing the outputs to a given folder
    private static void executeDAG(String outputFolderName, int runNumber, int executeStrategy, int errorProbability)
	throws IOException
    {
        int[] setpoint = new int[dag.length];
        finished = new boolean[dag.length];
        initialseThreadPools(errorProbability);

        /* Execute the thread pools */
        int[] inputThreads = new int[dag.length];
        int[] outputQueues = new int[dag.length];
        int count = 0;
        final int A = 8;   //magnitude
        final int SCALEFACTOR = 20; //frequency
        //output files
        FileWriter timeWriter = new FileWriter(new File(outputFolderName + "times" +  runNumber + ".csv"));
        FileWriter inputWriter = new FileWriter(new File(outputFolderName + "input" +  runNumber + ".csv"));
        FileWriter outputWriter = new FileWriter(new File(outputFolderName + "output" +  runNumber + ".csv"));
        long startTime = System.currentTimeMillis();    //start time
        long[] times = new long[dag.length+1];          //times for each of the threadpools

        while(!dag[dag.length-1].getFinished()) {   //while the last node hasn't finished executing
            StringBuilder inputBuilder = new StringBuilder();
            StringBuilder outputBuilder = new StringBuilder();
            List<ExecutorService> executors = new ArrayList<>();    //executors from the thread pools
            
	    //add inputs based on a sine wave
	    double radians = (Math.PI / SCALEFACTOR) * count;
	    int wave = (int)(A * Math.sin(radians)) + 1;
	    if(!dag[0].getFinished()) {     //if the first node hasn't finished, keep adding items to its' input
                for(int j=0;j<wave;j++)
                    dag[0].getInputQueue().add(10);

	    }
	    //calculate the set-point
	    for(int i=0;i<setpoint.length;i++) {
		if(!dag[i].getFinished())
		    setpoint[i] = (wave * getW(count)) / dag[i].getChainLength();
		else
		    setpoint[i] = 0;
	    }
	    count++;
	    if(debug && executeStrategy==1)
		System.out.print("SETPOINT : [");
            for(int i=0;i<dag.length;i++) {    //for each node in the dag, execute it
            if(debug && executeStrategy==1)
                System.out.print(setpoint[i] + " ");
                    inputBuilder.append(inputThreads[i]);
                    dag[i].resizeThreadpool(inputThreads[i]);
                    outputQueues[i] = dag[i].executeThreadPool();
                    if(dag[i].getFinished())
                        finished[i] = true;
                    //if the thread pool is executing, add it to the list of executors
                    if(inputThreads[i]>0)
                        executors.add(dag[i].getExecutor());
                    outputBuilder.append(outputQueues[i]);
                    if(inputThreads.length-1>i) {
                        inputBuilder.append(',');
                        outputBuilder.append(',');
                    }
                    if(dag[i].getFinished() && times[i]==0) {
                        times[i] = System.currentTimeMillis() - startTime;
                        if(debug)
                            System.out.println("Node " + i + ": " + times[i] / 1e3 + " seconds.");
                    }
                }
            if(debug && executeStrategy==1)
            System.out.println(']');
            for(ExecutorService es: executors)  //wait for all the thread pools to finish executing
                    while(!es.isTerminated()) {    }
                if(debug){
                    //print input queue lengths after execution
                    System.out.print("OUTPUT:[");
                    for(int i=0;i<outputQueues.length;i++) {
                        if (i < outputQueues.length - 1)
                            System.out.print(outputQueues[i] + ",");
                        else
                            System.out.print(outputQueues[i]);
                    }
                    System.out.println(']');
                    System.out.println();   //padding
                    //print num. threads for each thread pool
                    System.out.print("INPUT: [");
                    for(int i=0;i<inputThreads.length;i++) {
                        if(i<inputThreads.length-1)
                            System.out.print(inputThreads[i] + ",");
                        else
                            System.out.print(inputThreads[i]);
                    }
                    System.out.println(']');
            }
            //write to the input and output csv files
            inputBuilder.append('\n');
            outputBuilder.append('\n');
            inputWriter.write(inputBuilder.toString());
            outputWriter.write(outputBuilder.toString());

            //get the threads for the next execution of the DAG nodes
            if(executeStrategy==0)
                inputThreads = pipelineExecute(outputQueues);
            else if(executeStrategy==1)
                inputThreads = proportionalExecute(outputQueues);
            else if(executeStrategy==2)
                inputThreads = clampInput(pidExecute(outputQueues,setpoint));
        }
        times[times.length-1] = System.currentTimeMillis() - startTime; //total time to execute the DAG
        if(debug)
            System.out.println("Total DAG Time: " + times[times.length-1]/1e3 + " seconds.");
        //output the times to the csv file
        for(int i=0;i<times.length;i++) {
            timeWriter.write((times[i]/1e3)+"");
            if(i<times.length-1)
                timeWriter.write(',');
        }
        timeWriter.write('\n');
        timeWriter.flush();
        inputWriter.write('\n');
        inputWriter.flush();
        outputWriter.write('\n');
        outputWriter.flush();
        timeWriter.close();
        inputWriter.close();
        outputWriter.close();
    }

    ///TODO: Adjust W
    private static int getW(int i) {
        if(i<10)
            return 10;
        else if(i<25)
            return 35;
        else if(i<50)
            return 55;
        else if(i<75)
            return 75;
        return 95;
    }

    /// Ensures that the output of the PID controller doesn't over allocate threads
    ///RETURNS: the number of threads for each node, in an array
    private static int[] clampInput(int[] input) {
        if(debug)
            System.out.print("CONTROLLER OUTPUT: [ ");
        int sum = 0;
        //calculate the total number of allocated threads
        for(int in:input) {
            if(debug)
                System.out.print(in + " ");
            if(in>0)
                sum += in;
        }
        if(debug)
            System.out.println(']');
        //if the total number of threads is less than or equal to the maximum, return
        if(sum<=NUM_THREADS)
            return input;
        //if the total number of threads is greater than the maximum
        else{
            int[] in = new int[input.length];
            boolean[] increased = new boolean[in.length];
            for(int i=0;i<input.length;i++) {
                if(input[i]>0)
                    //scale the input based on it's percentage of the total
                    in[i] = (int)((double)NUM_THREADS*((double)input[i]/(double)sum));
            }
            //if all threads haven't been allocated, allocate them based on the size/number of threads
            while (Arrays.stream(in).sum()<NUM_THREADS) {
                int largeIndex = 0;
                int largestValue = 0;
                for(int i=0;i<in.length;i++) {
                    if(in[i]>largestValue && !increased[i]) {
                        largestValue = in[i];
                        largeIndex = i;
                    }
                }
                in[largeIndex]++;
                increased[largeIndex] = true;
            }
            return in;
        }
    }

    /// Initialises all of the thread pools in the DAG
    private static void initialseThreadPools(int errorProbability) {
        /* Initialise the thread pools and input/output queues */
        Queue<Integer>[] queues = new Queue[numConnections];
        for(int i=0;i<dag.length;i++) {
            dag[i].setErrorProb(errorProbability);
            dag[i].setNumInputs(0);
        }
        for(int i=0;i<numConnections;i++)   //initialise queues
            queues[i] = new LinkedList<>();
        for(int i=0;i<dag.length;i++) {     //initialise the threadpools
            dag[i].setFinished(false);
            dag[i].setTaskCount(0);
            finished[i] = false;
            if(i==0){ //if the node is the first node
                Queue<Integer>[] outputQueues = new Queue[dag[i].getConnections().length];
                for(int j=0;j<outputQueues.length;j++) {
                    outputQueues[j] = queues[dag[i].getConnections()[j] - 1];
                    //set the number of inputs for the child/downstream nodes
                    int pos = dag[i].getConnections()[j]-1;
                    dag[pos].setNumInputs(dag[pos].getNumInputs()+1);
                }
                dag[i].InitialiseThreadPool(queues[i],outputQueues);
                dag[i].setNumInputs(1); //manually set the number of inputs for the first node
            }
            else {
                Queue<Integer>[] outputQueues = null;
                if(dag[i].getConnections() != null) {
                    outputQueues = new Queue[dag[i].getConnections().length];
                    for (int j = 0; j < outputQueues.length; j++) {
                        outputQueues[j] = queues[dag[i].getConnections()[j] - 1];
                        //set the number of inputs for the child/downstream nodes
                        int pos = dag[i].getConnections()[j]-1;
                        dag[pos].setNumInputs(dag[pos].getNumInputs()+1);
                    }
                }
                dag[i].InitialiseThreadPool(queues[i], outputQueues);
            }
        }
    }

    /// Allocates threads based on the size of the input queues of the various nodes
    /// RETURNS: the number of threads for each node, in an array
    public static int[] proportionalExecute(int[] outputVals) {
        int[] input = new int[outputVals.length];
        int count = 0;
        for(int i=0;i<outputVals.length;i++)
            if(outputVals[i]>0)
                count+=outputVals[i];
        for(int i=0;i<input.length;i++)
            if(outputVals[i]>0)
                input[i] = NUM_THREADS*(count/outputVals[i]);
        return input;
    }

    /// Allocates threads based on the output from the PID controller
    /// RETURNS: the number of threads for each node, in an array
    public static int[] pidExecute(int[] outputVals, int[] setpoint) {
        int[] error = new int[setpoint.length];
        for(int i=0;i<setpoint.length;i++)
            error[i] = setpoint[i] - outputVals[i];
        return controller.work(error);
    }

    /// Allocates all threads evenly (if possible) across all nodes
    /// RETURNS: the number of threads for each node, in an array
    public static int[] pipelineExecute(int[] outputVals) {
        int[] input = new int[outputVals.length];
        int count = 0;
        for(int i=0;i<input.length;i++) {
            input[i] = NUM_THREADS/input.length;
            count+=NUM_THREADS/input.length;
        }
        // if all threads haven't been allocated, give the remaining threads to the nodes
        if(count<NUM_THREADS) {
            for(int i=0;i<input.length;i++) {
                if(outputVals[i]>0) {
                    input[i]++;
                    count++;
                    if(count==NUM_THREADS)
                        break;
                }
            }
        }
        return input;
    }

    /// Parses in a DAG from an XML file
    public static void parseDAG(String fileName)
        throws ParserConfigurationException,SAXException,IOException
    {
        if(!fileName.contains(".xml")){
            System.err.println("File not an xml file.");
            System.exit(1);
        }
        largestTime = 0;
        //https://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
        File file = new File(fileName);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);

        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("DAGNode");
        dag = new DAGNode[nodeList.getLength()];

        for(int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);

            if(node.getNodeType()==Node.ELEMENT_NODE){
                Element element = (Element) node;

                dag[i] = new DAGNode();
                dag[i].setNodeNumber(Integer.parseInt(element.getElementsByTagName("name").item(0).getTextContent()));
                String[] connections = element.getElementsByTagName("connections").item(0).getTextContent().split(",");
                int[] intConnections = null;
                if(connections[0].compareTo("")!=0) {   //if connections have been given
                    intConnections = new int[connections.length];
                    for (int j = 0; j < intConnections.length; j++)
                        intConnections[j] = Integer.parseInt(connections[j]);
                }
                dag[i].setConnections(intConnections);
                dag[i].setChainLength(Integer.parseInt(element.getElementsByTagName("chainLength").item(0).getTextContent()));
                dag[i].setWeight(Integer.parseInt(element.getElementsByTagName("weight").item(0).getTextContent()));
                if(dag[i].getWeight()>largestTime)
                    largestTime = dag[i].getWeight();

                if(dag[i].getConnections()!=null)
                    numConnections += dag[i].getConnections().length;
            }
        }
        largestTime = largestTime*40;
    }

    /* Gets and Sets */
    public static DAGNode[] getDag() { return dag; }
}

class PIDController {
    double kp,ki,kd;
    int[] totalError,prevError;

    public PIDController(double p, double i, double d, int length) {
        this.kp = p;
        this.ki = i;
        this.kd = d;
        totalError = new int[length];
        prevError = new int[length];
    }

    public int[] work(int[] error) {
        for(int i=0;i<error.length;i++)
            totalError[i]+=error[i];
        int[] uerror = new int[error.length];
        for(int i=0;i<error.length;i++){
            int up = (int)(error[i]*kp);
            int ui = (int)(totalError[i]*ki);
            int ud = (int)((error[i]-prevError[i])*kd);
            uerror[i] = up + ui + ud;
        }
        prevError = error;
        return uerror;
    }
}
