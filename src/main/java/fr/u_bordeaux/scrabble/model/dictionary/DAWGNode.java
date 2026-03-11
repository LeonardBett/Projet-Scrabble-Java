package fr.u_bordeaux.scrabble.model.dictionary;

import java.util.Map;
import java.util.TreeMap;
import java.util.Objects;

public class DAWGNode {
    private final char content;
    private boolean finite = false;
    public TreeMap<Character, DAWGNode> children = new TreeMap<>();

    public DAWGNode(char c) {
        this.content = c;
    }

    public char getContent() { return content; }
    public void setFinite(boolean value) { finite = value; }
    public boolean getFinite() { return finite; }

    /**
     * Crucial for minimization: identifies identical structures.
     * Two nodes are equal if they have the same content, same finite status,
     * and identical child transitions.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DAWGNode node = (DAWGNode) o;
        return content == node.content &&
                finite == node.finite &&
                children.equals(node.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, finite, children);
    }
}