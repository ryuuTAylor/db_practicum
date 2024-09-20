package operator;

import common.Tuple;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

/**
 * SortOperator sorts tuples based on the ORDER BY clause in the query, then by
 * the original schema.
 */
public class SortOperator extends Operator {
  private final Operator child;
  private final List<OrderByElement> orderByElements;
  private List<Tuple> sortedTuples;
  private int currentIndex;

  /**
   * Constructs a SortOperator.
   *
   * @param child           The child operator to sort.
   * @param orderByElements The list of ORDER BY elements specifying sort order.
   */
  public SortOperator(Operator child, List<OrderByElement> orderByElements) {
    super(child.getOutputSchema());
    this.child = child;
    this.orderByElements = orderByElements;
    this.sortedTuples = new ArrayList<>();
    this.currentIndex = 0;

    // Initialize by collecting and sorting all tuples
    collectAndSortTuples();
  }

  /**
   * Collects all tuples from the child operator and sorts them based on ORDER BY.
   */
  private void collectAndSortTuples() {
    Tuple tuple;
    while ((tuple = child.getNextTuple()) != null) {
      sortedTuples.add(tuple);
    }

    if (orderByElements != null && !orderByElements.isEmpty()) {
      sortedTuples.sort(
          new Comparator<Tuple>() {
            @Override
            public int compare(Tuple t1, Tuple t2) {
              for (OrderByElement element : orderByElements) {
                Expression expr = element.getExpression();
                if (!(expr instanceof Column)) {
                  throw new UnsupportedOperationException(
                      "Only column expressions are supported in ORDER BY.");
                }
                Column col = (Column) expr;
                String tableAlias = col.getTable() != null ? col.getTable().getName() : null;
                String columnName = col.getColumnName();
                int index = getColumnIndex(tableAlias, columnName);
                int val1 = t1.getElementAtIndex(index);
                int val2 = t2.getElementAtIndex(index);
                int comparison = Integer.compare(val1, val2);
                if (comparison != 0) {
                  return comparison;
                }
              }
              return 0;
            }
          });
    }
  }

  /**
   * Retrieves the next sorted tuple.
   *
   * @return The next Tuple in sorted order, or null if no more tuples.
   */
  @Override
  public Tuple getNextTuple() {
    if (currentIndex < sortedTuples.size()) {
      return sortedTuples.get(currentIndex++);
    }
    return null; // No more tuples
  }

  /** Resets the SortOperator to iterate through sorted tuples again. */
  @Override
  public void reset() {
    currentIndex = 0;
  }

  /**
   * Maps a column to its index in the tuple based on the schema.
   *
   * @param tableAlias The alias of the table, if any.
   * @param columnName The name of the column.
   * @return The index of the column in the tuple.
   * @throws RuntimeException If the column is not found.
   */
  private int getColumnIndex(String tableAlias, String columnName) {
    for (int i = 0; i < getOutputSchema().size(); i++) {
      Column col = getOutputSchema().get(i);
      String colTable = col.getTable().getName();
      String colName = col.getColumnName();

      if (tableAlias != null) {
        if (colTable.equals(tableAlias) && colName.equals(columnName)) {
          return i;
        }
      } else {
        if (colName.equals(columnName)) {
          return i;
        }
      }
    }
    throw new RuntimeException(
        "Column not found for ORDER BY: "
            + (tableAlias != null ? tableAlias + "." : "")
            + columnName);
  }
}
