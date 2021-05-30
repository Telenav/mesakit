package com.telenav.tdk.graph.query.program;

import com.telenav.tdk.core.kernel.debug.Debug;
import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.core.kernel.validation.Validate;
import com.telenav.tdk.graph.Route;

/**
 * An node in the abstract syntax tree (AST) that forms the {@link Program}.
 *
 * @author jonathanl (shibo)
 */
public class Node implements Expression
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    static final Debug DEBUG = new Debug(LOGGER);

    /** The number of traces issued while running this program */
    static int traceNumber;

    /** The code that was parsed to produce this node */
    private String code;

    /** The program that this AST node belongs to */
    private Program program;

    @Override
    public String code()
    {
        return code;
    }

    public Node code(final String text)
    {
        code = text;
        return this;
    }

    public Program program()
    {
        return program;
    }

    public void program(final Program program)
    {
        this.program = program;
    }

    public Route result()
    {
        return stack().match();
    }

    @Override
    public EdgeStack stack()
    {
        return program.stack();
    }

    @Override
    public void visit(final Visitor visitor)
    {
        visitor.at(this);
    }

    protected <T> T fail(final String message, final Object... arguments)
    {
        return Validate.fail("Error evaluating '" + code + "': " + message, arguments);
    }

    protected void trace(final String message, final Object... arguments)
    {
        DEBUG.trace(String.format("%3d. ", traceNumber++) + stack() + "\n     " + code() + "\n     " + message, arguments);
    }

    protected <T> T unsupported(final String message, final Object... arguments)
    {
        return Validate.unsupported("Error evaluating '" + code + "': " + message, arguments);
    }
}
