__author__ = 'max'

from network import Network

data = [[0, 0], [0, 1], [1, 0], [1, 1]]
ans1 = [[0], [1], [1], [1]]  # or
ans2 = [[0], [0], [0], [1]]  # and

max_iter = 100


def train_net(net, input_set, output_set, max_iter):
    for i in xrange(max_iter):
        for k, x in enumerate(input_set):
            net.train((x, output_set[k]))

#net1 = Network([2, 4, 1], mu=0.1)
#train_net(net1, data, ans1, 5000)
#print "OR results"
#for x in data:
#    print(x, net1.calc(x))
#
#net2 = Network([2, 1], mu=0.1)
#train_net(net2, data, ans2, 5000)
#print "AND results"
#for x in data:
#    print(x, net2.calc(x))

#net3 = Network([2, 4, 1], mu=0.1)
#import random
#data = [(random.gauss(0.5, 0.1), random.gauss(0, 0.1)) for x in xrange(10)]
#ans3 = [(x + y,) for x, y in data]
#train_net(net3, data, ans3, 5000)

#print "PLUS results"
#for x in data:
#    print(x, net3.calc(x)[0] - x[0] + x[1])