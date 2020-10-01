import matplotlib.pyplot as plt

def do(string):
	a = string.split("\n")
	b = []
	c = []
	for x in a:
		tmp = x.split(":")
		b.append(tmp[0])
		c.append(tmp[1])
	plt.plot(b,c)
