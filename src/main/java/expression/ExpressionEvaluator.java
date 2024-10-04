package expression;

import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.schema.Column;

/** Evaluates an expression to an integer value based on the provided tuple and schema. */
public class ExpressionEvaluator extends ExpressionVisitorAdapter {

  private final Tuple tuple;
  private final ArrayList<Column> schema; // This is to map column names to indices
  private int value; // Stores the computed value

  /**
   * Constructs an ExpressionEvaluator with the specified tuple and schema.
   *
   * @param tuple The Tuple to evaluate the expression against.
   * @param schema The schema mapping column names to their indices in the tuple.
   */
  public ExpressionEvaluator(Tuple tuple, ArrayList<Column> schema) {
    this.tuple = tuple;
    this.schema = schema;
  }

  /**
   * Returns the computed integer value after evaluation.
   *
   * @return The integer value of the expression.
   */
  public int getValue() {
    return value;
  }

  @Override
  public void visit(Column column) {
    String columnName = column.getColumnName();
    String tableName = column.getTable() != null ? column.getTable().getName() : null;
    int columnIndex = getColumnIndex(tableName, columnName);
    value = tuple.getElementAtIndex(columnIndex);
  }

  @Override
  public void visit(LongValue longValue) {
    value = (int) longValue.getValue();
  }

  @Override
  public void visit(Addition addition) {
    evaluateBinaryExpression(addition, (a, b) -> a + b);
  }

  @Override
  public void visit(Subtraction subtraction) {
    evaluateBinaryExpression(subtraction, (a, b) -> a - b);
  }

  @Override
  public void visit(Multiplication multiplication) {
    evaluateBinaryExpression(multiplication, (a, b) -> a * b);
  }

  @Override
  public void visit(Division division) {
    evaluateBinaryExpression(division, (a, b) -> a / b);
  }

  @Override
  public void visit(Parenthesis parenthesis) {
    parenthesis.getExpression().accept(this);
  }

  /** An interface for arithmetic operations on two integers. */
  private interface ArithmeticOperation {
    int compute(int a, int b);
  }

  /**
   * Evaluates a binary arithmetic expression.
   *
   * @param expr The binary expression to evaluate.
   * @param operation The operation to apply (e.g., addition, subtraction).
   */
  private void evaluateBinaryExpression(BinaryExpression expr, ArithmeticOperation operation) {
    ExpressionEvaluator leftEval = new ExpressionEvaluator(tuple, schema);
    expr.getLeftExpression().accept(leftEval);
    int leftValue = leftEval.getValue();

    ExpressionEvaluator rightEval = new ExpressionEvaluator(tuple, schema);
    expr.getRightExpression().accept(rightEval);
    int rightValue = rightEval.getValue();

    value = operation.compute(leftValue, rightValue);
  }

  /**
   * Retrieves the index of a column in the schema based on table and column names.
   *
   * @param tableName The name of the table containing the column.
   * @param columnName The name of the column.
   * @return The index of the column in the schema.
   * @throws RuntimeException If the column is not found in the schema.
   */
  private int getColumnIndex(String tableName, String columnName) {
    for (int i = 0; i < schema.size(); i++) {
      Column col = schema.get(i);
      String colTableName = col.getTable() != null ? col.getTable().getName() : null;
      String colName = col.getColumnName();

      if ((tableName == null || tableName.equals(colTableName)) && columnName.equals(colName)) {
        return i;
      }
    }
    throw new RuntimeException(
        "Column not found: " + (tableName != null ? tableName + "." : "") + columnName);
  }
}
