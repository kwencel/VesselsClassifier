import java.io.File;
import java.nio.file.Paths;

/**
 * @author Krzysztof Wencel
 */
public class BatchImageClassifier {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Proper arguments: <Training dir containing 'images', 'manuals' and 'masks' subdirs>" +
                                                 " <Working dir containing 'images', 'manuals' and 'masks' subdirs>");
            System.exit(1);
        }
        String workingDir = args[1];

        Application app = new Application();
        for (File image : Paths.get(workingDir, "images").toFile().listFiles()) {
            args[1] = image.getAbsolutePath();
            app.run(args);
        }
    }
}
