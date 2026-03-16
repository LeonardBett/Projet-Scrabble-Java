#!/bin/bash

echo "=================================================="
echo "   Génération des modèles TensorFlow (Scrabble)   "
echo "=================================================="

python src/main/resources/ai/train_models.py

echo "=================================================="
echo " Entraînement terminé ! Les modèles sont prêts.   "
echo "=================================================="
read -p "Appuyez sur Entrée pour fermer..."