package operator;

import common.Tuple;

import java.util.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

public class ProjectOperator extends Operator {
    private final Operator child;
    private final PlainSelect plainSelect;
    private final ArrayList<Column> schema;

    // query plan builder passes in a plain select
    // where child is either SelectOperator (if WHERE clause)
    // or ScanOperator (if no where clause)
    public ProjectOperator(Operator child, PlainSelect plainSelect) {
        super(child.outputSchema);
        this.child = child;
        this.plainSelect = plainSelect;
        this.schema = this.outputSchema;
    }

    public Tuple extractedTuple(Tuple tuple, PlainSelect plainSelect) {
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        List extractedColumns = new ArrayList<Integer>();
        for (SelectItem selectItem : selectItems) {
            if (selectItem instanceof AllColumns) {
                // add all columns to extractedColumns
                return tuple;
            } else if (selectItem instanceof SelectExpressionItem) {
                // can assume selectItem's expression is column
                Column column = (Column) ((SelectExpressionItem) selectItem).getExpression();

                String columnName = column.getColumnName();

                // Map column name to index (assuming schema provides this)
                int columnIndex = getColumnIndex(schema, columnName);

                // Get the tuple value for the column index
                int tupleValue = tuple.getElementAtIndex(columnIndex);

                // add to extracted columns
                extractedColumns.add(tupleValue);
            }
        }
        // return extracted columns as a Tuple
        return new Tuple((ArrayList<Integer>) extractedColumns);
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

    @Override
    public Tuple getNextTuple() {
        Tuple tuple;
        if ((tuple = child.getNextTuple()) != null) {
            return extractedTuple(tuple, plainSelect);
        }
        return null;
    }

    @Override
    public void reset() {
        child.reset();
    }
}