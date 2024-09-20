package operator;

import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

/**
 * DuplicateEliminationOperator removes duplicate tuples from the input. Assumes
 * the input is
 * sorted.
 */
public class DuplicateEliminationOperator extends Operator {
  private final Operator child;
  private Tuple lastReturnedTuple;

  public DuplicateEliminationOperator(ArrayList<Column> outputSchema, Operator child) {
    super(outputSchema);
    this.child = child;
    this.lastReturnedTuple = null;
  }

  @Override
  public void reset() {
    child.reset();
    lastReturnedTuple = null;
  }

  @Override
  public Tuple getNextTuple() {
    Tuple currentTuple;
    while ((currentTuple = child.getNextTuple()) != null) {
      if (!currentTuple.equals(lastReturnedTuple)) {
        lastReturnedTuple = currentTuple;
        return currentTuple;
      }
    }
    return null; // No more unique tuples
  }
}