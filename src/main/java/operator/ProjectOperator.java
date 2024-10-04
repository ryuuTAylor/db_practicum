package operator;

import common.Tuple;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

/**
 * The ProjectOperator selects specific columns from tuples based on the SELECT clause of a query.
 * It extends Operator to process and return tuples with only the desired columns.
 */
public class ProjectOperator extends Operator {
  private final Operator child;
  private final List<SelectItem> selectItems;
  private final ArrayList<Column> inputSchema;

  /**
   * Constructs a ProjectOperator with the specified child operator and PlainSelect query.
   *
   * @param child The child Operator providing input tuples.
   * @param plainSelect The PlainSelect object representing the SELECT clause.
   */
  public ProjectOperator(Operator child, PlainSelect plainSelect) {
    super(getProjectedSchema(child.getOutputSchema(), plainSelect.getSelectItems()));
    this.child = child;
    this.selectItems = plainSelect.getSelectItems();
    this.inputSchema = child.getOutputSchema();
  }

  /**
   * Determines the schema of the projected output based on the SELECT items.
   *
   * @param inputSchema The schema of the input tuples.
   * @param selectItems The list of SelectItems from the SELECT clause.
   * @return An ArrayList containing the projected schema.
   * @throws UnsupportedOperationException If the SELECT item type is unsupported.
   */
  private static ArrayList<Column> getProjectedSchema(
      ArrayList<Column> inputSchema, List<SelectItem> selectItems) {
    ArrayList<Column> projectedSchema = new ArrayList<>();
    for (SelectItem item : selectItems) {
      if (item instanceof AllColumns) {
        projectedSchema.addAll(inputSchema);
        break;
      } else if (item instanceof SelectExpressionItem) {
        Expression expr = ((SelectExpressionItem) item).getExpression();
        if (expr instanceof Column) {
          Column col = (Column) expr;
          projectedSchema.add(col);
        } else {
          throw new UnsupportedOperationException("Only columns are supported in SELECT clause.");
        }
      } else {
        throw new UnsupportedOperationException("Unsupported SELECT item.");
      }
    }
    return projectedSchema;
  }

  /** Resets the operator by resetting its child operator. */
  @Override
  public void reset() {
    child.reset();
  }

  /**
   * Retrieves the next projected tuple based on the SELECT clause.
   *
   * @return The next projected Tuple, or null if no more tuples are available.
   * @throws UnsupportedOperationException If the SELECT clause contains unsupported expressions.
   */
  @Override
  public Tuple getNextTuple() {
    Tuple tuple;
    if ((tuple = child.getNextTuple()) != null) {
      return extractTuple(tuple);
    }
    return null;
  }

  /**
   * Extracts the required columns from the input tuple to form the projected tuple.
   *
   * @param tuple The input Tuple to extract values from.
   * @return A new Tuple containing only the projected values.
   * @throws UnsupportedOperationException If the SELECT clause contains unsupported expressions.
   */
  private Tuple extractTuple(Tuple tuple) {
    ArrayList<Integer> extractedValues = new ArrayList<>();

    if (selectItems.size() == 1 && selectItems.get(0) instanceof AllColumns) {
      // SELECT *
      return tuple;
    }

    for (SelectItem item : selectItems) {
      if (item instanceof SelectExpressionItem) {
        Expression expr = ((SelectExpressionItem) item).getExpression();
        if (expr instanceof Column) {
          Column col = (Column) expr;
          String columnName = col.getColumnName();
          String tableName = col.getTable() != null ? col.getTable().getName() : null;
          int index = getColumnIndex(tableName, columnName);
          extractedValues.add(tuple.getElementAtIndex(index));
        } else {
          throw new UnsupportedOperationException("Only columns are supported in SELECT clause.");
        }
      } else {
        throw new UnsupportedOperationException("Unsupported SELECT item.");
      }
    }

    return new Tuple(extractedValues);
  }

  /**
   * Retrieves the index of a column in the input schema based on table and column names.
   *
   * @param tableName The name of the table containing the column.
   * @param columnName The name of the column.
   * @return The index of the column in the input schema.
   * @throws RuntimeException If the column is not found in the input schema.
   */
  private int getColumnIndex(String tableName, String columnName) {
    for (int i = 0; i < inputSchema.size(); i++) {
      Column col = inputSchema.get(i);
      String colTableName = col.getTable() != null ? col.getTable().getName() : null;
      String colName = col.getColumnName();

      if ((tableName == null || tableName.equals(colTableName)) && columnName.equals(colName)) {
        return i;
      }
    }
    throw new RuntimeException(
        "Column not found: " + (tableName != null ? tableName + "." : "") + columnName);
  }
}
