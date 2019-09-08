#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Sep  6 14:15:15 2019

@author: Joseph
"""
import pandas as pd
from os import listdir
from os.path import isfile

# get all folders in the current directory
folders = []
for f in listdir('.'):
    if not isfile(f):
        folders.append(f)
# add all time files within the folders a list of dfs
fileNames = []
dataframes = []        
for folder in folders:
    files = []
    for file in listdir('./' + folder):
        if "Time" in file:
            newDf = folder+'/'+file
            dataframes.append(pd.read_csv(newDf))
            fileNames.append(newDf)            
# concat all the dfs togeather and output them to a file
finalDf = pd.concat(dataframes,keys=fileNames)
finalDf.to_csv('times.csv')