import common.DBCatalog;
import common.Tuple;
import java.io.StringReader;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.ScanOperator;
import operator.SelectOperator;

/** Simple test for SelectOperator */
public class SelectOperatorTest {

  public static void main(String[] args) {
    // Set the data directory for DBCatalog (this points to the parent directory of
    // schema.txt and the data folder)
    DBCatalog.getInstance().setDataDirectory("src/test/taylor"); // Adjust this path accordingly

    // Define an empty output schema (modify this based on your table's schema)
    ArrayList<Column> outputSchema = new ArrayList<>();

    // Define a test SQL query with a WHERE condition
    String query = "SELECT * FROM Sailors WHERE Sailors.A = 1";

    // Test SelectOperator with a manually specified file path
    System.out.println("Testing SelectOperator with manual file path:");
    ScanOperator scanOperator =
        new ScanOperator(outputSchema, "Sailors", false, "src/test/taylor/data/Sailors");
    testSelectOperator(scanOperator, query);

    // Test using the SelectOperator with Catalog (using DBCatalog to retrieve the
    // file path)
    System.out.println("Testing SelectOperator with Catalog:");
    ScanOperator scanOperatorCatalog = new ScanOperator(outputSchema, "Sailors", true, null);
    testSelectOperator(scanOperatorCatalog, query);
  }

  // Helper method to test SelectOperator
  private static void testSelectOperator(ScanOperator scanOperator, String query) {
    try {
      // Parse the SQL query using JSQLParser
      Statement statement = CCJSqlParserUtil.parse(new StringReader(query));
      Select select = (Select) statement;
      PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

      // Extract the WHERE condition
      Expression whereCondition = plainSelect.getWhere();

      // Create a SelectOperator that filters based on the WHERE condition
      SelectOperator selectOperator = new SelectOperator(scanOperator, whereCondition);

      Tuple tuple;

      // Fetch and print each tuple that satisfies the WHERE condition
      while ((tuple = selectOperator.getNextTuple()) != null) {
        System.out.println("Selected Tuple: " + tuple);
      }

      // Reset the operator and fetch the tuples again after the reset
      System.out.println("After reset:");
      selectOperator.reset();
      while ((tuple = selectOperator.getNextTuple()) != null) {
        System.out.println("Selected Tuple after reset: " + tuple);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
