package com.telenav.mesakit.graph.query.compiler;

import com.telenav.kivakit.kernel.interfaces.code.Callback;
import com.telenav.kivakit.kernel.messaging.Message;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;

/**
 * @author jonathanl (shibo)
 */
public class GraphQueryErrorListener implements ANTLRErrorListener
{
    private final Callback<String> errorHandler;

    private boolean error;

    public GraphQueryErrorListener(final Callback<String> errorHandler)
    {
        this.errorHandler = errorHandler;
    }

    public boolean error()
    {
        return error;
    }

    @Override
    public void reportAmbiguity(final Parser parser, final DFA dfa, final int i, final int i1, final boolean b,
                                final BitSet bitSet, final ATNConfigSet atnConfigSet)
    {
    }

    @Override
    public void reportAttemptingFullContext(final Parser parser, final DFA dfa, final int i, final int i1,
                                            final BitSet bitSet, final ATNConfigSet atnConfigSet)
    {
    }

    @Override
    public void reportContextSensitivity(final Parser parser, final DFA dfa, final int i, final int i1, final int i2,
                                         final ATNConfigSet atnConfigSet)
    {
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingToken, final int line,
                            final int character, final String message, final RecognitionException e)
    {
        error = true;
        errorHandler.callback(Message.format("Syntax error at $: $", character, message));
    }
}
