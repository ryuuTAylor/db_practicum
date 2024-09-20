package operator;

import common.Tuple;
import expression.ExpressionVisitorImpl;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/** JoinOperator implements the tuple nested loop join algorithm. */
public class JoinOperator extends Operator {
    private final Operator leftChild;
    private final Operator rightChild;
    private final Expression condition;

    private Tuple currentLeftTuple;

    public JoinOperator(Operator leftChild, Operator rightChild, Expression condition) {
        super(mergeSchemas(leftChild.getOutputSchema(), rightChild.getOutputSchema()));
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.condition = condition;
        this.currentLeftTuple = null;
    }

    private static ArrayList<Column> mergeSchemas(
            ArrayList<Column> leftSchema, ArrayList<Column> rightSchema) {
        ArrayList<Column> merged = new ArrayList<>(leftSchema);
        merged.addAll(rightSchema);
        return merged;
    }

    @Override
    public void reset() {
        leftChild.reset();
        rightChild.reset();
        currentLeftTuple = null;
    }

    @Override
    public Tuple getNextTuple() {
        try {
            while (true) {
                if (currentLeftTuple == null) {
                    currentLeftTuple = leftChild.getNextTuple();
                    if (currentLeftTuple == null) {
                        return null; // No more tuples from the left table
                    }
                    rightChild.reset();
                }

                Tuple rightTuple = rightChild.getNextTuple();
                if (rightTuple == null) {
                    currentLeftTuple = null; // Reset the left tuple and try with the next one
                    continue;
                }

                // Merge tuples
                ArrayList<Integer> mergedElements = new ArrayList<>(currentLeftTuple.getAllElements());
                mergedElements.addAll(rightTuple.getAllElements());
                Tuple mergedTuple = new Tuple(mergedElements);

                // Evaluate the join condition
                if (condition != null) {
                    ExpressionVisitorImpl visitor = new ExpressionVisitorImpl(mergedTuple, getOutputSchema());
                    if (visitor.evaluate(condition)) {
                        return mergedTuple;
                    }
                } else {
                    // Cross product (if no condition is specified)
                    return mergedTuple;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in JoinOperator: " + e.getMessage());
        }
    }
}
