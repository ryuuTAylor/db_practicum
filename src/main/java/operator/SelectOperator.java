package operator;

import common.Tuple;
import expression.ExpressionVisitorImpl;
import net.sf.jsqlparser.expression.Expression;

/**
 * The SelectOperator applies a selection condition to tuples from its child operator. It extends
 * Operator to filter tuples based on the specified WHERE clause condition.
 */
public class SelectOperator extends Operator {
  private final Operator child;
  private final Expression condition;

  /**
   * Constructs a SelectOperator with the specified child operator and selection condition.
   *
   * @param child The child Operator providing input tuples.
   * @param condition The selection condition Expression to apply.
   */
  public SelectOperator(Operator child, Expression condition) {
    super(child.getOutputSchema());
    this.child = child;
    this.condition = condition;
  }

  /** Resets the SelectOperator by resetting its child operator. */
  @Override
  public void reset() {
    child.reset();
  }

  /**
   * Retrieves the next tuple that satisfies the selection condition.
   *
   * @return The next Tuple that meets the condition, or null if no such tuple exists.
   */
  @Override
  public Tuple getNextTuple() {
    Tuple tuple;

    while ((tuple = child.getNextTuple()) != null) {
      ExpressionVisitorImpl visitor = new ExpressionVisitorImpl(tuple, getOutputSchema());

      boolean satisfiesCondition = visitor.evaluate(condition);

      if (satisfiesCondition) {
        return tuple;
      }
    }
    return null;
  }
}
