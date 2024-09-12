package operator;

import common.Tuple;
import expression.ExpressionVisitorImpl;
import net.sf.jsqlparser.expression.Expression;

/** SelectOperator applies a condition to tuples from its child operator. */
public class SelectOperator extends Operator {
  private final Operator child;
  private final Expression condition;

  public SelectOperator(Operator child, Expression condition) {
    super(child.getOutputSchema());
    this.child = child;
    this.condition = condition;
  }

  @Override
  public void reset() {
    child.reset();
  }

  @Override
  public Tuple getNextTuple() {
    Tuple tuple;

    // Continuously fetch and evaluate tuples from the child operator
    while ((tuple = child.getNextTuple()) != null) {
      ExpressionVisitorImpl visitor = new ExpressionVisitorImpl(tuple, getOutputSchema());

      // Check if the tuple satisfies the condition
      if (visitor.evaluate(condition)) {
        return tuple; // Return the tuple if the condition is met
      }
    }
    return null; // Return null if no more matching tuples are found
  }
}
