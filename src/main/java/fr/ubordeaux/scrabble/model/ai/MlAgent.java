package fr.ubordeaux.scrabble.model.ai;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.tensorflow.Result;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TFloat32;

/**
 * Machine learning agent for word search using TensorFlow. This class loads a pre-trained model to
 * predict valid dictionary words based on the letters available in a player's rack.
 */
public class MlAgent implements AutoCloseable {

  private static final int ALPHABET_SIZE = 26;
  private SavedModelBundle model;
  private List<String> dictionary;

  /**
   * Constructs the MlAgent and gracefully handles missing models.
   *
   * @param modelPath The path to the directory containing the SavedModel.
   * @param dictionary A list of all valid words, indexed to match the model's output classes.
   */
  public MlAgent(String modelPath, List<String> dictionary) {
    this.dictionary = dictionary;

    File modelDir = new File(modelPath);
    if (!modelDir.exists() || !modelDir.isDirectory()) {
      System.out.println("[ML] Model directory not found at '" + modelPath
          + "'. Neural network will be disabled.");
      this.model = null;
      return;
    }

    try {
      this.model = SavedModelBundle.load(modelPath, "serve");
    } catch (Exception e) {
      System.out.println("[ML] Failed to load TensorFlow model: " + e.getMessage());
      this.model = null;
    }
  }

  /**
   * Checks whether the TensorFlow model was successfully loaded into memory.
   *
   * @return True if the model is ready, false if it is missing or failed to load.
   */
  public boolean isModelLoaded() {
    return this.model != null;
  }

  /**
   * Predicts the best words to form given a string representing the player's rack. Returns an empty
   * list if the model is not loaded.
   *
   * @param rack A string containing the characters available in the player's rack.
   * @param topK The number of top predictions to return.
   * @return A list of predicted words, ordered by confidence.
   */
  public List<String> predictWords(String rack, int topK) {
    List<String> predictedWords = new ArrayList<>();

    if (!isModelLoaded()) {
      return predictedWords;
    }

    float[] inputVector = vectorizeRack(rack);

    // Explicitly create a 2D FloatNdArray to match the expected batch shape
    FloatNdArray ndArray = NdArrays.ofFloats(Shape.of(1, ALPHABET_SIZE));
    for (int i = 0; i < ALPHABET_SIZE; i++) {
      ndArray.setFloat(inputVector[i], 0, i);
    }

    // Try-with-resources ensures Tensors and Result are safely closed to avoid
    // memory leaks
    try (TFloat32 inputTensor = TFloat32.tensorOf(ndArray);
        Result output = this.model.session().runner().feed("serving_default_input:0", inputTensor)
            .fetch("StatefulPartitionedCall:0").run()) {

      // Safely extract the output tensor and cast it
      try (TFloat32 outputTensor = (TFloat32) output.get(0)) {
        predictedWords = getTopPredictions(outputTensor, topK);
      }
    }

    return predictedWords;
  }

  /**
   * Converts a string of letters into a frequency vector of size 26.
   *
   * @param rack The string of letters.
   * @return A float array representing the count of each letter from A to Z.
   */
  private float[] vectorizeRack(String rack) {
    float[] vector = new float[ALPHABET_SIZE];
    String upperRack = rack.toUpperCase();

    for (char c : upperRack.toCharArray()) {
      if (c >= 'A' && c <= 'Z') {
        vector[c - 'A']++;
      }
    }
    return vector;
  }

  /**
   * Extracts the top K words based on the probability distribution output by the model.
   *
   * @param outputTensor The tensor output containing probabilities.
   * @param topK The number of words to extract.
   * @return A list of the top K predicted words.
   */
  private List<String> getTopPredictions(TFloat32 outputTensor, int topK) {
    final List<String> results = new ArrayList<>();

    int numClasses = (int) outputTensor.shape().size(1);

    /**
     * Helper class to store and sort probability predictions.
     */
    class Prediction implements Comparable<Prediction> {
      int index;
      float probability;

      Prediction(int index, float probability) {
        this.index = index;
        this.probability = probability;
      }

      @Override
      public int compareTo(Prediction other) {
        return Float.compare(other.probability, this.probability);
      }
    }

    List<Prediction> predictions = new ArrayList<>();

    for (int i = 0; i < numClasses; i++) {
      float prob = outputTensor.getFloat(0, i);
      // Filter threshold to speed up sorting
      if (prob > 0.001f) {
        predictions.add(new Prediction(i, prob));
      }
    }

    Collections.sort(predictions);

    int limit = Math.min(topK, predictions.size());
    for (int i = 0; i < limit; i++) {
      int wordIndex = predictions.get(i).index;
      if (wordIndex < this.dictionary.size()) {
        results.add(this.dictionary.get(wordIndex));
      }
    }

    return results;
  }

  /**
   * Closes the TensorFlow session and releases resources.
   */
  @Override
  public void close() {
    if (isModelLoaded()) {
      this.model.close();
    }
  }
}
