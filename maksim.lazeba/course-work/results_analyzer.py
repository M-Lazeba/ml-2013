__author__ = 'max'

# import os
# import pickle

# with open(os.path.join('results', '2-4-2', '100', 'res'))as f:
#     res = pickle.load(f)
#     print(res)

import time
t = time.time()
s = 0
for i in xrange(1000000000):
    s += 1
print s, (time.time() - t)