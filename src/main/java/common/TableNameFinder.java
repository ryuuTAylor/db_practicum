package common;

import java.util.HashSet;
import java.util.Set;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

/**
 * The TableNameFinder class traverses SQL expressions to identify and collect the names of tables
 * involved. It extends ExpressionVisitorAdapter to visit different types of expressions and extract
 * table names from columns.
 */
public class TableNameFinder extends ExpressionVisitorAdapter {

  private Set<String> tables = new HashSet<>();

  /**
   * Retrieves the set of table names found in the expressions.
   *
   * @return A Set containing the names of the tables.
   */
  public Set<String> getTables() {
    return tables;
  }

  /**
   * Visits a Column expression and extracts the table name if available.
   *
   * @param column The Column expression to visit.
   */
  @Override
  public void visit(Column column) {
    String tableName = column.getTable() != null ? column.getTable().getName() : null;
    if (tableName != null) {
      tables.add(tableName);
    }
  }

  /**
   * Visits an AndExpression and recursively visits its left and right expressions.
   *
   * @param expr The AndExpression to visit.
   */
  @Override
  public void visit(AndExpression expr) {
    expr.getLeftExpression().accept(this);
    expr.getRightExpression().accept(this);
  }

  /**
   * Visits an EqualsTo expression and recursively visits its left and right expressions.
   *
   * @param expr The EqualsTo expression to visit.
   */
  @Override
  public void visit(EqualsTo expr) {
    expr.getLeftExpression().accept(this);
    expr.getRightExpression().accept(this);
  }

  /**
   * Visits a GreaterThan expression and recursively visits its left and right expressions.
   *
   * @param expr The GreaterThan expression to visit.
   */
  @Override
  public void visit(GreaterThan expr) {
    expr.getLeftExpression().accept(this);
    expr.getRightExpression().accept(this);
  }

  /**
   * Visits a GreaterThanEquals expression and recursively visits its left and right expressions.
   *
   * @param expr The GreaterThanEquals expression to visit.
   */
  @Override
  public void visit(GreaterThanEquals expr) {
    expr.getLeftExpression().accept(this);
    expr.getRightExpression().accept(this);
  }

  /**
   * Visits a MinorThan expression and recursively visits its left and right expressions.
   *
   * @param expr The MinorThan expression to visit.
   */
  @Override
  public void visit(MinorThan expr) {
    expr.getLeftExpression().accept(this);
    expr.getRightExpression().accept(this);
  }

  /**
   * Visits a MinorThanEquals expression and recursively visits its left and right expressions.
   *
   * @param expr The MinorThanEquals expression to visit.
   */
  @Override
  public void visit(MinorThanEquals expr) {
    expr.getLeftExpression().accept(this);
    expr.getRightExpression().accept(this);
  }

  /**
   * Visits a NotEqualsTo expression and recursively visits its left and right expressions.
   *
   * @param expr The NotEqualsTo expression to visit.
   */
  @Override
  public void visit(NotEqualsTo expr) {
    expr.getLeftExpression().accept(this);
    expr.getRightExpression().accept(this);
  }
}
