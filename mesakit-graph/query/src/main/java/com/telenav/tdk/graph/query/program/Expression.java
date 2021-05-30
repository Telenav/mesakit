package com.telenav.tdk.graph.query.program;

/**
 * An expression has source code and a stack that can be used as context while evaluating the expression. It can also be
 * visited by a {@link Visitor}.
 *
 * @author jonathanl (shibo)
 */
public interface Expression
{
    /** The source code for this expression */
    String code();

    /** The context for evaluating the expression */
    EdgeStack stack();

    /**
     * Visits this expression
     */
    void visit(Visitor visitor);
}
