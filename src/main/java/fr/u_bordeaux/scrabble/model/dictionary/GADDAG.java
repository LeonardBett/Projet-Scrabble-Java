package fr.u_bordeaux.scrabble.model.dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class GADDAG extends Trie {
    private static char separator = '>';

    public GADDAG(){ root = new Node(Node.root); }

    @Override
    public void add(String word){
        if (word.length() == 0) return;

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

    private void findWordsRecurse(HashSet<String> words, String word, ArrayList<Character> rack, char hook, Node cur, boolean direction){
        Node hookNode = cur.getChild(hook);

        //Base case
        if (hookNode == null)
            return;

        String hookCh = hook == separator ? "" : String.valueOf(hook); //Empty character if we're the separator
        word = (direction ? hookCh + word : word + hookCh); //Direction-based concatenation (if direction hook+word else word +hook)

        //if we've reached the end a word, add the word to output
        if (hookNode.getFinite())
            words.add(word);

        for (char nodeKey : hookNode.getKeys()) {
            if (nodeKey == separator)
                findWordsRecurse(words, word, rack, separator, hookNode, false);
            else if (rack.contains(nodeKey)){
                //boolean duplicate = (rack.size() > 0 && (rack.get(nodeKey) == rack.get(rack.indexOf(nodeKey) - 1)));
                ArrayList<Character> newRack = (ArrayList<Character>) rack.clone();
                newRack.remove((Character)nodeKey);
                findWordsRecurse(words, word, newRack, nodeKey, hookNode, direction);
            }
        }
    }
}