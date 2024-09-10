package operator;

import java.util.*;

public class DuplicateCatalog {
  private static DuplicateCatalog instance; // Singleton instance
  private Map<String, String> tableFilePaths; // Map of table names to file paths

  // Private constructor to prevent instantiation
  private DuplicateCatalog() {
    tableFilePaths = new HashMap<>();
    // Populate the map with table names and their corresponding file paths
    tableFilePaths.put("Sailors", "src/test/taylor/sailors.txt");
  }

  // Public method to get the singleton instance
  public static DuplicateCatalog getInstance() {
    if (instance == null) {
      instance = new DuplicateCatalog(); // Create the instance if it doesn't exist
    }
    return instance;
  }

  // Method to get the file path for a given table
  public String getTableFilePath(String tableName) {
    return tableFilePaths.get(tableName);
  }
}
