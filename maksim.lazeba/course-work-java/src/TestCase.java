/**
 * Created by max on 3/12/14.
 */
public class TestCase {
    private int id;
    private double[] xs;
    private double[] ys;

    public TestCase(int id, double[] xs, double[] ys) {
        this.id = id;
        this.xs = xs;
        this.ys = ys;
    }

    public int getId() {
        return id;
    }

    public double[] getXs() {
        return xs;
    }

    public double[] getYs() {
        return ys;
    }
}
