package com.telenav.kivakit.graph.query.compiler;

import com.telenav.kivakit.kernel.scalars.counts.Maximum;
import com.telenav.kivakit.graph.query.program.*;
import com.telenav.kivakit.graph.query.program.expressions.closure.OneOrMore;
import com.telenav.kivakit.graph.query.program.expressions.logical.*;
import com.telenav.kivakit.graph.query.program.expressions.relational.Then;
import com.telenav.kivakit.graph.query.program.expressions.terminal.Compare;
import com.telenav.kivakit.graph.query.program.expressions.terminal.Compare.Type;
import com.telenav.kivakit.graph.query.program.expressions.terminal.value.*;
import com.telenav.kivakit.graph.query.program.statements.Select;
import com.telenav.kivakit.map.measurements.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import static com.telenav.kivakit.kernel.validation.Validate.*;
import static com.telenav.kivakit.graph.query.GraphQueryParser.*;

/**
 * Compiles a {@link BooleanExpression} tree that can be evaluated against an edge.
 *
 * @author jonathanl (shibo)
 */
public class GraphQueryCompiler extends com.telenav.kivakit.graph.query.GraphQueryBaseVisitor<Node>
{
    /**
     * Builds an abstract syntax tree from the parse tree
     *
     * @param tree The parse tree
     * @return The abstract syntax tree
     */
    public Program compile(final ParseTree tree, final Maximum maximumClosureLength)
    {
        final var node = visit(tree);
        if (node instanceof Select)
        {
            final var select = (Select) node;
            return new Program(select, maximumClosureLength);
        }
        return fail("Compiled query must be a select expression with a boolean query");
    }

    /**
     * Handles visiting an attribute in the parse tree
     *
     * @return The extracted attribute
     */
    @Override
    public Node visitAttribute(final AttributeContext attribute)
    {
        final var code = attribute.getText();
        return new Attribute(code).code(code);
    }

    /**
     * Evaluates the right-hand side of a comparison, the value.
     *
     * @return The value
     */
    @Override
    public Node visitConstantValue(final ConstantValueContext constant)
    {
        final var code = constant.getText();
        if (constant.unit != null)
        {
            switch (constant.unit.getText())
            {
                case "kph":
                case "mph":
                case "msec":
                    return new Constant(Value.of(Speed.parse(code))).code(code);

                default:
                    return new Constant(Value.of(Distance.parse(code))).code(code);
            }
        }
        return new Constant(Value.of(code)).code(code);
    }

    /**
     * Handles visiting a query in the parse tree (possibly recursively)
     *
     * @return True if the query matched and false if it didn't
     */
    @Override
    public Node visitQuery(final QueryContext query)
    {
        final var code = query.getText();

        // not [query]
        if (query.notOperator != null)
        {
            final var expression = visit(query.notQuery);
            if (expression instanceof BooleanExpression)
            {
                return new Not((BooleanExpression) expression).code(code);
            }
            return fail("Argument to not operator must be a boolean expression, not '$'", code);
        }

        // [query]+
        if (query.closure != null)
        {
            final var expression = visit(query.closureQuery);
            if (expression instanceof BooleanExpression)
            {
                return new OneOrMore((BooleanExpression) expression).code(code);
            }
            return fail("Argument to closure must be a boolean expression, not '$'", code);
        }

        // [query] THEN [query]
        if (query.thenOperator != null)
        {
            final var left = visit(query.thenLeftQuery);
            final var right = visit(query.thenRightQuery);
            if (left instanceof BooleanExpression && right instanceof BooleanExpression)
            {
                return new Then((BooleanExpression) left, (BooleanExpression) right).code(code);
            }
            return fail("Both sides of a THEN expression must be boolean expressions");
        }

        // [attribute] [comparison-operator] [value]
        if (query.comparisonOperator != null)
        {
            final var left = visit(query.comparisonAttribute);
            final var right = visit(query.comparisonValue);
            if (left instanceof ValueExpression && right instanceof ValueExpression)
            {
                return new Compare(typeOf(query.comparisonOperator), (ValueExpression) left, (ValueExpression) right).code(code);
            }
            return fail("Both sides of a comparison expression must be value expressions");
        }

        // [attribute]
        if (query.booleanAttribute != null)
        {
            final var text = query.booleanAttribute.getText();
            final var attribute = BooleanAttribute.parse(text);
            if (attribute != null)
            {
                return attribute.code(code);
            }
            return fail("A stand-alone attribute must be boolean, not 'Edge.$'", text);
        }

        // [query] OR [query]
        // [query] AND [query]
        if (query.logicalOperator != null)
        {
            final var left = visit(query.logicalLeftQuery);
            final var right = visit(query.logicalRightQuery);
            if (left instanceof BooleanExpression && right instanceof BooleanExpression)
            {
                switch (query.logicalOperator.getType())
                {
                    case AND:
                        return new And((BooleanExpression) left, (BooleanExpression) right).code(code);

                    case OR:
                        return new Or((BooleanExpression) left, (BooleanExpression) right).code(code);

                    default:
                        return unsupported("Unsupported logical operator '$'", query.logicalOperator.getText());
                }
            }
            return fail("Both sides of a logical (AND or OR) expression must be boolean expressions");
        }

        // ( [query] )
        if (query.getStart().getType() == OPEN_PARENTHESIS)
        {
            return visit(query.parenthesizedQuery);
        }

        return fail("Invalid query '$'", query.getText());
    }

    @Override
    public Node visitSelect(final SelectContext select)
    {
        final var query = visit(select.query());
        if (query instanceof BooleanExpression)
        {
            return new Select((BooleanExpression) query);
        }
        return fail("Select expression must be boolean");
    }

    private Type typeOf(final Token comparisonOperator)
    {
        switch (comparisonOperator.getType())
        {
            case EQUAL:
                return Type.EQUAL;

            case NOT_EQUAL:
                return Type.NOT_EQUAL;

            case GREATER_THAN:
                return Type.GREATER_THAN;

            case LESS_THAN:
                return Type.LESS_THAN;

            case GREATER_THAN_OR_EQUAL:
                return Type.GREATER_THAN_OR_EQUAL;

            case LESS_THAN_OR_EQUAL:
                return Type.LESS_THAN_OR_EQUAL;

            default:
                unsupported("Unsupported comparison '$'", comparisonOperator.getText());
                return null;
        }
    }
}
