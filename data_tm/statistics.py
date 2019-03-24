# -*- coding:utf-8 -*-

from collections import Counter


def function(filename):
	file = open(filename, 'r').readlines()
	c = Counter(file)
	for i in c:
		print(i , c[i], '\n')

def getSATDNumber():
	projects = open('projects', 'r').readlines()
	labels = open('labels', 'r').readlines()
	SATD = {}
	ALL = {}
	for i in range(0, len(projects)):
		SATD[projects[i]] = .0
		ALL[projects[i]] = .0
	for i in range(0, len(projects)):
		if labels[i] != 'WITHOUT_CLASSIFICATION\n':
			SATD[projects[i]] += 1
		ALL[projects[i]] += 1
	
	for key in SATD:
		print(key + ": " + str(SATD[key]) + " " + str(ALL[key]) + " " +  str(SATD[key] / ALL[key]))

	
#function('projects')
#function('labels')
getSATDNumber()
