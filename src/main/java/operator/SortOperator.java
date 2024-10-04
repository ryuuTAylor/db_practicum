package operator;

import common.Tuple;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

/**
 * The SortOperator sorts tuples based on the ORDER BY clause of a query. It extends Operator to
 * collect all input tuples, sort them, and provide sorted tuples sequentially.
 */
public class SortOperator extends Operator {
  private final Operator child;
  private final List<OrderByElement> orderByElements;
  private List<Tuple> sortedTuples;
  private int currentIndex;

  /**
   * Constructs a SortOperator with the specified child operator and ORDER BY elements.
   *
   * @param child The child Operator providing input tuples.
   * @param orderByElements The list of OrderByElements defining the sort order.
   */
  public SortOperator(Operator child, List<OrderByElement> orderByElements) {
    super(child.getOutputSchema());
    this.child = child;
    this.orderByElements = orderByElements;
    this.sortedTuples = new ArrayList<>();
    this.currentIndex = 0;

    collectAndSortTuples();
  }

  /**
   * Collects all tuples from the child operator and sorts them based on the ORDER BY clause.
   *
   * @throws UnsupportedOperationException If the ORDER BY clause contains unsupported expressions.
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
              // Break ties using remaining attributes in order
              for (int i = 0; i < getOutputSchema().size(); i++) {
                int val1 = t1.getElementAtIndex(i);
                int val2 = t2.getElementAtIndex(i);
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
   * @return The next sorted Tuple, or null if all tuples have been returned.
   */
  @Override
  public Tuple getNextTuple() {
    if (currentIndex < sortedTuples.size()) {
      return sortedTuples.get(currentIndex++);
    }
    return null;
  }

  /** Resets the SortOperator by resetting the current index to the start of the sorted list. */
  @Override
  public void reset() {
    currentIndex = 0;
  }

  /**
   * Retrieves the index of a column in the output schema based on table alias and column name.
   *
   * @param tableAlias The alias of the table containing the column.
   * @param columnName The name of the column.
   * @return The index of the column in the output schema.
   * @throws RuntimeException If the column is not found in the output schema.
   */
  private int getColumnIndex(String tableAlias, String columnName) {
    for (int i = 0; i < getOutputSchema().size(); i++) {
      Column col = getOutputSchema().get(i);
      String colTable = col.getTable().getName();
      String colName = col.getColumnName();

      if ((tableAlias == null || tableAlias.equals(colTable)) && columnName.equals(colName)) {
        return i;
      }
    }
    throw new RuntimeException(
        "Column not found for ORDER BY: "
            + (tableAlias != null ? tableAlias + "." : "")
            + columnName);
  }
}
