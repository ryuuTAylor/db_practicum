import common.DBCatalog;
import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import operator.ProjectOperator;
import operator.ScanOperator;

public class ProjectOperatorTest {
  public static void main(String[] args) throws Exception {
    // Set the data directory
    DBCatalog.getInstance().setDataDirectory("src/test/taylor");

    // Get the schema
    ArrayList<Column> sailorsSchema = DBCatalog.getInstance().getSchema("Sailors");

    // Create a ScanOperator
    ScanOperator scanOperator = new ScanOperator(sailorsSchema, "Sailors", true, null);

    // Define a projection (e.g., SELECT A, B FROM Sailors)
    String selectClause = "SELECT A, B FROM Sailors";
    PlainSelect plainSelect =
        (PlainSelect)
            ((net.sf.jsqlparser.statement.select.Select) CCJSqlParserUtil.parse(selectClause))
                .getSelectBody();

    // Create a ProjectOperator
    ProjectOperator projectOperator = new ProjectOperator(scanOperator, plainSelect);

    // Test the ProjectOperator
    Tuple tuple;
    while ((tuple = projectOperator.getNextTuple()) != null) {
      System.out.println("Projected Tuple: " + tuple);
    }

    // Reset and test again
    System.out.println("After reset:");
    projectOperator.reset();
    while ((tuple = projectOperator.getNextTuple()) != null) {
      System.out.println("Projected Tuple after reset: " + tuple);
    }
  }
}
