public class ConfusionMatrix {
    private final int truePositiveAmount;
    private final int trueNegativeAmount;
    private final int falsePositiveAmount;
    private final int falseNegativeAmount;

    public ConfusionMatrix(int truePositiveAmount, int trueNegativeAmount, int falsePositiveAmount, int falseNegativeAmount) {
        this.truePositiveAmount = truePositiveAmount;
        this.trueNegativeAmount = trueNegativeAmount;
        this.falsePositiveAmount = falsePositiveAmount;
        this.falseNegativeAmount = falseNegativeAmount;
    }

    public int getTruePositiveAmount() {
        return truePositiveAmount;
    }

    public int getTrueNegativeAmount() {
        return trueNegativeAmount;
    }

    public int getFalsePositiveAmount() {
        return falsePositiveAmount;
    }

    public int getFalseNegativeAmount() {
        return falseNegativeAmount;
    }

    @Override
    public String toString() {
        return  "TP " + truePositiveAmount + '\n' +
                "TN " + trueNegativeAmount + '\n' +
                "FP " + falsePositiveAmount + '\n' +
                "FN " + falseNegativeAmount;
    }
}
