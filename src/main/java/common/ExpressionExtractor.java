package common;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;

/**
 * ExpressionExtractor is responsible for extracting the conditions (expressions) from the WHERE
 * clause of a SQL query that are relevant to a specific table. It extends the
 * ExpressionVisitorAdapter to traverse the expression tree and filter out expressions that only
 * involve the target table.
 */
public class ExpressionExtractor extends ExpressionVisitorAdapter {

  // The name of the target table for which the conditions are being extracted
  private String targetTable;

  // The expression (conditions) relevant to the target table
  private Expression expression = null;

  /**
   * Constructor that initializes the extractor with the target table's name.
   *
   * @param tableName The name of the table for which we want to extract expressions.
   */
  public ExpressionExtractor(String tableName) {
    this.targetTable = tableName;
  }

  /**
   * Returns the expression that has been extracted after the visit operations.
   *
   * @return The extracted expression relevant to the target table.
   */
  public Expression getExpression() {
    return expression;
  }

  /**
   * Visits and processes an AND expression (a conjunction of two expressions). It recursively
   * processes the left and right sub-expressions of the AND expression to extract relevant
   * expressions for the target table.
   *
   * @param andExpression The AND expression to process.
   */
  @Override
  public void visit(AndExpression andExpression) {
    // Get the left and right expressions from the AND condition
    Expression left = andExpression.getLeftExpression();
    Expression right = andExpression.getRightExpression();

    // Create new ExpressionExtractor for each side of the AND expression
    ExpressionExtractor leftExtractor = new ExpressionExtractor(targetTable);
    left.accept(leftExtractor);

    ExpressionExtractor rightExtractor = new ExpressionExtractor(targetTable);
    right.accept(rightExtractor);

    // Get the extracted expressions from both sides
    Expression leftExpr = leftExtractor.getExpression();
    Expression rightExpr = rightExtractor.getExpression();

    // Combine the left and right extracted expressions
    if (leftExpr != null && rightExpr != null) {
      expression = combineExpressions(leftExpr, rightExpr);
    } else if (leftExpr != null) {
      expression = combineExpressions(expression, leftExpr);
    } else if (rightExpr != null) {
      expression = combineExpressions(expression, rightExpr);
    }
  }

  /**
   * Helper method to combine two expressions using an AND condition. If the first expression is
   * null, the second expression is returned as-is.
   *
   * @param expr1 The first expression.
   * @param expr2 The second expression.
   * @return The combined expression (expr1 AND expr2).
   */
  private Expression combineExpressions(Expression expr1, Expression expr2) {
    if (expr1 == null) {
      return expr2; // If there is no existing expression, return the new one
    } else {
      return new AndExpression(expr1, expr2); // Combine both expressions using AND
    }
  }

  /**
   * Visits and processes an equality condition (e.g., A = B).
   *
   * @param equalsTo The EqualsTo expression to process.
   */
  @Override
  public void visit(EqualsTo equalsTo) {
    processBinaryExpression(equalsTo);
  }

  /**
   * Visits and processes a greater-than condition (e.g., A > B).
   *
   * @param greaterThan The GreaterThan expression to process.
   */
  @Override
  public void visit(GreaterThan greaterThan) {
    processBinaryExpression(greaterThan);
  }

  /**
   * Visits and processes a greater-than-or-equal condition (e.g., A >= B).
   *
   * @param greaterThanEquals The GreaterThanEquals expression to process.
   */
  @Override
  public void visit(GreaterThanEquals greaterThanEquals) {
    processBinaryExpression(greaterThanEquals);
  }

  /**
   * Visits and processes a less-than condition (e.g., A < B).
   *
   * @param minorThan The MinorThan expression to process.
   */
  @Override
  public void visit(MinorThan minorThan) {
    processBinaryExpression(minorThan);
  }

  /**
   * Visits and processes a less-than-or-equal condition (e.g., A <= B).
   *
   * @param minorThanEquals The MinorThanEquals expression to process.
   */
  @Override
  public void visit(MinorThanEquals minorThanEquals) {
    processBinaryExpression(minorThanEquals);
  }

  /**
   * Visits and processes a not-equals condition (e.g., A != B).
   *
   * @param notEqualsTo The NotEqualsTo expression to process.
   */
  @Override
  public void visit(NotEqualsTo notEqualsTo) {
    processBinaryExpression(notEqualsTo);
  }

  /**
   * Processes a binary expression (e.g., A = B, A > B) by checking if it involves only the target
   * table. If the condition involves only the target table, it is added to the extracted
   * expression.
   *
   * @param expr The binary expression to process.
   */
  private void processBinaryExpression(BinaryExpression expr) {
    // Extract the tables involved in the left and right sides of the binary
    // expression
    List<String> leftTables = getTablesInExpression(expr.getLeftExpression());
    List<String> rightTables = getTablesInExpression(expr.getRightExpression());

    // Check if the expression only involves the target table
    boolean leftMatches = leftTables.size() == 1 && leftTables.contains(targetTable);
    boolean rightMatches = rightTables.size() == 1 && rightTables.contains(targetTable);

    // If either side involves only the target table, or if both sides match the
    // target table,
    // the expression is relevant to the target table and is combined with the
    // extracted expression.
    if ((leftMatches && rightTables.isEmpty())
        || (rightMatches && leftTables.isEmpty())
        || (leftMatches && rightMatches)) {
      expression = combineExpressions(expression, expr);
    }
  }

  /**
   * Retrieves the list of tables involved in a given expression by using the TableNameFinder class
   * to collect table names.
   *
   * @param expr The expression to analyze.
   * @return A list of table names involved in the expression.
   */
  private List<String> getTablesInExpression(Expression expr) {
    // Use TableNameFinder to extract all tables from the expression
    TableNameFinder finder = new TableNameFinder();
    expr.accept(finder);
    return new ArrayList<>(finder.getTables()); // Convert the set of tables to a list
  }
}
