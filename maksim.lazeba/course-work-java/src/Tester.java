import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Created by max on 3/14/14.
 */
public class Tester {
    public static void main(String[] args) throws IOException {
        List<TestCase> tests = DatasetParser.getTrainSet(args[0], 1000);
        Network net = Network.fromFile(Paths.get("results", args[1], "50", "net.txt"), args[1]);
        double error = 0;
        for (TestCase test : tests){
            double[] res = net.calc(test.getXs());
            int resValue = 0;
            for (int i = 0; i < res.length; i++){
                if (res[i] > res[resValue]){
                    resValue = i;
                }
            }
            int ansValue = 0;
            for (int i = 0; i < test.getYs().length; i++){
                if (test.getYs()[i] > test.getYs()[ansValue]){
                    ansValue = i;
                }
            }
            if (resValue != ansValue){
                error += 1;
                System.out.println(test.getId() + " res=" + resValue + " ans=" + ansValue);
            }
        }
        System.out.println("Error rate is: " + (error / tests.size()));

    }
}
