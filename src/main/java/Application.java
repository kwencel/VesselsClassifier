import java.io.IOException;

public class Application {

    public static void main(String[] args) throws Exception {
        nu.pattern.OpenCV.loadShared();
        Application app = new Application();
        app.run();
    }

    private void run() throws IOException {
        Variant testVariant = new TrainingVariant(getClass().getResourceAsStream("training/N_01.jpg"), false);
        testVariant.getVector(VariantModel.DIFFERENTIAL);
        System.out.println("test");
    }


}
