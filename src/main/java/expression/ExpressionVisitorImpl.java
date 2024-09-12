package expression;

import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;

/** The ExpressionVisitorImpl class evaluates SQL WHERE clause expressions on tuples. */
public class ExpressionVisitorImpl extends ExpressionVisitorAdapter {

  private final Tuple tuple;
  private final ArrayList<Column> schema; // This is to map column names to indices
  private boolean result; // Stores the result of the expression evaluation

  public ExpressionVisitorImpl(Tuple tuple, ArrayList<Column> schema) {
    this.tuple = tuple;
    this.schema = schema;
    this.result = false; // Default to false; it will change if the condition is met
  }

  /**
   * Evaluate the expression on the tuple.
   *
   * @param expression The SQL expression (e.g., WHERE id = 4).
   * @return true if the expression evaluates to true for the tuple, otherwise false.
   */
  public boolean evaluate(Expression expression) {
    expression.accept(this); // Traverse the expression tree
    return result; // Return the final evaluation result
  }

  @Override
  public void visit(EqualsTo equalsTo) {
    // Handle the equality expression (e.g., WHERE id = 4)
    Expression leftExpression = equalsTo.getLeftExpression();
    Expression rightExpression = equalsTo.getRightExpression();

    // Check if the left side is a column
    if (leftExpression instanceof Column) {
      Column column = (Column) leftExpression;
      String columnName = column.getColumnName();

      // Map column name to index (assuming schema provides this)
      int columnIndex = getColumnIndex(columnName);

      // Get the tuple value for the column index
      int tupleValue = tuple.getElementAtIndex(columnIndex);

      // Now compare the right side value (which could be a LongValue or StringValue)
      if (rightExpression instanceof LongValue) {
        LongValue value = (LongValue) rightExpression;
        result = tupleValue == value.getValue();
      }
    }
  }

  // Helper method to map column name to index using the schema
  private int getColumnIndex(String columnName) {
    for (int i = 0; i < schema.size(); i++) {
      if (schema.get(i).getColumnName().equals(columnName)) {
        return i;
      }
    }
    throw new RuntimeException("Column not found: " + columnName);
  }

  @Override
  public void visit(LongValue longValue) {
    // Handle LongValue
  }

  @Override
  public void visit(Column column) {
    // Handle Column (if needed)
  }
}
