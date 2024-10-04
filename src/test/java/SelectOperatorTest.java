import common.DBCatalog;
import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import operator.ScanOperator;
import operator.SelectOperator;

public class SelectOperatorTest {
  public static void main(String[] args) throws Exception {
    // Set the data directory
    DBCatalog.getInstance().setDataDirectory("src/test/taylor");

    // Get the schema
    ArrayList<Column> sailorsSchema = DBCatalog.getInstance().getSchema("Sailors");

    // Create a ScanOperator for Sailors
    ScanOperator scanOperator = new ScanOperator(sailorsSchema, "Sailors", true, "src/test/taylor");

    // Define a selection condition (e.g., A < 5)
    Expression condition = CCJSqlParserUtil.parseCondExpression("A < 5");

    // Create a SelectOperator with the condition
    SelectOperator selectOperator = new SelectOperator(scanOperator, condition);

    // Test the SelectOperator
    Tuple tuple;
    while ((tuple = selectOperator.getNextTuple()) != null) {
      System.out.println("Selected Tuple: " + tuple);
    }

    // Reset and test again
    System.out.println("After reset:");
    selectOperator.reset();
    while ((tuple = selectOperator.getNextTuple()) != null) {
      System.out.println("Selected Tuple after reset: " + tuple);
    }
  }
}
