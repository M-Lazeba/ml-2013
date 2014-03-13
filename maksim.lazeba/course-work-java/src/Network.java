import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by max on 3/12/14.
 */
public class Network {
    private ArrayList<Perceptron> perceptrons = new ArrayList<Perceptron>();
    private double mu = 0;
    private ArrayList<Perceptron> inputs = new ArrayList<Perceptron>();
    private ArrayList<Perceptron> outputs = new ArrayList<Perceptron>();


    private Network(double mu) {
        this.mu = mu;
    }

    public double[] calc(double[] xs){
        if (xs.length != inputs.size()){
            throw new IllegalArgumentException();
        }
        for (Perceptron p : perceptrons){
            p.clear();
        }
        for (int i = 0; i < xs.length; i++){
            inputs.get(i).value = xs[i];
        }
        double[] ys = new double[outputs.size()];
        for (int i = 0; i < ys.length; i++){
            ys[i] = outputs.get(i).calc();
        }
        return ys;
    }

    //TODO return some metric like error measure?
    public void train(double[] xs, double[] ys){
        double[] outs = calc(xs);
        for (int i = 0; i < outs.length; i++){
            outputs.get(i).setDeltaForOutput(ys[i]);
        }
        for (Perceptron p : outputs){
            p.backPropagation();
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(perceptrons.size()).append(" ").append(inputs.size()).append(" ").append(outputs.size()).append("\n");
        for (Perceptron p : perceptrons){
            sb.append(p.id).append(" ").append(p.inputs.size()).append(" ");
            for (int i : p.inputs.keySet()){
                sb.append(i).append(" ").append(p.inputs.get(i)).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static Network fromLayerConfigString(String config){
        String[] s1 = config.split("/");
        double mu = Double.valueOf(s1[0]);
        String[] s2 = s1[1].split("-");
        int inputsCount = Integer.valueOf(s2[0]);
        int[] hiddenLayers = new int[s2.length - 2];
        for (int i = 1; i < s2.length - 1; i++){
            hiddenLayers[i - 1] = Integer.valueOf(s2[i]);
        }
        int outputsCount = Integer.valueOf(s2[s2.length - 1]);

        Network net = new Network(mu);
        for (int in = 0; in < inputsCount; in++){
            net.inputs.add(net.new Perceptron());
        }
        List<Perceptron> prev = net.inputs;
        for (int l : hiddenLayers){
            List<Perceptron> layer = new ArrayList<Perceptron>();
            for (int i = 0; i < l; i++){
                Perceptron p = net.new Perceptron();
                layer.add(p);
                for (Perceptron in : prev){
                    p.joinTo(in.id);
                }
            }
            prev = layer;
        }
        for (int i = 0; i < outputsCount; i++){
            Perceptron p = net.new Perceptron();
            net.outputs.add(p);
            for (Perceptron in : prev){
                p.joinTo(in.id);
            }
        }
        return net;
    }

    public static Network fromFile(Path fullPath, String configuration) throws IOException {
        double mu = Double.valueOf(configuration.split("/")[0]);

        try (Scanner in = new Scanner(fullPath)){
            int ps = in.nextInt();
            int inputs = in.nextInt();
            int outputs = in.nextInt();
            Network net = new Network(mu);
            for (int i = 0; i < ps; i++){
                int id = in.nextInt();
                int k = in.nextInt();
                Perceptron p = net.new Perceptron();
                for (int j = 0; j < k; j++){
                    int pIn = in.nextInt();
                    p.joinTo(pIn);
                    p.inputs.put(pIn, in.nextDouble());
                }
                if (i < inputs){
                    net.inputs.add(p);
                }
                if (i >= ps - outputs){
                    net.outputs.add(p);
                }
            }


            return net;
        }
    }

    private class Perceptron{
        int id;
        HashMap<Integer, Double> inputs = new HashMap<Integer, Double>();
        ArrayList<Integer> outputs = new ArrayList<Integer>();
        Double value = null;
        Double delta = null;
        boolean updated = false;

        private Perceptron() {
            this.id = perceptrons.size();
            perceptrons.add(this);
        }

        public double calc(){
            if (value == null){
                double v = 0;
                for (int in : inputs.keySet()){
                    v += perceptrons.get(in).calc() * inputs.get(in);
                }
                value = Double.valueOf(1/(1+Math.exp(-v)));
            }
            return value;
        }

        public void joinTo(int pId){
            if (id <= pId){
                throw  new IllegalArgumentException();
            }
            inputs.put(pId, Math.random() - 0.5);
            perceptrons.get(pId).outputs.add(id);
        }

        public double getDelta(){
            if (delta == null){
                if (inputs.isEmpty()){
                    return 0;
                }
                double sum = 0;
                for (int i : outputs){
                    Perceptron out = perceptrons.get(i);
                    sum += out.getDelta() * out.inputs.get(id);
                }
                delta = sum * value * (1 - value);
            }
            return delta;
        }

        public void setDeltaForOutput(double out){
            delta = -value * (1 - value) * (out - value);
        }

        public void updateWeights(){
            for (int in : inputs.keySet()){
                double w = inputs.get(in) - mu * delta * perceptrons.get(in).calc();
                inputs.put(in, w);
            }
        }

        public void backPropagation(){
            if (updated){
                return;
            }
            getDelta();
            for (int in : inputs.keySet()){
                perceptrons.get(in).backPropagation();
            }
            updateWeights();
            updated = true;
        }

        public void clear(){
            value = null;
            delta = null;
            updated = false;
        }
    }
}
