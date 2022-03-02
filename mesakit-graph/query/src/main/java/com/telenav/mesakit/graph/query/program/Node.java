package com.telenav.mesakit.graph.query.program;

import com.telenav.kivakit.ensure.Ensure;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.mesakit.graph.Route;

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

    public Node code(String text)
    {
        code = text;
        return this;
    }

    public Program program()
    {
        return program;
    }

    public void program(Program program)
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
    public void visit(Visitor visitor)
    {
        visitor.at(this);
    }

    protected <T> T fail(String message, Object... arguments)
    {
        return Ensure.fail("Error evaluating '" + code + "': " + message, arguments);
    }

    protected void trace(String message, Object... arguments)
    {
        DEBUG.trace(String.format("%3d. ", traceNumber++) + stack() + "\n     " + code() + "\n     " + message, arguments);
    }

    protected <T> T unsupported(String message, Object... arguments)
    {
        return Ensure.unsupported("Error evaluating '" + code + "': " + message, arguments);
    }
}
