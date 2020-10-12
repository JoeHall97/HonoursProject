# HonoursProject

## Abstract

Legacy programs utilize a sequential architecture due to the hardware architecture of their time. With the advent of multi-core systems, being able to exploit any latent parallelism in these legacy programs is important for improved run-times and the ability to utilise distributed computing systems. By dividing the program into different regions, legacy programs are able to be executed concurrently without their code needing to be changed. To the best of my knowledge, there has been no attempt to test the performance of such a strategy. This project, therefore, works towards building a benchmark to test different threaded execution strategies of these legacy programs, as well as attempting to answering whether utilising Control Theory can provide an effective solution. 

## Modeling legacy program execution. What is a DAG?

To find out how or if a legacy program can be executed in parallel we first need to find out if the program can be divided into inpedentdant processes. This can be done by analysing a programs memory accesses during runtime to establish any consumer/producer relationships in the program. These relationships can be modeled using a Directed A-cyclic Graph (DAG). 

## Benchmarking execution. What does this project do?

Instead of dividing an real legacy program and then benchmarking it's execution, this benchmarker takes in a DAG (as an XML file) and then builds a series of connected threadpools in order to simulate execution. These threadpools each have a task queue which has a set size, allowing us to determine when a program has finished executing, as all the task queues should be empty. The size of the threadpools can be adjusted overtime, allowing for different thread allocation strategies to be tested. These threads also can have a random chance to drop (not execute), to simulate a resource constraned environment. We can then measure the time that it took for the entire DAG to execute, the time that it took for each node in the DAG to execute, the size of the task queues overtime, and the thread allocation overtime. These are all output to CSV files.

Currently the implemented stratigies are a *base case*, spreading all system threads evenly across all nodes, a *proportional strategy*, spreading threads across nodes proportional to the size of their input queue, and a *control strategy*, which uses a PID controller to allocate threads. 
