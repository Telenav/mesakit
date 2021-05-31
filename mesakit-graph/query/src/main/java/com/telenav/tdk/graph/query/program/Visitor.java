package com.telenav.kivakit.graph.query.program;

/**
 * @author jonathanl (shibo)
 */
public interface Visitor
{
    /**
     * Called for each node when visiting the nodes in the abstract syntax tree forming a {@link Program}
     */
    void at(Node node);
}
