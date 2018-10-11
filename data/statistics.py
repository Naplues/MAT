# -*- coding:utf-8 -*-

from collections import Counter


def function(filename):
	file = open(filename, 'r').readlines()
	c = Counter(file)
	for i in c:
		print(i , c[i], '\n')


function('projects')
function('labels')
