__author__ = 'max'

from network import Network
import pickle
import os


def train_step(net, train_set):
    for i, x, y in train_set:
        net.train((x, y))


def test_step(net, test_set):
    results = []
    for i, x, y in test_set:
        res = net.calc(x)
        s = sum(res)
        probs = [float(q) / s for q in res]
        results.append(probs)
    return results


def create_dirs(path):
    if not os.path.exists(path):
        os.makedirs(path)
    return path


def save_results(net_type, mu, step, net, results):
    path = create_dirs(os.path.join('results', net_type, mu, step))
    with open(os.path.join(path, 'net'), mode='w') as net_file:
        pickle.dump(net, net_file)
    with open(os.path.join(path, 'res'), mode='w') as res_file:
        pickle.dump(results, res_file)


def train_net(net, train_set, test_set, iterations):
    for i in xrange(iterations):
        train_step(net, train_set)
        results = test_step(net, test_set)
        save_results(net.type, str(net.mu), str(i+1), net, results)
        print "iteration %s over net %s successfully ended, results was saved" % (i, net.type)


def process_images_to_inputs(dataset):
    def process((i, l, im)):
        pixels = []
        for row in im:
            for p in row:
                pixels.append(float(p) / 255)
        ans = [0.] * 10
        ans[l] = 1.
        return i, pixels, ans
    return map(process, dataset)


def start(configurations, mus, steps):
    from dataset_parser import extract_test_images, extract_train_images
    test = process_images_to_inputs(extract_test_images())
    # train = process_images_to_inputs(extract_train_images())
    train = test
    inputs = len(test[0][1])
    outputs = len(test[0][2])
    print "All data read, start training"
    for hidden in configurations:
        for mu in mus:
            net = Network([inputs] + hidden + [outputs], mu=mu)
            train_net(net, train, test, steps)


def main():
    import sys
    if len(sys.argv) == 4:
        conf = [int(l) for l in sys.argv[1].split(',')]
        configurations = [conf]
        mus = [float(sys.argv[2])]
        steps = int(sys.argv[3])
        print configurations, mus, steps
    else:
        configurations = [[400], [200, 200], [200, 100, 50]]
        mus = [0.1]
        steps = 100
    start(configurations, mus, steps)


if __name__ == '__main__':
    main()
