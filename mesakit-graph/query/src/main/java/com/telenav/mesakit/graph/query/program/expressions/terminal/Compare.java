package com.telenav.mesakit.graph.query.program.expressions.terminal;

import com.telenav.mesakit.graph.query.program.BooleanExpression;
import com.telenav.mesakit.graph.query.program.Node;
import com.telenav.mesakit.graph.query.program.Visitor;
import com.telenav.mesakit.graph.query.program.expressions.terminal.value.ValueExpression;

/**
 * Comparison expressions, including <b>=</b>, <b>!=</b>, <b>&lt;</b>, <b>&gt;</b>, <b>&lt;=</b> and <b>&gt;=</b>
 *
 * @author jonathanl (shibo)
 */
public class Compare extends Node implements BooleanExpression
{
    public enum Type
    {
        EQUAL("="),
        NOT_EQUAL("!="),
        LESS_THAN("<"),
        GREATER_THAN(">"),
        LESS_THAN_OR_EQUAL("<="),
        GREATER_THAN_OR_EQUAL(">=");

        private final String text;

        Type(String text)
        {
            this.text = text;
        }

        @Override
        public String toString()
        {
            return text;
        }
    }

    /** The type of comparison to perform */
    private final Type type;

    /** The left side of the comparison */
    private final ValueExpression left;

    /** The right side of the comparison */
    private final ValueExpression right;

    public Compare(Type type, ValueExpression left, ValueExpression right)
    {
        this.type = type;
        this.left = left;
        this.right = right;
    }

    /**
     * Evaluates comparisons between attributes and values, such as [attribute] <b>&lt;</b> [value], [attribute]
     * <b>&gt;</b> [value], [attribute] <b>=</b> [value], etc.
     *
     * @return True if the given comparison operator is true in relation to the left and right values.
     */
    @Override
    public boolean evaluate()
    {
        var left = this.left.evaluate();
        var right = this.right.evaluate();
        if (!left.isNull() && !right.isNull())
        {
            var leftNumber = left.asNumber();
            var rightNumber = right.asNumber();
            if (leftNumber != null && rightNumber != null)
            {
                boolean result;
                switch (type)
                {
                    case EQUAL:
                        result = leftNumber.equals(rightNumber);
                        break;

                    case NOT_EQUAL:
                        result = !leftNumber.equals(rightNumber);
                        break;

                    case LESS_THAN:
                        result = leftNumber < rightNumber;
                        break;

                    case GREATER_THAN:
                        result = leftNumber > rightNumber;
                        break;

                    case LESS_THAN_OR_EQUAL:
                        result = leftNumber <= rightNumber;
                        break;

                    case GREATER_THAN_OR_EQUAL:
                        result = leftNumber >= rightNumber;
                        break;

                    default:
                        unsupported("Unsupported comparison operator in expression '$'", code());
                        return false;
                }
                trace("Evaluates to $", result);
                return result;
            }
        }

        fail("The comparison operator '$' cannot be applied to $ and $", type, left, right);
        return false;
    }

    @Override
    public String toString()
    {
        return left + " " + type + " " + right;
    }

    @Override
    public void visit(Visitor visitor)
    {
        super.visit(visitor);
        left.visit(visitor);
        right.visit(visitor);
    }
}
