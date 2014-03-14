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
        int trainSize = System.getProperty("trainSet") == null ? 4000 : Integer.parseInt(System.getProperty("trainSet"));
        int testSize = System.getProperty("testSet") == null ? 1000 : Integer.parseInt(System.getProperty("testSet"));
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
            executor.submit(new Trainer(args[i], train, test, folder));
        }
    }

}

class Trainer implements Runnable{
    public static final int MAX_ITERATIONS = Integer.MAX_VALUE;
    private final List<TestCase> trainSet;
    private final List<TestCase> testSet;
    private final String pathPrefix;
    private final String netConfig;
    private int iteration;

    Trainer(String netConfig, List<TestCase> trainSet, List<TestCase> testSet, String pathPrefix) {
        this.trainSet = trainSet;
        this.testSet = testSet;
        this.pathPrefix = pathPrefix;
        this.netConfig = netConfig;
    }

    @Override
    public void run() {
        Network network = getNetwork();

        try {
            for (; iteration < MAX_ITERATIONS; iteration++){
                System.out.println(String.format("Start %d iteration for net %s", iteration, netConfig));
                trainIteration(network);
                System.out.println(String.format("Start %d iteration testing %s", iteration, netConfig));
                List<Result> results = testIteration(network);
                System.out.println(String.format("Saving %d iteration results %s", iteration, netConfig));
                save(network, results);
                System.out.println(String.format("Iteration %d for %s successfully ended", iteration, netConfig));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Network getNetwork(){
        Path path = Paths.get(pathPrefix, netConfig);
        File dir = path.toFile();
        if (dir.exists() && dir.isDirectory()){
            String[] dirs = dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    try{
                        Integer.parseInt(name);
                        return true;
                    } catch (Exception e){
                        return false;
                    }
                }
            });
            int max = 0;
            for (int i = 0; i < dirs.length; i++){
                max = Math.max(max, Integer.parseInt(dirs[i]));
            }
            for (int i = max; i >= 0; i--){
                try {
                    Network n = Network.fromFile(Paths.get(pathPrefix, netConfig, String.valueOf(i), "net.txt"), netConfig);
                    iteration = i + 1;
                    return n;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        iteration = 0;
        return Network.fromLayerConfigString(netConfig);
    }

    private List<Result> testIteration(Network network){
        ArrayList<Result> results = new ArrayList<Result>();
        for (TestCase testCase : testSet){
            double[] ans = network.calc(testCase.getXs());
            results.add(new Result(testCase, ans));
        }
        return results;
    }

    private void trainIteration(Network network){
        int i = 0;
        int n =  trainSet.size();
        int d = n / 100;
        for (TestCase testCase : trainSet){
            network.train(testCase.getXs(), testCase.getYs());
            if (i % d == 0){
                System.out.println(String.format("Training iteration %d, net %s, %d%% completed", iteration, netConfig, i * 100 / n));
            }
            i++;
        }
    }

    private void save(Network net, List<Result> results) throws FileNotFoundException {
        Path folder = Paths.get(pathPrefix, netConfig, String.valueOf(iteration));
        folder.toFile().mkdirs();
        saveResults(results, folder.resolve("res.txt").toFile());
        saveNetwork(net, folder.resolve("net.txt").toFile());
    }

    private void saveResults(List<Result> results, File f) throws FileNotFoundException {
        try(PrintWriter out = new PrintWriter(f)){
            for (Result r : results){
                out.println(r.toString());
            }
        }
    }

    private void saveNetwork(Network net, File f) throws FileNotFoundException {
        String netString = net.toString();
        try(PrintWriter out = new PrintWriter(f)){
            out.print(netString);
        }
    }

    private static class Result{
        private final TestCase testCase;
        private final double[] ans;

        private Result(TestCase testCase, double[] ans) {
            this.testCase = testCase;
            this.ans = ans;
        }

        @Override
        public String toString() {
            return testCase.getId() + " " + Arrays.toString(testCase.getYs()) + " " + Arrays.toString(ans);
        }
    }
}
