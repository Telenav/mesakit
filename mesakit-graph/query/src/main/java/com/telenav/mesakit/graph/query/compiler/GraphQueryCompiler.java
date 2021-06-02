package com.telenav.mesakit.graph.query.compiler;

import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.mesakit.graph.query.antlr.GraphQueryBaseVisitor;
import com.telenav.mesakit.graph.query.antlr.GraphQueryParser;
import com.telenav.mesakit.graph.query.program.BooleanExpression;
import com.telenav.mesakit.graph.query.program.Node;
import com.telenav.mesakit.graph.query.program.Program;
import com.telenav.mesakit.graph.query.program.expressions.closure.OneOrMore;
import com.telenav.mesakit.graph.query.program.expressions.logical.And;
import com.telenav.mesakit.graph.query.program.expressions.logical.Not;
import com.telenav.mesakit.graph.query.program.expressions.logical.Or;
import com.telenav.mesakit.graph.query.program.expressions.relational.Then;
import com.telenav.mesakit.graph.query.program.expressions.terminal.Compare;
import com.telenav.mesakit.graph.query.program.expressions.terminal.value.Attribute;
import com.telenav.mesakit.graph.query.program.expressions.terminal.value.BooleanAttribute;
import com.telenav.mesakit.graph.query.program.expressions.terminal.value.Constant;
import com.telenav.mesakit.graph.query.program.expressions.terminal.value.Value;
import com.telenav.mesakit.graph.query.program.expressions.terminal.value.ValueExpression;
import com.telenav.mesakit.graph.query.program.statements.Select;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.motion.Speed;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;
import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.unsupported;
import static com.telenav.mesakit.graph.query.antlr.GraphQueryParser.AND;
import static com.telenav.mesakit.graph.query.antlr.GraphQueryParser.EQUAL;
import static com.telenav.mesakit.graph.query.antlr.GraphQueryParser.GREATER_THAN;
import static com.telenav.mesakit.graph.query.antlr.GraphQueryParser.GREATER_THAN_OR_EQUAL;
import static com.telenav.mesakit.graph.query.antlr.GraphQueryParser.LESS_THAN;
import static com.telenav.mesakit.graph.query.antlr.GraphQueryParser.LESS_THAN_OR_EQUAL;
import static com.telenav.mesakit.graph.query.antlr.GraphQueryParser.NOT_EQUAL;
import static com.telenav.mesakit.graph.query.antlr.GraphQueryParser.OR;

/**
 * Compiles a {@link BooleanExpression} tree that can be evaluated against an edge.
 *
 * @author jonathanl (shibo)
 */
public class GraphQueryCompiler extends GraphQueryBaseVisitor<Node>
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
    public Node visitAttribute(final GraphQueryParser.AttributeContext attribute)
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
    public Node visitConstantValue(final GraphQueryParser.ConstantValueContext constant)
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
    public Node visitQuery(final GraphQueryParser.QueryContext query)
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
        if (query.getStart().getType() == GraphQueryParser.OPEN_PARENTHESIS)
        {
            return visit(query.parenthesizedQuery);
        }

        return fail("Invalid query '$'", query.getText());
    }

    @Override
    public Node visitSelect(final GraphQueryParser.SelectContext select)
    {
        final var query = visit(select.query());
        if (query instanceof BooleanExpression)
        {
            return new Select((BooleanExpression) query);
        }
        return fail("Select expression must be boolean");
    }

    private Compare.Type typeOf(final Token comparisonOperator)
    {
        switch (comparisonOperator.getType())
        {
            case EQUAL:
                return Compare.Type.EQUAL;

            case NOT_EQUAL:
                return Compare.Type.NOT_EQUAL;

            case GREATER_THAN:
                return Compare.Type.GREATER_THAN;

            case LESS_THAN:
                return Compare.Type.LESS_THAN;

            case GREATER_THAN_OR_EQUAL:
                return Compare.Type.GREATER_THAN_OR_EQUAL;

            case LESS_THAN_OR_EQUAL:
                return Compare.Type.LESS_THAN_OR_EQUAL;

            default:
                unsupported("Unsupported comparison '$'", comparisonOperator.getText());
                return null;
        }
    }
}
