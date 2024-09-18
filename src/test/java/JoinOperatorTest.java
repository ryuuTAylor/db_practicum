import common.DBCatalog;
import common.Tuple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.ScanOperator;
import operator.SelectOperator;
import operator.JoinOperator;

public class JoinOperatorTest {
    public static void main(String[] args) {
        // Set the data directory for DBCatalog (this points to the parent directory of
        // schema.txt and the data folder)
        DBCatalog.getInstance().setDataDirectory("src/test/taylor"); // Adjust this path accordingly

        // Define an empty output schema (modify this based on your table's schema)
        ArrayList<Column> outputSchema = new ArrayList<>();
        outputSchema.add(new Column("sid")); // Sailors.sid & Reservations.sid
        outputSchema.add(new Column("age")); // Sailors.age
        outputSchema.add(new Column("rating")); // Sailors.rating
        outputSchema.add(new Column("rid")); // Reservations.rid
        outputSchema.add(new Column("bid")); // Reservations.bid

        // Test
        String query = "SELECT Sailors.sid, Sailors.age, Sailors.rating, Reservations.rid, Reservations.bid FROM Sailors LEFT JOIN Reservations ON Sailors.sid = Reservations.sid";

        // Test using the ScanOperator with a manually specified file path
        System.out.println("Testing with manual file path:");
        ScanOperator joinOperatorManual1 = new ScanOperator(outputSchema, "Sailors", false,
                "src/test/taylor/data/Sailors");
        ScanOperator joinOperatorManual2 = new ScanOperator(outputSchema, "Reservations", false,
                "src/test/taylor/data/Reservations");

        testJoinOperator(joinOperatorManual1, joinOperatorManual2, query);
    }

    private static void testJoinOperator(ScanOperator leftChild, ScanOperator rightChild, String query) {
        try {
            // Parse the SQL query using JSQLParser; check this below!!
            Statement statement = CCJSqlParserUtil.parse(new StringReader(query));
            Select select = (Select) statement;
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            JoinOperator joinOperator = null;

            List<Join> joins = plainSelect.getJoins();
            if (joins != null && !joins.isEmpty()) {
                Join join = joins.get(0);
                Collection<Expression> joinConditions = join.getOnExpressions();
                for (Expression E : joinConditions) {
                    Expression joinCondition = E;
                    joinOperator = new JoinOperator(leftChild, rightChild, joinCondition);
                }
            }

            // Expected output from the join operation (example)
            List<String> expectedResults = Arrays.asList(
                    "1, 20, 5, 4, 30", // Example result
                    "2, 22, 7, NULL, NULL",
                    "3, 25, 3, 8, 8" // Example result
            );

            // Verify Join Operation
            int i = 0;
            Tuple tuple;
            while ((tuple = joinOperator.getNextTuple()) != null) {
                assertEquals(expectedResults.get(i), tuple.toString());
                i++;
            }
            assertNull(joinOperator.getNextTuple());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
