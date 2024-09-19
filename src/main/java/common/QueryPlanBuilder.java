package common;

import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Class to translate a JSQLParser statement into a relational algebra query
 * plan. For now only
 * works for Statements that are Selects, and specifically PlainSelects. Could
 * implement the visitor
 * pattern on the statement, but doesn't for simplicity as we do not handle
 * nesting or other complex
 * query features.
 *
 * <p>
 * Query plan fixes join order to the order found in the from clause and uses a
 * left deep tree
 * join. Maximally pushes selections on individual relations and evaluates join
 * conditions as early
 * as possible in the join tree. Projections (if any) are not pushed and
 * evaluated in a single
 * projection operator after the last join. Finally, sorting and duplicate
 * elimination are added if
 * needed.
 *
 * <p>
 * For the subset of SQL which is supported as well as assumptions on semantics,
 * see the Project
 * 2 student instructions, Section 2.1
 */
public class QueryPlanBuilder {
  public QueryPlanBuilder() {
  }

  /**
   * Top level method to translate statement to query plan
   *
   * @param stmt statement to be translated
   * @return the root of the query plan
   * @precondition stmt is a Select having a body that is a PlainSelect
   */

  public Operator buildPlan(Statement stmt) throws ExecutionControl.NotImplementedException {
    // Make sure the statement is a Select
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

    // Step 3: Handle JOINs (using left-deep join tree)
    List<Join> joins = plainSelect.getJoins();
    if (joins != null) {
      currentOperator = applyJoins(currentOperator, joins);
    }

    // Step 4: Apply projection (SELECT clause)
    currentOperator = new ProjectOperator(currentOperator, plainSelect); // Handle projections

    // Step 5: Handle ORDER BY clause (if present)
    List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
    if (orderByElements != null) {
      currentOperator = new SortOperator(currentOperator, orderByElements); // Apply sorting
    }

    // // Step 6: Handle DISTINCT clause
    // if (plainSelect.getDistinct() != null) {
    //   // If no ORDER BY clause, add SortOperator first
    //   if (orderByElements == null) {
    //     currentOperator = new SortOperator(currentOperator, null); // Sort by default columns
    //   }
    //   // Add DuplicateEliminationOperator
    //   currentOperator = new DuplicateEliminationOperator(currentOperator);
    // }

    // Step 7: Return the root of the query plan
    return currentOperator;
  }

  /**
   * Step 1: Handle the FROM clause by creating ScanOperators for each table.
   *
   * @param plainSelect The parsed SQL statement.
   * @return The initial operator for the FROM clause.
   */
  private Operator createFromClausePlan(PlainSelect plainSelect) {
    FromItem fromItem = plainSelect.getFromItem(); // This should be the first table in the FROM clause

    // Assuming no aliases and the table name is directly available.
    String tableName = fromItem.toString();

    // Use the ScanOperator, assuming we want to use the catalog for file paths
    ArrayList<Column> outputSchema = new ArrayList<>(); // Create an empty schema (for now)
    return new ScanOperator(outputSchema, tableName, true, null); // Always use the catalog
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

      // Use ScanOperator for the right table in the join
      ArrayList<Column> rightTableSchema = new ArrayList<>(); // Create an empty schema (for now)
      Operator rightChild = new ScanOperator(rightTableSchema, rightTableName, true, null);

      // Get the join condition
      @SuppressWarnings("deprecation")
      Expression joinCondition = join.getOnExpression();

      // Apply JoinOperator (left-deep tree, joining currentOperator with rightChild)
      currentOperator = new JoinOperator(currentOperator, rightChild, joinCondition);
    }

    return currentOperator;
  }
}
