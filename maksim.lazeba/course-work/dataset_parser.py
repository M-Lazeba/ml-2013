__author__ = 'max'

import struct


def read_labels_file(file_path):
    with open(file_path, 'rb') as f:
        file_bytes = f.read()
    magic_number = struct.unpack(">I", file_bytes[0:4])[0]
    length = struct.unpack(">I", file_bytes[4:8])[0]
    print(magic_number, length)
    if length != len(file_bytes) - 8 or magic_number != 2049:
        raise Exception('Wrong file format')
    return [struct.unpack(">B", b)[0] for b in file_bytes[8:]]


def read_images_file(file_path):
    with open(file_path, 'rb') as f:
        file_bytes = f.read()
    print "read bytes", len(file_bytes)
    magic_number = struct.unpack(">I", file_bytes[0:4])[0]
    length = struct.unpack(">I", file_bytes[4:8])[0]
    rows = struct.unpack(">I", file_bytes[8:12])[0]
    columns = struct.unpack(">I", file_bytes[12:16])[0]
    print(magic_number, length)
    if length * rows * columns != len(file_bytes) - 16 or magic_number != 2051:
        raise Exception('Wrong file format')
    images_bytes = [struct.unpack(">B", b)[0] for b in file_bytes[16:]]
    images = []
    cur = 0
    for k in xrange(length):
        img_rows = []
        for y in xrange(rows):
            img_rows.append(images_bytes[cur:cur + columns])
            cur += columns
        images.append(img_rows)
    return images


def extract_images():
    labels = read_labels_file("data/t10k-labels-idx1-ubyte")
    images = read_images_file("data/t10k-images-idx3-ubyte")
    if len(labels) != len(images):
        raise Exception('Number of images and labels differ')
    return [(k, images[i]) for (i, k) in enumerate(labels)]


def clear_dirs(path):
    import os
    d = os.path.dirname(path + '/')
    if os.path.exists(d):
        for f in os.listdir(d):
            f = os.path.join(d, f)
            os.remove(f)
    else:
        os.makedirs(d)
    return d


def save_images(folder_path, images):
    folder_path = clear_dirs(folder_path)
    from PIL import Image, ImageDraw
    for i, image in enumerate(images[:10]):
        size = (len(image[1]), len(image[1][0]))
        img = Image.new('RGB', size)
        img_drawer = ImageDraw.Draw(img)
        for y, row in enumerate(image[1]):
            for x, value in enumerate(row):
                img_drawer.point((x, y), (value, value, value))
        img.save(folder_path + "/" + str(i) + "_" + str(image[0]) + ".png")


if __name__ == '__main__':
    import sys
    if len(sys.argv[0]) == 0:
        print "choose folder where to extract images"
        sys.exit(1)
    save_images(sys.argv[0], extract_images())
