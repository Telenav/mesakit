package com.telenav.mesakit.graph.query.compiler;

import com.telenav.kivakit.interfaces.code.Callback;
import com.telenav.kivakit.core.messaging.Message;
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

    public GraphQueryErrorListener(Callback<String> errorHandler)
    {
        this.errorHandler = errorHandler;
    }

    public boolean error()
    {
        return error;
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b,
                                BitSet bitSet, ATNConfigSet atnConfigSet)
    {
    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1,
                                            BitSet bitSet, ATNConfigSet atnConfigSet)
    {
    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2,
                                         ATNConfigSet atnConfigSet)
    {
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingToken, int line,
                            int character, String message, RecognitionException e)
    {
        error = true;
        errorHandler.callback(Message.format("Syntax error at $: $", character, message));
    }
}
