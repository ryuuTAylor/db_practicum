package operator;

import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

/**
 * The DuplicateEliminationOperator removes duplicate tuples from its input. It assumes that the
 * input tuples are sorted, enabling efficient duplicate detection.
 */
public class DuplicateEliminationOperator extends Operator {
  private final Operator child;
  private Tuple lastReturnedTuple;

  /**
   * Constructs a DuplicateEliminationOperator with the specified output schema and child operator.
   *
   * @param outputSchema The schema of the output tuples after duplicate elimination.
   * @param child The child Operator providing input tuples.
   */
  public DuplicateEliminationOperator(ArrayList<Column> outputSchema, Operator child) {
    super(outputSchema);
    this.child = child;
    this.lastReturnedTuple = null;
  }

  /** Resets the operator by resetting its child and clearing the last returned tuple. */
  @Override
  public void reset() {
    child.reset();
    lastReturnedTuple = null;
  }

  /**
   * Retrieves the next unique tuple by eliminating duplicates from the child operator's output.
   *
   * @return The next unique Tuple, or null if no more tuples are available.
   */
  @Override
  public Tuple getNextTuple() {
    Tuple currentTuple;
    while ((currentTuple = child.getNextTuple()) != null) {
      if (!currentTuple.equals(lastReturnedTuple)) {
        lastReturnedTuple = currentTuple;
        return currentTuple;
      }
    }
    return null;
  }
}
