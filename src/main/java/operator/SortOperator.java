package operator;

import common.Tuple;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.schema.Column;
import java.util.ArrayList;
// import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortOperator extends Operator {
  private final Operator child;
  private final List<OrderByElement> orderByElements;
  private List<Tuple> sortedTuples;
  private int currentIndex;

  public SortOperator(Operator child, List<OrderByElement> orderByElements) {
    super(child.getOutputSchema());
    this.child = child;
    this.orderByElements = orderByElements;
    this.sortedTuples = new ArrayList<>();
    this.currentIndex = 0;

    // Initialize by collecting and sorting all tuples
    collectAndSortTuples();
  }

  // Collect all tuples from the child operator and sort them
  private void collectAndSortTuples() {
    Tuple tuple;
    while ((tuple = child.getNextTuple()) != null) {
      sortedTuples.add(tuple);
    }

    // Sort the tuples based on the ORDER BY columns
    sortedTuples.sort(new Comparator<Tuple>() {
      @Override
      public int compare(Tuple t1, Tuple t2) {
        for (OrderByElement element : orderByElements) {
          // Assuming the orderByElement gives the index of the column in schema
          String columnName = element.getExpression().toString();
          int columnIndex = getColumnIndex(getOutputSchema(), columnName);
          int val1 = t1.getElementAtIndex(columnIndex);
          int val2 = t2.getElementAtIndex(columnIndex);
          int comparison = Integer.compare(val1, val2);

          // If the values are different, return the comparison result
          if (comparison != 0) {
            return comparison;
          }
        }
        return 0;
      }
    });
  }

  @Override
  public Tuple getNextTuple() {
    if (currentIndex < sortedTuples.size()) {
      return sortedTuples.get(currentIndex++);
    }
    return null; // No more tuples
  }

  @Override
  public void reset() {
    currentIndex = 0; // Reset the index to iterate through sorted tuples again
  }

  // Helper method to map column name to index using the schema
  private static int getColumnIndex(ArrayList<Column> schema, String columnName) {
    for (int i = 0; i < schema.size(); i++) {
      if (schema.get(i).getColumnName().equals(columnName)) {
        return i;
      }
    }
    throw new RuntimeException("Column not found: " + columnName);
  }
}
