package common;

import java.util.ArrayList;
import java.util.List;
import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.*;

/**
 * Class to translate a JSQLParser statement into a relational algebra query
 * plan.
 */
public class QueryPlanBuilder {
  public QueryPlanBuilder() {
  }

  /**
   * Top-level method to translate a SQL statement into a query plan.
   *
   * @param stmt The SQL statement.
   * @return The root of the query plan.
   * @throws ExecutionControl.NotImplementedException
   */
  public Operator buildPlan(Statement stmt) throws ExecutionControl.NotImplementedException {
    // Make sure the statement is a SELECT statement
    if (!(stmt instanceof Select)) {
      throw new UnsupportedOperationException("Only SELECT statements are supported.");
    }

    // Extract the body of the SELECT statement (assuming it's a PlainSelect)
    PlainSelect plainSelect = (PlainSelect) ((Select) stmt).getSelectBody();

    // Step 1: Handle FROM clause (ScanOperators for each table)
    Operator currentOperator = createFromClausePlan(plainSelect);

    // Step 2: Apply the WHERE clause (Selection) if present
    Expression whereExpression = plainSelect.getWhere();
    if (whereExpression != null) {
      currentOperator = new SelectOperator(currentOperator, whereExpression); // Apply selection
    }

    // Step 3: Handle JOINs (using a left-deep join tree)
    List<Join> joins = plainSelect.getJoins();
    if (joins != null) {
      currentOperator = applyJoins(currentOperator, joins);
    }

    // Step 4: Apply projection (SELECT clause)
    currentOperator = new ProjectOperator(currentOperator, plainSelect); // Handle projections

    // Step 5: Return the root of the query plan
    return currentOperator;
  }

  /**
   * Step 1: Handle the FROM clause by creating ScanOperators for each table. This
   * method also
   * resolves table aliases.
   *
   * @param plainSelect The parsed SQL statement.
   * @return The initial operator for the FROM clause.
   */
  private Operator createFromClausePlan(PlainSelect plainSelect) {
    FromItem fromItem = plainSelect.getFromItem(); // This should be the first table in the FROM clause

    if (fromItem instanceof Table) {
      Table table = (Table) fromItem;
      String tableName = table.getName();
      String alias = table.getAlias() != null ? table.getAlias().getName() : null;

      // If there's an alias, register it with the catalog
      if (alias != null) {
        DBCatalog.getInstance().addAlias(alias, tableName);
      }

      // Get the schema using either the alias or table name
      ArrayList<Column> outputSchema = DBCatalog.getInstance().getSchema(tableName);
      return new ScanOperator(outputSchema, tableName, true, null); // Always use the catalog
    } else {
      throw new UnsupportedOperationException("Only table FROM items are supported.");
    }
  }

  /**
   * Step 3: Apply joins in the order they appear, building a left-deep join tree.
   *
   * @param currentOperator The current operator (starting with the FROM clause).
   * @param joins           The list of joins in the query.
   * @return An operator that incorporates the joins.
   */
  private Operator applyJoins(Operator currentOperator, List<Join> joins) {
    for (Join join : joins) {
      FromItem rightTable = join.getRightItem();
      String rightTableName = rightTable.toString();

      // Get the schema for the right table (alias-aware)
      ArrayList<Column> rightTableSchema = DBCatalog.getInstance().getSchema(rightTableName);

      // Use ScanOperator for the right table in the join
      Operator rightChild = new ScanOperator(rightTableSchema, rightTableName, true, null);

      // Get the join condition
      Expression joinCondition = join.getOnExpression();

      // Apply JoinOperator (left-deep tree, joining currentOperator with rightChild)
      currentOperator = new JoinOperator(currentOperator, rightChild, joinCondition);
    }

    return currentOperator;
  }
}
