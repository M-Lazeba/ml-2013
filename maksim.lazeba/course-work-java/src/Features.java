import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by max on 3/20/14.
 */
public class Features {
    public static double mu = 0.01;

    public static void main(String[] args) throws IOException {
        String dataSet = System.getProperty("dataSet", "data");
//        List<TestCase> data = Cancer.parseDataSet(Paths.get("../labs/data/wdbc.data"));
        List<TestCase> data = DatasetParser.getTrainSet(dataSet, 800);
//        List<TestCase> positive = new ArrayList<>();
//        for (TestCase t : data){
//            if (t.getYs()[1] == 1){
//                positive.add(t);
//            }
//        }
        int xn = data.get(0).getXs().length;
        Neuron[] neurons = {new Neuron(xn), new Neuron(xn), new Neuron(xn)};
        int ITERATIONS = 500;
        for (int i = 0; i < ITERATIONS; i++){
            double y1 = neurons[0].getOutput(data.get(0).getXs());
            for (TestCase test : data){
                double[] xs = Arrays.copyOf(test.getXs(), test.getXs().length);
                neurons[0].analyze(xs);
                neurons[1].analyze(xs);
                neurons[2].analyze(xs);
            }
            double y2 = neurons[0].getOutput(data.get(0).getXs());
            if (y1 != y2){
                System.out.println(i + " iteration: Y changed " + y1 + " " + y2);
            }
//            System.out.println(i + " " + changed);
//            if (i % 50000 == 0){
//                save(i,printResults(data, neurons));
//                System.out.println(i);
//            }
        }
        save(ITERATIONS, printResults(data, neurons));
    }

    public static void save(int iteration, String s) throws FileNotFoundException {
        String filename = String.format("digits3d-%d.txt", iteration);
        PrintWriter out = new PrintWriter(filename);
        out.print(s);
        out.close();
        System.out.println(filename + " updated");
    }

    public static String printResults(List<TestCase> data, Neuron[] neurons){
        StringBuilder sb = new StringBuilder();
        for (TestCase test : data){
            int ans = 0;
            for (double d : test.getYs()){
                if (d == 1){
                    break;
                }
                ans++;
            }

            sb.append(String.format("%d [%d] : (%f,%f,%f)",
                    test.getId(), ans,
                    neurons[0].getOutput(test.getXs()),
                    neurons[1].getOutput(test.getXs()),
                    neurons[2].getOutput(test.getXs())));
            sb.append("\n");
        }
        return sb.toString();
    }

    public static class Neuron{
        private double[] weights;

        public Neuron(int n){
            weights = new double[n];
//            Arrays.fill(weights, 1);
            for (int i = 0; i < n; i++){
//                weights[i] = Math.random();
                weights[i] = 1;
            }
            normalize(weights);
        }

        public double getOutput(double[] xs){
            double y = 0;
            for (int i = 0; i < xs.length; i++){
                y += xs[i] * weights[i];
            }
            return y;
        }

        public void analyze(double[] xs){
            double y = getOutput(xs);
            for (int i = 0; i < weights.length; i++){
                double dw = mu * y * xs[i];
//                System.out.println(dw);
                xs[i] -= weights[i] * y;
                weights[i] += dw;
            }
            normalize(weights);
        }

        public static void normalize(double[] weights){
            double sum = 0;
            for (double w : weights){
                sum += w*w;
            }
            sum = Math.sqrt(sum);
            for (int i = 0; i < weights.length; i++){
                weights[i] /= sum;
            }
        }
    }
}
