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
 * Class to contain information about database - names of tables, schema of each
 * table and file
 * where each table is located. Uses singleton pattern.
 *
 * <p>
 * Assumes dbDirectory has a schema.txt file and a /data subdirectory containing
 * one file per
 * relation, named "relname".
 *
 * <p>
 * Call by using DBCatalog.getInstance();
 */
public class DBCatalog {
  private final Logger logger = LogManager.getLogger();

  // Map of table names to their column schemas
  private final HashMap<String, ArrayList<Column>> tables;

  // Map for table aliases
  private final HashMap<String, String> aliasMap;

  // Singleton instance
  private static DBCatalog db;

  private String dbDirectory;

  /** Private constructor for singleton pattern */
  private DBCatalog() {
    tables = new HashMap<>();
    aliasMap = new HashMap<>();
  }

  /** Get the singleton instance */
  public static DBCatalog getInstance() {
    if (db == null) {
      db = new DBCatalog();
    }
    return db;
  }

  /** Set the data directory and load schema */
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

  /** Add an alias for a table */
  public void addAlias(String alias, String tableName) {
    aliasMap.put(alias, tableName);
  }

  /** Resolve table name by checking alias map first */
  public String resolveTableName(String tableNameOrAlias) {
    return aliasMap.getOrDefault(tableNameOrAlias, tableNameOrAlias);
  }

  /** Get file path for the table */
  public File getFileForTable(String tableName) {
    return new File(dbDirectory + "/data/" + resolveTableName(tableName));
  }

  /** Get schema for a table or alias */
  public ArrayList<Column> getSchema(String tableNameOrAlias) {
    String tableName = resolveTableName(tableNameOrAlias);
    ArrayList<Column> schema = tables.get(tableName);
    if (schema == null) {
      throw new RuntimeException("Schema not found for table: " + tableName);
    }
    return schema;
  }
}
