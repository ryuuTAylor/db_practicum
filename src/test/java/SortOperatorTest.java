import common.DBCatalog;
import common.Tuple;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import operator.ScanOperator;
import operator.SortOperator;

public class SortOperatorTest {
  public static void main(String[] args) throws Exception {
    // Set the data directory
    DBCatalog.getInstance().setDataDirectory("src/test/taylor");

    // Get the schema
    ArrayList<Column> sailorsSchema = DBCatalog.getInstance().getSchema("Sailors");

    // Create a ScanOperator
    ScanOperator scanOperator = new ScanOperator(sailorsSchema, "Sailors", true, null);

    // Define ORDER BY clause (e.g., ORDER BY B)
    String orderByClause = "SELECT * FROM Sailors ORDER BY B";
    PlainSelect plainSelect =
        (PlainSelect)
            ((net.sf.jsqlparser.statement.select.Select) CCJSqlParserUtil.parse(orderByClause))
                .getSelectBody();
    List<OrderByElement> orderByElements = plainSelect.getOrderByElements();

    // Create a SortOperator
    SortOperator sortOperator = new SortOperator(scanOperator, orderByElements);

    // Test the SortOperator
    Tuple tuple;
    while ((tuple = sortOperator.getNextTuple()) != null) {
      System.out.println("Sorted Tuple: " + tuple);
    }

    // Reset and test again
    System.out.println("After reset:");
    sortOperator.reset();
    while ((tuple = sortOperator.getNextTuple()) != null) {
      System.out.println("Sorted Tuple after reset: " + tuple);
    }
  }
}
