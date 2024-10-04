package expression;

import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

/**
 * The ExpressionVisitorImpl class evaluates SQL WHERE clause expressions against a given tuple. It
 * extends ExpressionVisitorAdapter to traverse and evaluate different types of expressions.
 */
public class ExpressionVisitorImpl extends ExpressionVisitorAdapter {

  private final Tuple tuple;
  private final ArrayList<Column> schema; // This is to map column names to indices
  private boolean result; // Stores the result of the expression evaluation

  /**
   * Constructs an ExpressionVisitorImpl with the specified tuple and schema.
   *
   * @param tuple The Tuple to evaluate the expression against.
   * @param schema The schema mapping column names to their indices in the tuple.
   */
  public ExpressionVisitorImpl(Tuple tuple, ArrayList<Column> schema) {
    this.tuple = tuple;
    this.schema = schema;
    this.result = false; // Default to false; it will change if the condition is met
  }

  /**
   * Evaluates the given SQL expression against the tuple.
   *
   * @param expression The SQL expression to evaluate (e.g., WHERE id < 3).
   * @return true if the expression evaluates to true for the tuple, otherwise false.
   */
  public boolean evaluate(Expression expression) {
    expression.accept(this); // Traverse the expression tree
    return result; // Return the final evaluation result
  }

  /**
   * Visits an EqualsTo expression and evaluates its comparison.
   *
   * @param equalsTo The EqualsTo expression to visit.
   */
  @Override
  public void visit(EqualsTo equalsTo) {
    evaluateComparison(equalsTo, (a, b) -> a == b);
  }

  /**
   * Visits a NotEqualsTo expression and evaluates its comparison.
   *
   * @param notEqualsTo The NotEqualsTo expression to visit.
   */
  @Override
  public void visit(NotEqualsTo notEqualsTo) {
    evaluateComparison(notEqualsTo, (a, b) -> a != b);
  }

  /**
   * Visits a GreaterThan expression and evaluates its comparison.
   *
   * @param greaterThan The GreaterThan expression to visit.
   */
  @Override
  public void visit(GreaterThan greaterThan) {
    evaluateComparison(greaterThan, (a, b) -> a > b);
  }

  /**
   * Visits a GreaterThanEquals expression and evaluates its comparison.
   *
   * @param greaterThanEquals The GreaterThanEquals expression to visit.
   */
  @Override
  public void visit(GreaterThanEquals greaterThanEquals) {
    evaluateComparison(greaterThanEquals, (a, b) -> a >= b);
  }

  /**
   * Visits a MinorThan expression and evaluates its comparison.
   *
   * @param minorThan The MinorThan expression to visit.
   */
  @Override
  public void visit(MinorThan minorThan) {
    evaluateComparison(minorThan, (a, b) -> a < b);
  }

  /**
   * Visits a MinorThanEquals expression and evaluates its comparison.
   *
   * @param minorThanEquals The MinorThanEquals expression to visit.
   */
  @Override
  public void visit(MinorThanEquals minorThanEquals) {
    evaluateComparison(minorThanEquals, (a, b) -> a <= b);
  }

  /**
   * Visits an AndExpression and evaluates the logical AND of its left and right expressions.
   *
   * @param andExpr The AndExpression to visit.
   */
  @Override
  public void visit(AndExpression andExpr) {
    ExpressionVisitorImpl leftVisitor = new ExpressionVisitorImpl(tuple, schema);
    boolean leftResult = leftVisitor.evaluate(andExpr.getLeftExpression());

    if (!leftResult) {
      result = false;
      return;
    }

    ExpressionVisitorImpl rightVisitor = new ExpressionVisitorImpl(tuple, schema);
    boolean rightResult = rightVisitor.evaluate(andExpr.getRightExpression());

    result = rightResult;
  }

  /** An interface for comparing two integer values. */
  private interface Comparator {
    boolean compare(int a, int b);
  }

  /**
   * Evaluates a binary expression using the provided comparator.
   *
   * @param expr The BinaryExpression to evaluate.
   * @param comparator The Comparator defining the comparison logic.
   */
  private void evaluateComparison(BinaryExpression expr, Comparator comparator) {
    ExpressionEvaluator leftEval = new ExpressionEvaluator(tuple, schema);
    expr.getLeftExpression().accept(leftEval);
    int leftValue = leftEval.getValue();

    ExpressionEvaluator rightEval = new ExpressionEvaluator(tuple, schema);
    expr.getRightExpression().accept(rightEval);
    int rightValue = rightEval.getValue();

    result = comparator.compare(leftValue, rightValue);
  }
}
