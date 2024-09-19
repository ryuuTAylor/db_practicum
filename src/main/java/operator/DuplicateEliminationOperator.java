package operator;

import java.util.ArrayList;

import common.Tuple;
import net.sf.jsqlparser.schema.Column;

public class DuplicateEliminationOperator extends Operator {

  public DuplicateEliminationOperator(ArrayList<Column> outputSchema) {
    super(outputSchema);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void reset() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'reset'");
  }

  @Override
  public Tuple getNextTuple() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getNextTuple'");
  }

}
