import java.util.List;

public class TrainingVector {

    private final List<Double> vectorData;

    private final boolean isVessel;

    public TrainingVector(List<Double> vectorData, boolean isVessel) {
        this.vectorData = vectorData;
        this.isVessel = isVessel;
    }

    public List<Double> getVectorData() {
        return vectorData;
    }

    public boolean isVessel() {
        return isVessel;
    }

    public void normalize(List<Double> means, List<Double> standardDeviations) {
        StatisticUtils.normalize(vectorData, means, standardDeviations);
    }
}
