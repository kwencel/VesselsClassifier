# Blood vessel for retinal images using KNN classifier

(We used images from [HRF database](https://www5.cs.fau.de/research/data/fundus-images/))

### Instructions
First clone the repository:
```
git clone https://github.com/kwencel/VesselsClassifier.git
```
Next go to the root directory and run:
```
./gradlew fatJar
```
This will produce a jar with all dependencies.

Run the program with example images:
```
java -jar ./build/libs/VesselsClassifier-all-1.0-SNAPSHOT.jar ./Examples/training/ ./Examples/test/images/01_dr.JPG
```
This may take a while depending on your CPU.
Results will be written to `./Examples/test/results`

### Manual segmentation

| Input image | Manualy labeled image |
|---|---|
| ![](https://github.com/kwencel/VesselsClassifier/blob/master/Examples/Input.jpg?raw=true)  | ![](https://github.com/kwencel/VesselsClassifier/blob/master/Examples/Manual.jpg?raw=true) |

To achive same result without time-consuming manual segmentation we used our custom implementation of k Nearest Neighbours Classifier.

### Preprocessing
First we extract green channel from input image as it has the highest contrast between the blood vessels and the retinal background. Next we equalize the histogram of the channel.

![](https://github.com/kwencel/VesselsClassifier/blob/master/Examples/Preprocessing.jpg?raw=true)

### Classifier
Model was trained using 300 random balanced samples.
We used 7 features -- Hu moments for 11 x 11 surrounding of each pixel.
11 neighbours proved to yield the best result.

![](https://github.com/kwencel/VesselsClassifier/blob/master/Examples/Result.jpg?raw=true)

### Results
For 15 test inputs we achived average accuracy of 87,7%
