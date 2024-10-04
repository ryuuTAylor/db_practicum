package operator;

import common.Tuple;
import expression.ExpressionVisitorImpl;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/**
 * The JoinOperator implements the tuple nested loop join algorithm. It joins tuples from the left
 * and right child operators based on a specified join condition.
 */
public class JoinOperator extends Operator {
  private final Operator leftChild;
  private final Operator rightChild;
  private final Expression condition;

  private Tuple currentLeftTuple;

  /**
   * Constructs a JoinOperator with the specified left and right child operators and join condition.
   *
   * @param leftChild The left child Operator.
   * @param rightChild The right child Operator.
   * @param condition The join condition Expression.
   */
  public JoinOperator(Operator leftChild, Operator rightChild, Expression condition) {
    super(mergeSchemas(leftChild.getOutputSchema(), rightChild.getOutputSchema()));
    this.leftChild = leftChild;
    this.rightChild = rightChild;
    this.condition = condition;
    this.currentLeftTuple = null;
  }

  /**
   * Merges the schemas of the left and right child operators into a single schema.
   *
   * @param leftSchema The schema of the left child.
   * @param rightSchema The schema of the right child.
   * @return An ArrayList containing the merged schema.
   */
  private static ArrayList<Column> mergeSchemas(
      ArrayList<Column> leftSchema, ArrayList<Column> rightSchema) {
    ArrayList<Column> merged = new ArrayList<>(leftSchema);
    merged.addAll(rightSchema);
    return merged;
  }

  /** Resets the operator by resetting both child operators and clearing the current left tuple. */
  @Override
  public void reset() {
    leftChild.reset();
    rightChild.reset();
    currentLeftTuple = null;
  }

  /**
   * Retrieves the next joined tuple that satisfies the join condition.
   *
   * @return The next joined Tuple, or null if no more joined tuples are available.
   * @throws RuntimeException If an error occurs during tuple processing.
   */
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

        Tuple rightTuple;
        while ((rightTuple = rightChild.getNextTuple()) != null) {
          // Merge tuples
          ArrayList<Integer> mergedElements = new ArrayList<>(currentLeftTuple.getAllElements());
          mergedElements.addAll(rightTuple.getAllElements());
          Tuple mergedTuple = new Tuple(mergedElements);

          // Evaluate the join condition
          if (condition != null) {
            ExpressionVisitorImpl visitor =
                new ExpressionVisitorImpl(mergedTuple, getOutputSchema());
            if (visitor.evaluate(condition)) {
              return mergedTuple;
            }
          } else {
            // Cross product (if no condition is specified)
            return mergedTuple;
          }
        }
        currentLeftTuple = null; // Move to next left tuple
      }
    } catch (Exception e) {
      throw new RuntimeException("Error in JoinOperator: " + e.getMessage());
    }
  }
}
