import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by max on 3/19/14.
 */
public class Cancer {
    public static void main(String[] args) throws IOException {
        String folder = "cancer-results";
        String dataSet = System.getProperty("dataSet", "data");
        List<TestCase> data = parseDataSet(Paths.get(dataSet));
        Collections.shuffle(data, new Random(42));
        int border = (int) (data.size() * 0.8);
        List<TestCase> train = data.subList(0,border);
        List<TestCase> test = data.subList(border, data.size());

        Trainer.NET_SAVE_PERIOD = 20;
        Trainer.MAX_ITERATIONS = 2000;
        Trainer trainer = new Trainer(args[0], train, test, folder, new Tester.CorrectnessTest() {
            @Override
            public boolean test(double[] rightAnswer, double[] netAnswer) {
                int k = netAnswer[0] > netAnswer[1] ? 0 : 1;
                return rightAnswer[k] == 1;
            }
        });
        trainer.run();
    }

    public static List<TestCase> parseDataSet(Path path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))){
            List<TestCase> list = new ArrayList<>();
            String line;
            double[] maxes = new double[30];
            while ((line = reader.readLine()) != null){
                String[] splitted = line.split(",");
                int id = Integer.parseInt(splitted[0]);
                int ans = "M".equals(splitted[1]) ? 1 : 0;
                double[] ys = new double[2];
                ys[ans] = 1;
                double[] xs = new double[splitted.length - 2];
                for (int i = 0; i < xs.length; i++){
                    xs[i] = Double.parseDouble(splitted[i + 2]);
                    if (xs[i] > maxes[i]){
                        maxes[i] = xs[i];
                    }
                }
                list.add(new TestCase(id, xs ,ys));
            }
            for (TestCase t : list){
                for (int i = 0; i < maxes.length; i++){
                    t.getXs()[i] /= maxes[i];
                }
            }
            return list;
        }
    }
}
