__author__ = 'max'

import math
import random

random.seed(42)


class Perceptron:
    def __init__(self, p_id, ps, inputs=list()):
        self.id = p_id
        self.ps = ps
        self.inputs = {}
        self.joined = set()
        self.value = None
        self.delta = None
        self.w0 = random.gauss(0, 1)
        for p in inputs:
            self.join_to(p)

    def join_to(self, p):
        if self.id <= p.id:
            raise Exception('Illegal join perceptron connection')
        if p.id in self.inputs:
            return
        self.inputs[p.id] = random.gauss(0, 1)
        p.joined.add(self.id)

    def calc(self):
        if self.value is None:
            v = sum([self.ps[p].calc() * self.inputs[p] for p in self.inputs]) + self.w0
            self.value = 1 / (1 + math.e ** (-v))
        return self.value

    def calc_delta(self):
        if self.delta is None:
            self.delta = self.calc() * (1 - self.calc()) * sum([self.ps[c_id].calc_delta() * self.ps[c_id].inputs[self.id] for c_id in self.joined])
        return self.delta

    def update_weights(self, mu):
        self.w0 += mu * self.delta
        for p in self.inputs:
            self.inputs[p] += mu * self.delta * self.ps[p].value

    def clear(self):
        self.value = None
        self.delta = None


class Network:

    def __init__(self, layers, mu=0.01):
        self.input_ps = []
        self.out_ps = []
        self.ps = []
        self.mu = mu
        self.type = '-'.join([str(l) for l in layers])
        prev_layer = []
        for k in xrange(layers[0]):
            p = Perceptron(p_id=len(self.ps), ps=self.ps)
            self.input_ps.append(p)
            self.ps.append(p)
            prev_layer.append(p)
        for layer_length in layers[1:]:
            layer = []
            for k in xrange(layer_length):
                p = Perceptron(p_id=len(self.ps), ps=self.ps, inputs=prev_layer)
                self.ps.append(p)
                layer.append(p)
            prev_layer = layer
        self.out_ps = prev_layer

    def train(self, (test_in, test_out)):
        self.calc(test_in)
        self.back_propagation(test_out)

    def back_propagation(self, test_out):
        front = set()
        for k, p in enumerate(self.out_ps):
            p.delta = p.calc() * (1 - p.calc()) * (test_out[k] - p.calc())
            for prev in p.inputs:
                front.add(self.ps[prev])
        while len(front) > 0:
            p = front.pop()
            p.calc_delta()
            for prev in p.inputs:
                front.add(self.ps[prev])
        for p in self.ps:
            p.update_weights(self.mu)

    def calc(self, input_vector):
        if len(input_vector) != len(self.input_ps):
            raise Exception("Illegal argument")
        for p in self.ps:
            p.clear()

        for i, input_p in enumerate(self.input_ps):
            input_p.value = input_vector[i]

        return [out_p.calc() for out_p in self.out_ps]
