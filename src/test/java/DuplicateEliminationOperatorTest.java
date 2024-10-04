import common.DBCatalog;
import common.Tuple;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import operator.DuplicateEliminationOperator;
import operator.ScanOperator;
import operator.SortOperator;

public class DuplicateEliminationOperatorTest {
  public static void main(String[] args) throws Exception {
    // Set the data directory
    DBCatalog.getInstance().setDataDirectory("src/test/taylor");

    // Get the schema
    ArrayList<Column> sailorsSchema = DBCatalog.getInstance().getSchema("Sailors");

    // Create a ScanOperator
    ScanOperator scanOperator = new ScanOperator(sailorsSchema, "Sailors", true, null);

    // Define ORDER BY clause to sort the input (e.g., ORDER BY A, B, C)
    String orderByClause = "SELECT * FROM Sailors ORDER BY A, B, C";
    PlainSelect plainSelect =
        (PlainSelect)
            ((net.sf.jsqlparser.statement.select.Select) CCJSqlParserUtil.parse(orderByClause))
                .getSelectBody();
    List<OrderByElement> orderByElements = plainSelect.getOrderByElements();

    // Create a SortOperator
    SortOperator sortOperator = new SortOperator(scanOperator, orderByElements);

    // Create a DuplicateEliminationOperator
    DuplicateEliminationOperator dupElimOperator =
        new DuplicateEliminationOperator(sortOperator.getOutputSchema(), sortOperator);

    // Test the DuplicateEliminationOperator
    Tuple tuple;
    while ((tuple = dupElimOperator.getNextTuple()) != null) {
      System.out.println("Unique Tuple: " + tuple);
    }

    // Reset and test again
    System.out.println("After reset:");
    dupElimOperator.reset();
    while ((tuple = dupElimOperator.getNextTuple()) != null) {
      System.out.println("Unique Tuple after reset: " + tuple);
    }
  }
}
