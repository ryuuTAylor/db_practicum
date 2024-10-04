package operator;

import common.DBCatalog;
import common.Tuple;
import java.io.*;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

/**
 * The ScanOperator reads tuples from a table by scanning a data file. It extends Operator to
 * provide tuples sequentially from the table data.
 */
public class ScanOperator extends Operator {

  private BufferedReader reader; // Reader that reads from the table file
  private String filePath; // Path to the file containing the table data

  /**
   * Constructs a ScanOperator with the specified schema, table name, catalog usage, and file path.
   *
   * @param outputSchema The schema of the tuples to be read.
   * @param tableName The name of the table to scan.
   * @param useCatalog Flag indicating whether to use the catalog to find the file path.
   * @param filePath The file path to the table data (used if useCatalog is false).
   */
  public ScanOperator(
      ArrayList<Column> outputSchema, String tableName, boolean useCatalog, String filePath) {
    super(outputSchema);

    if (useCatalog) {
      this.filePath = DBCatalog.getInstance().getFileForTable(tableName).getAbsolutePath();
    } else {
      this.filePath = filePath;
    }

    init();
  }

  /** Initializes the BufferedReader to read from the specified file path. */
  private void init() {
    try {
      reader = new BufferedReader(new FileReader(filePath));
    } catch (IOException e) {
      e.printStackTrace(); // Handle exceptions
    }
  }

  /** Resets the ScanOperator by closing and reopening the file reader. */
  @Override
  public void reset() {
    try {
      if (reader != null) {
        reader.close();
      }
      init();
    } catch (IOException e) {
      e.printStackTrace(); // Handle exceptions
    }
  }

  /**
   * Retrieves the next tuple from the table by reading the next line from the file.
   *
   * @return The next Tuple, or null if the end of the file is reached.
   */
  @Override
  public Tuple getNextTuple() {
    try {
      String line = reader.readLine();
      if (line == null) {
        return null;
      }
      return new Tuple(line);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
