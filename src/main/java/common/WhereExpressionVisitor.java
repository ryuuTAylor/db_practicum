package common;

import java.util.*;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;

public class WhereExpressionVisitor extends ExpressionVisitorAdapter {

  private List<String> tableNames;
  private Expression selectExpression = null;
  private List<Expression> joinExpressions = new ArrayList<>();

  public WhereExpressionVisitor(List<String> tableNames) {
    this.tableNames = tableNames;
  }

  public Expression getSelectExpression() {
    return selectExpression;
  }

  public List<Expression> getJoinExpressions() {
    return joinExpressions;
  }

  @Override
  public void visit(AndExpression andExpression) {
    andExpression.getLeftExpression().accept(this);
    andExpression.getRightExpression().accept(this);
  }

  @Override
  public void visit(EqualsTo equalsTo) {
    processBinaryExpression(equalsTo);
  }

  @Override
  public void visit(GreaterThan greaterThan) {
    processBinaryExpression(greaterThan);
  }

  @Override
  public void visit(GreaterThanEquals greaterThanEquals) {
    processBinaryExpression(greaterThanEquals);
  }

  @Override
  public void visit(MinorThan minorThan) {
    processBinaryExpression(minorThan);
  }

  @Override
  public void visit(MinorThanEquals minorThanEquals) {
    processBinaryExpression(minorThanEquals);
  }

  @Override
  public void visit(NotEqualsTo notEqualsTo) {
    processBinaryExpression(notEqualsTo);
  }

  private void processBinaryExpression(BinaryExpression expr) {
    List<String> leftTables = getTablesInExpression(expr.getLeftExpression());
    List<String> rightTables = getTablesInExpression(expr.getRightExpression());

    Set<String> uniqueTables = new HashSet<>();
    uniqueTables.addAll(leftTables);
    uniqueTables.addAll(rightTables);

    if (uniqueTables.size() == 1) {
      // Selection condition
      if (selectExpression == null) {
        selectExpression = expr;
      } else {
        selectExpression = new AndExpression(selectExpression, expr);
      }
    } else if (uniqueTables.size() > 1) {
      // Join condition
      joinExpressions.add(expr);
    }
  }

  private List<String> getTablesInExpression(Expression expr) {
    TableNameFinder finder = new TableNameFinder();
    expr.accept(finder);
    return new ArrayList<>(finder.getTables());
  }
}
