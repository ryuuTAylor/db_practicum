package operator;

import common.Tuple;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

/** ProjectOperator handles the projection of specified columns from tuples. */
public class ProjectOperator extends Operator {
    private final Operator child; // The input operator (e.g., ScanOperator, JoinOperator)
    private final PlainSelect plainSelect; // The SQL statement part to handle projections
    private final ArrayList<Column> schema; // Schema of the current table (outputSchema)

    public ProjectOperator(Operator child, PlainSelect plainSelect) {
        super(child.getOutputSchema()); // Use schema of the child as the initial schema
        this.child = child;
        this.plainSelect = plainSelect;
        this.schema = this.outputSchema; // The schema defines column names and their indices
    }

    private Tuple extractTuple(Tuple tuple, PlainSelect plainSelect) {
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        ArrayList<Integer> extractedColumns = new ArrayList<>();
        boolean selectAll = false;

        // Check if we're selecting all columns (i.e., SELECT *)
        for (SelectItem selectItem : selectItems) {
            if (selectItem instanceof AllColumns) {
                selectAll = true;
                break;
            }
        }

        // If SELECT *, return the entire tuple
        if (selectAll) {
            return tuple;
        }

        // Extract only the specified columns
        for (SelectItem selectItem : selectItems) {
            if (selectItem instanceof SelectExpressionItem) {
                Column column = (Column) ((SelectExpressionItem) selectItem).getExpression();
                String columnName = column.getColumnName();

                // Find the index of the column in the schema
                int columnIndex = getColumnIndex(schema, columnName);

                // Get the value from the tuple at the specified column index
                int tupleValue = tuple.getElementAtIndex(columnIndex);

                // Add the value to the list of extracted columns
                extractedColumns.add(tupleValue);
            }
        }

        // Return the extracted columns as a new tuple
        return new Tuple(extractedColumns);
    }

    private int getColumnIndex(ArrayList<Column> schema, String columnName) {
        for (int i = 0; i < schema.size(); i++) {
            String schemaColumn = schema.get(i).getColumnName();

            // Handle table aliasing (table.column or just column)
            if (schemaColumn.equals(columnName) || schemaColumn.endsWith("." + columnName)) {
                return i;
            }
        }
        throw new RuntimeException("Column not found: " + columnName);
    }

    @Override
    public Tuple getNextTuple() {
        Tuple tuple;
        if ((tuple = child.getNextTuple()) != null) {
            return extractTuple(tuple, plainSelect); // Apply projection to the tuple
        }
        return null;
    }

    @Override
    public void reset() {
        child.reset();
    }
}