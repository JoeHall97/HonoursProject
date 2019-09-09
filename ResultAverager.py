#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Aug 20 16:07:02 2019

@author: Joseph
"""

import pandas as pd
from os import listdir
from os.path import isfile

dags = ['Pipeline', 'MPEG', 'CallGraph']
folders = []
for folder in listdir('.'):
    if not isfile(folder):
        folders.append([folder + '/' + d for d in dags])
# flatten the list of folders
folders = [item for f in folders for item in f]

for dName in folders:
    inputFiles = []
    outputFiles = []
    timeFiles = []
    #get all the file names
    for num in range(16):
        inputFiles.append(dName + "/input" + str(num) + ".csv")
        outputFiles.append(dName + "/output" + str(num) + ".csv")
        timeFiles.append(dName + "/times" + str(num) + ".csv")
    #parse into dataframes
    if(dName=='Pipeline'):
        inputDataframes = [pd.read_csv(f, names=['Node 1', 'Node 2', 'Node 3', 'Node 4', 'Node 5', 'Node 6']) for f in inputFiles]
        outputDataframes = [pd.read_csv(f, names=['Node 1', 'Node 2', 'Node 3', 'Node 4', 'Node 5', 'Node 6']) for f in outputFiles]
        timeDataframes = [pd.read_csv(f, names=['Node 1', 'Node 2', 'Node 3', 'Node 4', 'Node 5', 'Node 6', 'Total']) for f in timeFiles]
    elif(dName=='MPEG'):
        inputDataframes = [pd.read_csv(f, names=['Node 1', 'Node 2', 'Node 3', 'Node 4', 'Node 5', 'Node 6', 'Node 7']) for f in inputFiles]
        outputDataframes = [pd.read_csv(f, names=['Node 1', 'Node 2', 'Node 3', 'Node 4', 'Node 5', 'Node 6', 'Node 7']) for f in outputFiles]
        timeDataframes = [pd.read_csv(f, names=['Node 1', 'Node 2', 'Node 3', 'Node 4', 'Node 5', 'Node 6', 'Node 7', 'Total']) for f in timeFiles]
    else:
        inputDataframes = [pd.read_csv(f, names=['Node 1', 'Node 2', 'Node 3', 'Node 4', 'Node 5', 'Node 6', 'Node 7', 'Node 8']) for f in inputFiles]
        outputDataframes = [pd.read_csv(f, names=['Node 1', 'Node 2', 'Node 3', 'Node 4', 'Node 5', 'Node 6', 'Node 7', 'Node 8']) for f in outputFiles]
        timeDataframes = [pd.read_csv(f, names=['Node 1', 'Node 2', 'Node 3', 'Node 4', 'Node 5', 'Node 6', 'Node 7', 'Node 8', 'Total']) for f in timeFiles]
    #concat dataframes together
    dfInput = pd.concat(inputDataframes, axis=1)
    dfOutput = pd.concat(outputDataframes, axis=1)
    dfTimes = pd.concat(timeDataframes, axis=1)
    #merge and average results
    dfInput = dfInput.groupby(dfInput.columns, axis=1).sum()/len(inputFiles)
    dfOutput = dfOutput.groupby(dfOutput.columns, axis=1).sum()/len(outputFiles)
    dfTimes = dfTimes.groupby(dfTimes.columns, axis=1).sum()/len(timeFiles)
    #output to csv
    dfInput.to_csv(dName.lower()+'InputAvg.csv', index=False)
    dfOutput.to_csv(dName.lower()+'OutputAvg.csv', index=False)
    dfTimes.to_csv(dName.lower()+'TimeAvg.csv', index=False)
