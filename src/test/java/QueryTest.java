import common.DBCatalog;
import common.QueryPlanBuilder;
import common.Tuple;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import operator.Operator;

public class QueryTest {
  public static void main(String[] args) throws Exception {
    // Set the data directory
    DBCatalog.getInstance()
        .setDataDirectory("src/test/taylor"); // Updated directory to point to the data
    // folder containing both Sailors and Reserves

    // Test Query 5
    testQuery5();

    // Test Query 6
    testQuery6();

    // Test Query 7
    testQuery7();

    // Test Query 11
    testQuery11();

    testQuery27();

    // Test Query 40
    testQuery40();
  }

  private static void testQuery5() throws Exception {
    System.out.println("Testing Query 5:");
    String query = "SELECT * FROM Sailors WHERE Sailors.B >= Sailors.C;";
    List<String> expectedOutput = new ArrayList<>();
    expectedOutput.add("1,200,50");
    expectedOutput.add("2,200,200");
    expectedOutput.add("4,100,50");

    executeAndCompare(query, expectedOutput);
  }

  private static void testQuery6() throws Exception {
    System.out.println("\nTesting Query 6:");
    String query = "SELECT Sailors.A FROM Sailors WHERE Sailors.B >= Sailors.C;";
    List<String> expectedOutput = new ArrayList<>();
    expectedOutput.add("1");
    expectedOutput.add("2");
    expectedOutput.add("4");

    executeAndCompare(query, expectedOutput);
  }

  private static void testQuery7() throws Exception {
    System.out.println("\nTesting Query 7:");
    String query =
        "SELECT Sailors.A FROM Sailors WHERE Sailors.B >= Sailors.C AND Sailors.B < Sailors.C;";
    List<String> expectedOutput = new ArrayList<>(); // Expected to be empty

    executeAndCompare(query, expectedOutput);
  }

  private static void testQuery11() throws Exception {
    System.out.println("\nTesting Query 11:");
    String query =
        "SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G AND Sailors.C > Sailors.B;";
    List<String> expectedOutput = new ArrayList<>();
    expectedOutput.add("3,100,105,3,102");

    executeAndCompare(query, expectedOutput);
  }

  private static void testQuery27() throws Exception {
    System.out.println("\nTesting Query 27:");
    String query =
        "SELECT * FROM TestTable2 WHERE TestTable2.K >= TestTable2.L AND TestTable2.L <= TestTable2.M;";
    List<String> expectedOutput = new ArrayList<>();
    expectedOutput.add("101,2,3");
    expectedOutput.add("102,3,4");
    expectedOutput.add("103,1,1");
    expectedOutput.add("107,2,8");
    expectedOutput.add("109,2,100");

    executeAndCompare(query, expectedOutput);
  }

  private static void testQuery40() throws Exception {
    System.out.println("\nTesting Query 40:");
    String query =
        "SELECT * FROM Sailors S, Reserves R WHERE S.A = R.G AND S.C > S.B ORDER BY S.A;";
    List<String> expectedOutput = new ArrayList<>();
    expectedOutput.add("3,100,105,3,102");

    executeAndCompare(query, expectedOutput);
  }

  private static void executeAndCompare(String query, List<String> expectedOutput)
      throws Exception {
    // Parse the SQL query
    Statement statement = CCJSqlParserUtil.parse(query);

    // Build the query plan
    QueryPlanBuilder queryPlanBuilder = new QueryPlanBuilder();
    Operator plan = queryPlanBuilder.buildPlan(statement);

    // Execute the plan and collect the results
    List<String> actualOutput = new ArrayList<>();
    Tuple tuple;
    while ((tuple = plan.getNextTuple()) != null) {
      actualOutput.add(tuple.toString());
      System.out.println("Output Tuple: " + tuple);
    }

    // Compare the actual output with the expected output
    if (actualOutput.equals(expectedOutput)) {
      System.out.println("Test Passed!");
    } else {
      System.out.println("Test Failed!");
      System.out.println("Expected Output:");
      for (String expected : expectedOutput) {
        System.out.println(expected);
      }
      System.out.println("Actual Output:");
      for (String actual : actualOutput) {
        System.out.println(actual);
      }
    }
  }
}
