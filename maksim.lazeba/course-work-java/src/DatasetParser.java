import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by max on 3/12/14.
 */
public class DatasetParser {

    private static List<TestCase> parseFiles(File images, File labels, int max) throws IOException {
        DataInputStream inImages = new DataInputStream(new FileInputStream(images));
        DataInputStream inLabels = new DataInputStream(new FileInputStream(labels));
        int mgi = inImages.readInt();
        int mgl = inLabels.readInt();
        int lenI = inImages.readInt();
        int lenL = inLabels.readInt();
        if (lenI != lenL){
            throw new IllegalArgumentException();
        }

        if (max <= 0 || max > lenI){
            max = lenI;
        }
        int h = inImages.readInt();
        int w = inImages.readInt();
        byte[] pixels = new byte[h * w];
        ArrayList<TestCase> ret = new ArrayList<TestCase>();
        for (int i = 0; i < max; i++){
            byte label = inLabels.readByte();
            inImages.readFully(pixels);
            double[] input = new double[pixels.length];
            for (int j = 0; j < input.length; j++){
                int p = pixels[j];
                if (p < 0){
                    p += 256;
                }
                input[j] = ((double)p) / 256;
            }
            double[] output = new double[10];
            Arrays.fill(output, 0);
            output[label] = 1;
            ret.add(new TestCase(i, input, output));
        }
        return ret;
    }

    public static List<TestCase> getTestSet(String location, int max) throws IOException {
        Path images = Paths.get(location, "t10k-images-idx3-ubyte");
        Path labels = Paths.get(location, "t10k-labels-idx1-ubyte");
        return parseFiles(images.toFile(), labels.toFile(), max);
    }

    public static List<TestCase> getTrainSet(String location, int max) throws IOException {
        Path images = Paths.get(location, "train-images-idx3-ubyte");
        Path labels = Paths.get(location, "train-labels-idx1-ubyte");
        return parseFiles(images.toFile(), labels.toFile(), max);
    }

    public static void main(String[] args) throws IOException {
//        getTestSet(args[0], 0);
//        getTrainSet(args[0], 0);
    }
}
