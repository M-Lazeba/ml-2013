import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by max on 3/14/14.
 */
public class Distinct {
    public static void main(String[] args) throws IOException {
        int trainSize = System.getProperty("trainSet") == null ? 4000 : Integer.parseInt(System.getProperty("trainSet"));
        int testSize = System.getProperty("testSet") == null ? 1000 : Integer.parseInt(System.getProperty("testSet"));
        System.out.println("train: " + trainSize + " tests: " + testSize);
        List<TestCase> train = DatasetParser.getTrainSet(args[0], trainSize);
        List<TestCase> test = DatasetParser.getTestSet(args[0], testSize);
//        int threads = Math.min(Integer.valueOf(args[1]), 10);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        String folder = System.getProperty("resFolder");
        if (folder == null) {
            folder = "results";
        }

        String config = args[1];
        if (!config.endsWith("-1")){
            throw new IllegalArgumentException("Output must be 1");
        }

        for (int i = 0; i < 10; i++){
            List<TestCase> optTrainSet = optimizeInputSet(train, i);
            List<TestCase> optTestSet = optimizeInputSet(test, i);
            executor.submit(new Trainer(config, optTrainSet, optTestSet, Paths.get(folder, Integer.toString(i)).toString()));
        }

    }

    private static List<TestCase> optimizeInputSet(List<TestCase> set, int k){
        List<TestCase> res = new ArrayList<>();
        for (TestCase t : set){
            double[] ans = new double[1];
            if (t.getYs()[k] == 1.0){
                ans[0] = 1.0;
            } else {
                ans[0] = 0.0;
            }
            res.add(new TestCase(t.getId(), t.getXs(), ans));
        }
        return res;
    }
}
