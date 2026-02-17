package fr.u_bordeaux.scrabble.model.dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class GADDAG extends Trie {
    private static final char separator = '>';

    public GADDAG(){ root = new Node(Node.root); }

    public static class GaddagResult {
        public final String word;
        public final String gaddagPath;

        public GaddagResult(String word, String gaddagPath) {
            this.word = word;
            this.gaddagPath = gaddagPath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GaddagResult that = (GaddagResult) o;
            return word.equals(that.word) && gaddagPath.equals(that.gaddagPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(word, gaddagPath);
        }
    }

    @Override
    public void add(String word){
        if (word.isEmpty()) return;

        word = word.toLowerCase();

        String prefix;
        char[] ch;
        int i;
        for (i = 1; i < word.length(); i++){
            prefix = word.substring(0,i); //get a substring from the word, from 0 and with a length i, so it increase from 1 to word length (not 0 because we need a hook)
            ch = prefix.toCharArray();
            reverse(ch); // reverse the prefix in order to respect GADDAG spec
            super.add(new String(ch) + separator + word.substring(i));
        }
        ch = word.toCharArray();
        reverse(ch);
        super.add(new String(ch) + separator + word.substring(i)); // for the last letter, reverse the all word (optional ? "+ word.substring(i)")
    }

    private void reverse(char[] validData){
        for(int i = 0; i < validData.length/2; i++)
        {
            int temp = validData[i];
            validData[i] = validData[validData.length - i - 1];
            validData[validData.length - i - 1] = (char)temp;
        }
    }


    public HashSet<GaddagResult> findWordsWithRackAndHook(Character[] rack, char hook){
        HashSet<GaddagResult> words = new HashSet<>();
        Arrays.sort(rack);
        ArrayList<Character> rackList = new ArrayList<>(Arrays.asList(rack));

        if (hook == ' '){
            char tile;
            while (rackList.size() > 1){
                tile = rackList.removeFirst();
                // On initialise le gaddagPath avec une chaîne vide ""
                findWordsRecurse(words, "", "", rackList, tile, root, true);
            }
        } else {
            findWordsRecurse(words, "", "", rackList, hook, root, true);
        }
        return words;
    }

    private void findWordsRecurse(HashSet<GaddagResult> words, String word, String gaddagPath, ArrayList<Character> rack, char hook, Node cur, boolean direction){
        Node hookNode = cur.getChild(hook);

        if (hookNode == null) return;

        String hookCh = hook == separator ? "" : String.valueOf(hook);
        word = (direction ? hookCh + word : word + hookCh);

        // On accumule le chemin parcouru dans le GADDAG
        gaddagPath = gaddagPath + hook;

        if (hookNode.getFinite())
            words.add(new GaddagResult(word, gaddagPath)); // On sauvegarde les deux !

        for (char nodeKey : hookNode.getKeys()) {
            if (nodeKey == separator)
                findWordsRecurse(words, word, gaddagPath, rack, separator, hookNode, false);
            else if (rack.contains(nodeKey)){
                ArrayList<Character> newRack = (ArrayList<Character>) rack.clone();
                newRack.remove((Character)nodeKey);
                findWordsRecurse(words, word, gaddagPath, newRack, nodeKey, hookNode, direction);
            }
        }
    }
}