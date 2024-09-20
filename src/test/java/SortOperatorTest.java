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
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.ScanOperator;
import operator.SelectOperator;
import operator.JoinOperator;
import operator.ProjectOperator;
import operator.SortOperator;

public class SortOperatorTest {
    public static void main(String[] args) {
        // Set the data directory for DBCatalog (this points to the parent directory of
        // schema.txt and the data folder)
        DBCatalog.getInstance().setDataDirectory("src/test/taylor"); // Adjust this path accordingly

        // Define an empty output schema (modify this based on your table's schema)
        ArrayList<Column> outputSchema = new ArrayList<>();
        outputSchema.add(new Column("sid")); // Sailors.sid & Reservations.sid
        outputSchema.add(new Column("age")); // Sailors.age
        outputSchema.add(new Column("rating"));

        // Test
        String query = "SELECT* FROM Sailors ORDER BY rating ASC";

        // Test using the ScanOperator with a manually specified file path
        System.out.println("Testing with manual file path:");
        ScanOperator projectOperatorManual = new ScanOperator(outputSchema, "Sailors", false,
                "src/test/taylor/data/Sailors");
        testSortOperator(projectOperatorManual, query);

    }

    private static void testSortOperator(ScanOperator child, String query) {
        try {
            // Parse the SQL query using JSQLParser; check this below!!
            Statement statement = CCJSqlParserUtil.parse(new StringReader(query));
            Select select = (Select) statement;
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
            SortOperator sortOperator = new SortOperator(child, orderByElements);

            // Expected output from the project operation (example)
            ArrayList<String> expectedResults = new ArrayList<>();
            expectedResults.add("3,25,3");
            expectedResults.add("1,20,5");
            expectedResults.add("2,22,7");

            // Verify Project Operation
            int i = 0;
            Tuple tuple;
            while ((tuple = sortOperator.getNextTuple()) != null) {
                assertEquals(expectedResults.get(i), tuple.toString());
                i++;
            }
            assertNull(sortOperator.getNextTuple());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
