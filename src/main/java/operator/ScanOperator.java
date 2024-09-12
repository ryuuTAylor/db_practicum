package operator;

import common.DBCatalog;
import common.Tuple;
import java.io.*;
import java.util.*;
import net.sf.jsqlparser.schema.Column;

// import net.sf.jsqlparser.schema.Sequence.Parameter;

public class ScanOperator extends Operator {

  private BufferedReader reader; // Reader that reads from the table file
  private String filePath; // Path to the file containing the table data

  public ScanOperator(
      ArrayList<Column> outputSchema, String tableName, boolean useCatalog, String filePath) {
    super(outputSchema);

    // We can either manually provide the filePath, or query from Catalog
    if (useCatalog) {
      // Get the filePath from DBCatalog
      this.filePath = DBCatalog.getInstance().getFileForTable(tableName).getAbsolutePath();
    } else {
      // Use the manually provided filePath instead
      this.filePath = filePath;
    }

    init(); // Call the funciton init() to initialize the BufferedReader for the file
  }

  // Initialize the reader to open the table file
  private void init() {
    try {
      reader = new BufferedReader(new FileReader(filePath));
    } catch (IOException e) {
      e.printStackTrace(); // Handle exceptions
    }
  }

  @Override
  public void reset() {
    try {
      // Close the Existing Reader if exists
      if (reader != null) {
        reader.close();
      }

      // Reinitialize everything
      init(); // Reopen the file and set up a new BufferedReader

    } catch (IOException e) {
      e.printStackTrace(); // Handle exceptions
    }
  }

  @Override
  public Tuple getNextTuple() {
    try {
      // Read the next line from the file
      String line = reader.readLine();

      // We should return next Tuple, or null if we are at the end
      if (line == null) {
        return null;
      }

      // Use the provided common.Tuple class
      return new Tuple(line);

    } catch (IOException e) {
      e.printStackTrace(); // Handle exceptions
      return null; // Be default, if there's an error, we return null
    }
  }
}
