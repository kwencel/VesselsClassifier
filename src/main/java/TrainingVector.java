import java.util.List;

public class TrainingVector {

    private final List<Short> vectorData;

    private final boolean isVessel;

    public TrainingVector(List<Short> vectorData, boolean isVessel) {
        this.vectorData = vectorData;
        this.isVessel = isVessel;
    }

    public List<Short> getVectorData() {
        return vectorData;
    }

    public boolean isVessel() {
        return isVessel;
    }
}

