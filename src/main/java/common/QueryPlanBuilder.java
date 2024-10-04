package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import operator.*;

/** Builds a query plan from a SQL statement. */
public class QueryPlanBuilder {

  public QueryPlanBuilder() {}

  /**
   * Builds the query plan for the provided SQL SELECT statement.
   *
   * @param stmt SQL SELECT statement.
   * @return Root operator of the query plan.
   */
  public Operator buildPlan(Statement stmt) {
    if (!(stmt instanceof Select)) {
      throw new UnsupportedOperationException("Only SELECT statements are supported.");
    }

    PlainSelect plainSelect = (PlainSelect) ((Select) stmt).getSelectBody();

    // Step 1: Handle FROM clause
    List<Operator> scanOperators = new ArrayList<>();
    List<String> tableNames = new ArrayList<>();
    Map<String, String> aliasToTable = new HashMap<>();
    processFromClause(plainSelect, scanOperators, tableNames, aliasToTable);

    // Step 2: Handle WHERE clause
    Expression whereExpression = plainSelect.getWhere();
    Expression decomposedSelectExpr = null;
    List<Expression> joinConditions = new ArrayList<>();
    if (whereExpression != null) {
      WhereExpressionVisitor visitor = new WhereExpressionVisitor(tableNames);
      whereExpression.accept(visitor);
      decomposedSelectExpr = visitor.getSelectExpression();
      joinConditions = visitor.getJoinExpressions();
    }

    // Step 3: Apply selection conditions
    Map<String, Operator> operatorsMap = new HashMap<>();
    for (int i = 0; i < tableNames.size(); i++) {
      String tableName = tableNames.get(i);
      Operator op = scanOperators.get(i);
      Expression selectionExpr = null;
      if (decomposedSelectExpr != null) {
        ExpressionExtractor extractor = new ExpressionExtractor(tableName);
        decomposedSelectExpr.accept(extractor);
        selectionExpr = extractor.getExpression();
      }
      if (selectionExpr != null) {
        op = new SelectOperator(op, selectionExpr);
      }
      operatorsMap.put(tableName, op);
    }

    // Step 4: Build the join tree
    Operator currentOperator = operatorsMap.get(tableNames.get(0));
    for (int i = 1; i < tableNames.size(); i++) {
      String rightTable = tableNames.get(i);
      Operator rightOperator = operatorsMap.get(rightTable);
      Expression joinExpr = null;
      List<Expression> relevantJoins = new ArrayList<>();
      for (Expression expr : joinConditions) {
        JoinExpressionExtractor joinExtractor =
            new JoinExpressionExtractor(getTablesInOperator(currentOperator), rightTable);
        expr.accept(joinExtractor);
        if (joinExtractor.isRelevant()) {
          relevantJoins.add(expr);
        }
      }
      if (!relevantJoins.isEmpty()) {
        joinExpr = combineExpressions(relevantJoins);
      }
      currentOperator = new JoinOperator(currentOperator, rightOperator, joinExpr);
    }

    // Step 5: Apply projection
    currentOperator = new ProjectOperator(currentOperator, plainSelect);

    // Step 6: Handle ORDER BY
    if (plainSelect.getOrderByElements() != null) {
      currentOperator = new SortOperator(currentOperator, plainSelect.getOrderByElements());
    }

    // Step 7: Handle DISTINCT
    if (plainSelect.getDistinct() != null) {
      currentOperator =
          new DuplicateEliminationOperator(currentOperator.getOutputSchema(), currentOperator);
    }

    // Return the root of the query plan
    return currentOperator;
  }

  /** Processes the FROM clause to create scan operators for each table. */
  private void processFromClause(
      PlainSelect plainSelect,
      List<Operator> scanOperators,
      List<String> tableNames,
      Map<String, String> aliasToTable) {

    // Process the main table in FROM
    FromItem fromItem = plainSelect.getFromItem();
    processFromItem(fromItem, scanOperators, tableNames, aliasToTable);

    // Process joins
    List<Join> joins = plainSelect.getJoins();
    if (joins != null) {
      for (Join join : joins) {
        FromItem joinItem = join.getRightItem();
        processFromItem(joinItem, scanOperators, tableNames, aliasToTable);
      }
    }
  }

  /** Processes a single table or alias from the FROM clause. */
  private void processFromItem(
      FromItem fromItem,
      List<Operator> scanOperators,
      List<String> tableNames,
      Map<String, String> aliasToTable) {
    if (fromItem instanceof Table) {
      Table table = (Table) fromItem;
      String tableName = table.getName();
      String alias = table.getAlias() != null ? table.getAlias().getName() : null;

      String schemaTableName = tableName;
      if (alias != null) {
        DBCatalog.getInstance().addAlias(alias, tableName);
        schemaTableName = alias;
      }

      ArrayList<Column> outputSchema = DBCatalog.getInstance().getSchema(schemaTableName);
      Operator scanOp = new ScanOperator(outputSchema, schemaTableName, true, null);
      scanOperators.add(scanOp);
      tableNames.add(schemaTableName);
    } else {
      throw new UnsupportedOperationException("Only table FROM items are supported.");
    }
  }

  /** Combines a list of expressions using AND. */
  private Expression combineExpressions(List<Expression> expressions) {
    if (expressions == null || expressions.isEmpty()) {
      return null;
    }
    Expression combined = expressions.get(0);
    for (int i = 1; i < expressions.size(); i++) {
      combined = new AndExpression(combined, expressions.get(i));
    }
    return combined;
  }

  /** Retrieves the tables referenced in an operator's schema. */
  private List<String> getTablesInOperator(Operator op) {
    List<String> tableNames = new ArrayList<>();
    for (Column col : op.getOutputSchema()) {
      String tableName = col.getTable().getName();
      if (!tableNames.contains(tableName)) {
        tableNames.add(tableName);
      }
    }
    return tableNames;
  }
}
