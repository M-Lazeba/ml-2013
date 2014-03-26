import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by max on 3/12/14.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        int trainSize = System.getProperty("trainSet") == null ? 400 : Integer.parseInt(System.getProperty("trainSet"));
        int testSize = System.getProperty("testSet") == null ? 100 : Integer.parseInt(System.getProperty("testSet"));
        System.out.println("train: " + trainSize + " tests: " + testSize);
        List<TestCase> train = DatasetParser.getTrainSet(args[0], trainSize);
        List<TestCase> test = DatasetParser.getTestSet(args[0], testSize);
        int threads = Math.min(Integer.valueOf(args[1]), 10);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        String folder = System.getProperty("resFolder");
        if (folder == null)
            folder = "results";
        System.out.println("Save results to " + folder);
        for (int i = 2; i < args.length; i++){
            executor.submit(new Trainer(args[i], train, test, folder, new Tester.CorrectnessTest() {
                @Override
                public boolean test(double[] rightAnswer, double[] netAnswer) {
                    int resValue = 0;
                    for (int i = 0; i < netAnswer.length; i++){
                        if (netAnswer[i] > netAnswer[resValue]){
                            resValue = i;
                        }
                    }
                    int ansValue = 0;
                    for (int i = 0; i < rightAnswer.length; i++){
                        if (rightAnswer[i] > rightAnswer[ansValue]){
                            ansValue = i;
                        }
                    }
                    return ansValue == resValue;
                }
            }));
        }
    }

}

