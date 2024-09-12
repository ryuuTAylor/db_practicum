import common.DBCatalog; // Import the provided DBCatalog
import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;
import operator.ScanOperator;

public class ScanOperatorTest {
  public static void main(String[] args) {
    // Set the data directory for DBCatalog (this points to the parent directory of
    // schema.txt and the data folder)
    DBCatalog.getInstance().setDataDirectory("src/test/taylor"); // Adjust this path accordingly

    // Define an empty output schema (you can modify this based on your table's
    // schema)
    ArrayList<Column> outputSchema = new ArrayList<>();

    // Test using the ScanOperator with a manually specified file path
    System.out.println("Testing with manual file path:");
    ScanOperator scanOperatorManual =
        new ScanOperator(outputSchema, "Sailors", false, "src/test/taylor/data/Sailors");
    testScanOperator(scanOperatorManual);

    // Test using the ScanOperator with Catalog (using DBCatalog to retrieve the
    // file path)
    System.out.println("Testing with Catalog:");
    ScanOperator scanOperatorCatalog = new ScanOperator(outputSchema, "Sailors", true, null);
    testScanOperator(scanOperatorCatalog);
  }

  // Helper method to test the ScanOperator
  private static void testScanOperator(ScanOperator scanOperator) {
    Tuple tuple;

    // Fetch and print each tuple until we reach the end
    while ((tuple = scanOperator.getNextTuple()) != null) {
      System.out.println("Tuple: " + tuple);
    }

    // Reset the operator and fetch the tuples again
    System.out.println("After reset:");
    scanOperator.reset();
    while ((tuple = scanOperator.getNextTuple()) != null) {
      System.out.println("Tuple after reset: " + tuple);
    }
  }
}
