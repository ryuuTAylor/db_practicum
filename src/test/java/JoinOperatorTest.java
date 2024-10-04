import common.DBCatalog;
import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import operator.JoinOperator;
import operator.ScanOperator;

public class JoinOperatorTest {
  public static void main(String[] args) throws Exception {
    // Set the data directory
    DBCatalog.getInstance().setDataDirectory("src/test/taylor");

    // Get schemas
    ArrayList<Column> sailorsSchema = DBCatalog.getInstance().getSchema("Sailors");
    ArrayList<Column> reservationsSchema = DBCatalog.getInstance().getSchema("Reservations");

    // Create ScanOperators
    ScanOperator sailorsScan = new ScanOperator(sailorsSchema, "Sailors", true, null);
    ScanOperator reservationsScan =
        new ScanOperator(reservationsSchema, "Reservations", true, null);

    // Define a join condition, e.g., Sailors.A = Reservations.G
    Expression joinCondition = CCJSqlParserUtil.parseCondExpression("Sailors.A = Reservations.G");

    // Create a JoinOperator
    JoinOperator joinOperator = new JoinOperator(sailorsScan, reservationsScan, joinCondition);

    // Test the JoinOperator
    Tuple tuple;
    while ((tuple = joinOperator.getNextTuple()) != null) {
      System.out.println("Joined Tuple: " + tuple);
    }

    // Reset and test again
    System.out.println("After reset:");
    joinOperator.reset();
    while ((tuple = joinOperator.getNextTuple()) != null) {
      System.out.println("Joined Tuple after reset: " + tuple);
    }
  }
}
