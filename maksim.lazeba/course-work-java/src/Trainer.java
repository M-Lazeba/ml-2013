import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by max on 3/14/14.
 */
class Trainer implements Runnable{
    public static int MAX_ITERATIONS = Integer.MAX_VALUE;
    public static int NET_SAVE_PERIOD = 5;
    private final List<TestCase> trainSet;
    private final List<TestCase> testSet;
    private final String pathPrefix;
    private final String netConfig;
    private final Tester.CorrectnessTest correctnessTest;
    private int iteration;

    Trainer(String netConfig, List<TestCase> trainSet, List<TestCase> testSet, String pathPrefix, Tester.CorrectnessTest correctnessTest) {
        this.trainSet = trainSet;
        this.testSet = testSet;
        this.pathPrefix = pathPrefix;
        this.netConfig = netConfig;
        this.correctnessTest = correctnessTest;
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
                System.out.println(String.format("Saving %d iteration results %s. Error rate = %f", iteration, netConfig, Tester.calcErrorRate(results, correctnessTest)));
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
//            if (i % d == 0){
//                System.out.println(String.format("Training iteration %d, net %s, %d%% completed", iteration, netConfig, i * 100 / n));
//            }
            i++;
        }
    }

    private void save(Network net, List<Result> results) throws FileNotFoundException {
        Path folder = Paths.get(pathPrefix, netConfig, String.valueOf(iteration));
        folder.toFile().mkdirs();
        saveResults(results, folder.resolve("res.txt").toFile());
        if (iteration % NET_SAVE_PERIOD == 0) {
            saveNetwork(net, folder.resolve("net.txt").toFile());
        }
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

    public static class Result{
        public final TestCase testCase;
        public final double[] ans;

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
