package operator;

import common.Tuple;
import java.io.PrintStream;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

/** Abstract class to represent relational operators. */
public abstract class Operator {

  protected ArrayList<Column> outputSchema;

  public Operator(ArrayList<Column> outputSchema) {
    this.outputSchema = outputSchema;
  }

  public ArrayList<Column> getOutputSchema() {
    return outputSchema;
  }

  /** Resets cursor on the operator to the beginning */
  public abstract void reset();

  /**
   * Get next tuple from operator
   *
   * @return next Tuple, or null if we are at the end
   */
  public abstract Tuple getNextTuple();

  /**
   * Iterate through output of operator and send it all to the specified printStream)
   *
   * @param printStream stream to receive output, one tuple per line.
   */
  public void dump(PrintStream printStream) {
    Tuple t;
    while ((t = this.getNextTuple()) != null) {
      printStream.println(t);
    }
  }
}
