__author__ = 'max'

import math


class Perceptron:
    inputs = []
    value = None

    def __init__(self, inputs=list()):
        self.inputs = inputs

    def calc(self):
        if not self.value:
            v = sum([p.calc * w for p, w in self.inputs])
            self.value = 1 / (1 + math.e ** (-v))
        return self.value

    def clear(self):
        self.value = None


class Network:
    input_ps = []
    out_ps = []
    ps = []
    mu = None

    def __init__(self, layers, mu=0.01):
        self.mu = mu
        prev_layer = []
        for k in xrange(layers[0]):
            p = Perceptron()
            self.input_ps.append(p)
            self.ps.append(p)
            prev_layer.append(p)
        for layer_length in layers[1:]:
            layer = []
            for k in xrange(layer_length):
                p = Perceptron(prev_layer)
                self.ps.append(p)
                layer.append(p)
            prev_layer = layer
        self.out_ps = prev_layer

    def train(self, (test_in, test_out)):
        self.calc(test_in)
        pass

    def calc(self, input_vector):
        if len(input_vector) != len(self.input_ps):
            raise Exception("Illegal argument")
        for p in self.ps:
            p.clear()

        for i, input_p in enumerate(self.input_ps):
            input_p.value = input_vector[i]

        return [out_p.calc() for out_p in self.out_ps]

