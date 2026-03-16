import tensorflow as tf
from tensorflow import keras
import numpy as np
import os

# --- Configuration ---
# You can tweak these parameters based on your computer's RAM
EPOCHS = 10
BATCH_SIZE = 1024 

def encode_rack(word):
    """
    Converts a word into a 26-element array representing letter frequencies.
    Example for 'BABA': [2, 2, 0, 0, ..., 0] (2 'A's, 2 'B's)
    """
    counts = np.zeros(26, dtype=np.float32)
    for char in word:
        if 'A' <= char <= 'Z':
            counts[ord(char) - ord('A')] += 1.0
    return counts

def train_and_export_model(dictionary_path, model_dir_name, labels_file_name):
    """
    Reads the dictionary, trains a neural network to predict words from letter counts,
    and exports both the model and the label mapping for Java.
    """
    if not os.path.exists(dictionary_path):
        print(f" Error: File {dictionary_path} not found.")
        return

    print(f"\n--- Processing {dictionary_path} ---")
    
    # 1. Read all valid words
    with open(dictionary_path, 'r', encoding='utf-8') as f:
        # Strip whitespace, ignore empty lines, ensure uppercase
        words = [line.strip().upper() for line in f if line.strip()]
        
    num_words = len(words)
    print(f"Loaded {num_words} words from dictionary.")

    # 2. Export the mapping for Java (Index -> Word)
    # The line number in this file will correspond to the Neural Network's output class
    with open(labels_file_name, 'w', encoding='utf-8') as f:
        for word in words:
            f.write(word + "\n")
    print(f"Exported labels to {labels_file_name}")

    # 3. Prepare the Dataset (X = Letter frequencies, Y = Word index)
    print("Preparing dataset (encoding words to tensors)...")
    X = np.zeros((num_words, 26), dtype=np.float32)
    y = np.arange(num_words, dtype=np.int32)

    for i, word in enumerate(words):
        X[i] = encode_rack(word)

    # 4. Define the Neural Network Architecture
    # Input: 26 neurons (A-Z)
    # Hidden layers: 256 and 512 neurons to learn complex anagram patterns
    # Output: 'num_words' neurons (one for each possible word)
    model = keras.Sequential([
        keras.layers.Dense(256, activation='relu', input_shape=(26,)),
        keras.layers.Dropout(0.2), # Prevents overfitting
        keras.layers.Dense(512, activation='relu'),
        keras.layers.Dense(num_words, activation='softmax') # Softmax gives probability % for each word
    ])

    model.compile(optimizer='adam',
                  loss='sparse_categorical_crossentropy',
                  metrics=['accuracy'])

    # 5. Train the model
    print(f"Training the model {model_dir_name}...")
    model.fit(X, y, epochs=EPOCHS, batch_size=BATCH_SIZE)

    # 6. Export the model for Java (SavedModel format)
    model.export(model_dir_name)
    print(f" Model successfully saved in directory: {model_dir_name}")

if __name__ == '__main__':
    import os
    
    # 1. Get the absolute path of the directory containing this script (src/main/resources/ai)
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # 2. Get the absolute path of the dictionaries directory
    # Using abspath here resolves the ".." properly to avoid invalid paths
    dict_dir = os.path.abspath(os.path.join(script_dir, "..", "dictionaries"))
    
    # 3. Setup paths for English
    en_dict_path = os.path.join(dict_dir, "lexicon_en.txt")
    en_model_dir = os.path.join(script_dir, "model_en")
    en_labels_path = os.path.join(script_dir, "labels_en.txt")
    
    # 4. Setup paths for French
    fr_dict_path = os.path.join(dict_dir, "lexicon_fr.txt")
    fr_model_dir = os.path.join(script_dir, "model_fr")
    fr_labels_path = os.path.join(script_dir, "labels_fr.txt")

    # Start training for English
    print("=== Démarrage de l'entraînement : Modèle Anglais ===")
    print(f"Loading dictionary from: {en_dict_path}")
    train_and_export_model(en_dict_path, en_model_dir, en_labels_path)

    # Start training for French
    print("\n=== Démarrage de l'entraînement : Modèle Français ===")
    print(f"Loading dictionary from: {fr_dict_path}")
    train_and_export_model(fr_dict_path, fr_model_dir, fr_labels_path)