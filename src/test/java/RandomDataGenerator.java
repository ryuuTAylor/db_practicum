import common.Tuple;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class RandomDataGenerator {
    public static void main(String[] args) {

        // Creating a table with 5000 rows and 4 columns
        int numAttributes = 4;
        int numOfTuples = 5000;
        // int numOfTuples = 50;
        int intBytes = 4 * numAttributes; // attributes are going to be integers
        int capacity = intBytes * numOfTuples;

        // Creating a ByteBuffer to store the binary format of the tuples generated
        ByteBuffer buffer = ByteBuffer.allocate(capacity);

        // Start loop
        while (numOfTuples > 0) {
            ArrayList<Integer> attributes = new ArrayList<>();
            int randomNum;

            // Create each tuple with 4 attributes using an arrayList
            for (int y = 1; y <= numAttributes; y++) {
                randomNum = (int) (Math.random() * 101); // generating a random int from 0 to 100 inclusive
                attributes.add(randomNum);
            }

            // Create each tuple with 4 attributes using an arrayList
            Tuple generatedTuple = new Tuple(attributes);
            // System.out.println(generatedTuple.toString()); --> worked!

            for (int i = 0; i < numAttributes; i++) {
                buffer.putInt(generatedTuple.getElementAtIndex(i));
            }

            numOfTuples--;
        }
    }

}
