package common;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.*;

/**
 * The JoinExpressionExtractor class is responsible for determining whether a given binary
 * expression (e.g., EqualsTo, GreaterThan) is relevant to a join between two tables. The class
 * identifies if the expression involves columns from one or more tables specified in the join
 * operation.
 *
 * <p>It works by visiting each binary expression (e.g., A = B, A > B) and checking if one side of
 * the expression involves columns from the "left" tables and the other side involves the "right"
 * table. If such a condition is found, the expression is marked as relevant for the join operation.
 */
public class JoinExpressionExtractor extends ExpressionVisitorAdapter {

  private List<String> leftTables; // List of tables from the left side of the join
  private String rightTable; // The table from the right side of the join
  private boolean isRelevant = false; // Boolean flag to indicate whether the expression is relevant

  /**
   * Constructor to initialize the left and right tables involved in the join.
   *
   * @param leftTables A list of table names representing the left side of the join.
   * @param rightTable The table name representing the right side of the join.
   */
  public JoinExpressionExtractor(List<String> leftTables, String rightTable) {
    this.leftTables = leftTables;
    this.rightTable = rightTable;
  }

  /**
   * Method to check whether the processed expression is relevant to the join.
   *
   * @return A boolean value indicating whether the expression is relevant for the join.
   */
  public boolean isRelevant() {
    return isRelevant;
  }

  /**
   * Visits an EqualsTo expression (e.g., A = B) to check its relevance to the join.
   *
   * @param equalsTo The EqualsTo expression to be visited.
   */
  @Override
  public void visit(EqualsTo equalsTo) {
    processBinaryExpression(equalsTo); // Delegate the check to a helper method
  }

  /**
   * Visits a GreaterThan expression (e.g., A > B) to check its relevance to the join.
   *
   * @param greaterThan The GreaterThan expression to be visited.
   */
  @Override
  public void visit(GreaterThan greaterThan) {
    processBinaryExpression(greaterThan); // Delegate the check to a helper method
  }

  /**
   * Visits a GreaterThanEquals expression (e.g., A >= B) to check its relevance to the join.
   *
   * @param greaterThanEquals The GreaterThanEquals expression to be visited.
   */
  @Override
  public void visit(GreaterThanEquals greaterThanEquals) {
    processBinaryExpression(greaterThanEquals); // Delegate the check to a helper method
  }

  /**
   * Visits a MinorThan expression (e.g., A < B) to check its relevance to the join.
   *
   * @param minorThan The MinorThan expression to be visited.
   */
  @Override
  public void visit(MinorThan minorThan) {
    processBinaryExpression(minorThan); // Delegate the check to a helper method
  }

  /**
   * Visits a MinorThanEquals expression (e.g., A <= B) to check its relevance to the join.
   *
   * @param minorThanEquals The MinorThanEquals expression to be visited.
   */
  @Override
  public void visit(MinorThanEquals minorThanEquals) {
    processBinaryExpression(minorThanEquals); // Delegate the check to a helper method
  }

  /**
   * Visits a NotEqualsTo expression (e.g., A != B) to check its relevance to the join.
   *
   * @param notEqualsTo The NotEqualsTo expression to be visited.
   */
  @Override
  public void visit(NotEqualsTo notEqualsTo) {
    processBinaryExpression(notEqualsTo); // Delegate the check to a helper method
  }

  /**
   * Helper method to process any binary expression (e.g., A = B, A > B). It determines whether the
   * expression is relevant for the join by checking whether one side of the expression refers to a
   * table from the left set of tables and the other side refers to the right table.
   *
   * @param expr The binary expression to be checked for relevance.
   */
  private void processBinaryExpression(BinaryExpression expr) {
    // Get the list of tables on both sides of the binary expression
    List<String> leftExprTables = getTablesInExpression(expr.getLeftExpression());
    List<String> rightExprTables = getTablesInExpression(expr.getRightExpression());

    // Check if the left side contains tables from the left set and the right side
    // refers to the right table
    boolean leftContainsLeftTable = !leftTables.isEmpty() && leftTables.containsAll(leftExprTables);
    boolean rightIsRightTable = rightExprTables.size() == 1 && rightExprTables.contains(rightTable);

    // Check if the right side contains tables from the left set and the left side
    // refers to the right table
    boolean rightContainsLeftTable =
        !leftTables.isEmpty() && leftTables.containsAll(rightExprTables);
    boolean leftIsRightTable = leftExprTables.size() == 1 && leftExprTables.contains(rightTable);

    // If either condition is satisfied, mark the expression as relevant
    if ((leftContainsLeftTable && rightIsRightTable)
        || (rightContainsLeftTable && leftIsRightTable)) {
      isRelevant = true;
    }
  }

  /**
   * Helper method to extract the list of tables involved in a given expression. It uses the
   * TableNameFinder to retrieve the table names referenced by the expression.
   *
   * @param expr The expression whose tables are to be extracted.
   * @return A list of table names involved in the expression.
   */
  private List<String> getTablesInExpression(Expression expr) {
    // Use TableNameFinder to extract table names from the expression
    TableNameFinder finder = new TableNameFinder();
    expr.accept(finder);
    return new ArrayList<>(finder.getTables()); // Convert the Set of tables to a List
  }
}
