# INSTRUCTIONS.md

## Top-Level Class

The top-level class of the code is **`compiler.Compiler`**, located in the `src/main/java/compiler/Compiler.java` file. This class is responsible for reading SQL queries from the input, constructing a query plan, and producing the output results.

To run the program, use the following command:

```bash
java -jar db_practicum_team_name-deliverable.jar <inputdir> <outputdir>
```

- `<inputdir>`: Directory containing the `db` folder and SQL queries.
- `<outputdir>`: Directory where the output results will be written.

---

## Logic for Extracting Join Conditions from the WHERE Clause

The logic for extracting join conditions from the `WHERE` clause is located in the **`QueryPlanBuilder.java`** class, specifically within the `applyJoins()` method.

### Steps:
1. The method scans the `WHERE` clause and identifies conditions that involve two or more tables. These conditions are classified as join conditions.
2. The **`WhereExpressionVisitor`** class (located in `src/main/java/common/WhereExpressionVisitor.java`) is used to traverse and analyze the expressions. It separates join conditions from selection conditions.

### Code Location:
- **`applyJoins()` Method**: This method is part of the **`QueryPlanBuilder.java`** class, which is responsible for assembling the query plan and identifying join conditions.
- **Detailed Comments**: Full explanation of this logic can be found in the comments within the `WhereExpressionVisitor.java` file.

---

## Other Information

- **Known Bugs**:
  - There are no critical known bugs at this time, but the system has not been tested with very large datasets, which may affect performance.

- **Assumptions**:
  - The program assumes that all SQL queries are valid and well-formed.
  - Advanced SQL features like subqueries, aggregation functions, `GROUP BY`, or `HAVING` are not supported.
  - Only basic `SELECT-FROM-WHERE` queries, along with `JOIN`, `ORDER BY`, and `DISTINCT`, are supported.

- **Additional Notes**:
  - Test cases for all operators (such as `ScanOperator`, `JoinOperator`, `SelectOperator`, etc.) are located in the `src/test/java/` directory.
  - Ensure that the `inputdir/db/` directory contains both `schema.txt` and the relevant table files in the correct format for successful execution.

---

This content is ready to be copied directly into your `INSTRUCTIONS.md` file.


