// DBCatalog.java
package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to contain information about database - names of tables, schema of each table and file
 * where each table is located. Uses singleton pattern.
 */
public class DBCatalog {
  private final Logger logger = LogManager.getLogger();

  private final HashMap<String, ArrayList<Column>> tables;
  private final HashMap<String, String> aliases; // Alias to real table mapping
  private static DBCatalog db;

  private String dbDirectory;

  /** Reads schemaFile and populates schema information */
  private DBCatalog() {
    tables = new HashMap<>();
    aliases = new HashMap<>();
  }

  /**
   * Instance getter for singleton pattern, lazy initialization on first invocation
   *
   * @return unique DB catalog instance
   */
  public static DBCatalog getInstance() {
    if (db == null) {
      db = new DBCatalog();
    }
    return db;
  }

  /**
   * Sets the data directory for the database catalog.
   *
   * @param directory The input directory.
   */
  public void setDataDirectory(String directory) {
    try {
      dbDirectory = directory;
      BufferedReader br = new BufferedReader(new FileReader(directory + "/schema.txt"));
      String line;
      while ((line = br.readLine()) != null) {
        String[] tokens = line.split("\\s+");
        String tableName = tokens[0];
        ArrayList<Column> cols = new ArrayList<>();
        for (int i = 1; i < tokens.length; i++) {
          cols.add(new Column(new Table(null, tableName), tokens[i]));
        }
        tables.put(tableName, cols);
      }
      br.close();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  /**
   * Gets path to file where a particular table is stored
   *
   * @param tableName table name
   * @return file where table is found on disk
   */
  public File getFileForTable(String tableName) {
    return new File(dbDirectory + "/data/" + resolveAlias(tableName));
  }

  /**
   * Adds an alias for a table.
   *
   * @param alias The alias.
   * @param tableName The real table name.
   */
  public void addAlias(String alias, String tableName) {
    aliases.put(alias, tableName);
  }

  /**
   * Resolves the alias to the actual table name.
   *
   * @param alias The alias to resolve.
   * @return The actual table name, or the alias if no mapping exists.
   */
  public String resolveAlias(String alias) {
    return aliases.getOrDefault(alias, alias);
  }

  /**
   * Gets the schema of a table.
   *
   * @param tableName the name of the table or alias.
   * @return the schema as a list of Columns.
   */
  public ArrayList<Column> getSchema(String tableName) {
    String resolvedName = resolveAlias(tableName);
    ArrayList<Column> schema = tables.get(resolvedName);
    if (schema == null) {
      throw new RuntimeException("Schema not found for table: " + resolvedName);
    }
    // Update the schema columns with the correct table name (alias or original)
    ArrayList<Column> aliasedSchema = new ArrayList<>();
    for (Column col : schema) {
      Column aliasedCol = new Column(new Table(null, tableName), col.getColumnName());
      aliasedSchema.add(aliasedCol);
    }
    return aliasedSchema;
  }
}
