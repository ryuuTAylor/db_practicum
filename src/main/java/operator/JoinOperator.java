package operator;

import common.Tuple;
import expression.ExpressionVisitorImpl;
import net.sf.jsqlparser.expression.Expression;

public class JoinOperator extends Operator {
    private final Operator rightChild;
    private final Operator leftChild;
    private final Expression condition;

    public JoinOperator(Operator leftChild, Operator rightChild, Expression condition) {
        super(leftChild.getOutputSchema());
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.condition = condition;
    }

    @Override
    public void reset() {
        rightChild.reset();
    }

    @Override
    public Tuple getNextTuple() {
        Tuple leftTuple;
        Tuple rightTuple;
        // Scan the left (outer) child once
        while ((leftTuple = leftChild.getNextTuple()) != null) {
            // Scan the right (inner) child
            while((rightTuple = rightChild.getNextTuple()) != null) {
                // Glue the two together
                String tuples = leftTuple + rightTuple.toString();
                Tuple gluedTuple = new Tuple(tuples);
                // If there is a non-null join condition,
                // the tuple is only returned if it matches the condition
                if (condition != null) {
                    ExpressionVisitorImpl visitor = new ExpressionVisitorImpl(gluedTuple, getOutputSchema());
                    if (visitor.evaluate(condition)){
                        return gluedTuple;
                    }
                }
                // If there is no condition, return cartesian product
                else {
                    return gluedTuple;
                }
                return null;
            }
            // Reset inner child for next iteration
            reset();
        }
        return null;
    };
}
