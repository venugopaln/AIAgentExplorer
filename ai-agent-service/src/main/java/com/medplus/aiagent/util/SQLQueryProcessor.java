package com.medplus.aiagent.util;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLQueryProcessor {

    private static final Set<String> SQL_KEYWORDS = new HashSet<>(Arrays.asList(
        "select", "from", "where", "insert", "into", "update", "set",
        "delete", "values", "create", "table", "drop", "alter",
        "join", "on", "as", "and", "or", "not", "null", "like", "in",
        "group", "by", "order", "having", "limit", "offset", "union",
        "distinct", "case", "when", "then", "else", "end"
    ));

    // Combines all the steps
    public static String cleanAndFormatAIQuery(String input) {
        if (input == null || input.trim().isEmpty()) return "";

        // STEP 1: Trim AI-generated clutter (markdown/code-blocks, helper texts, etc.)
        String query = stripAIExtras(input);

        // STEP 2: Remove SQL comments and trailing semicolon
        query = removeSQLComments(query);
        query = query.replaceAll(";\\s*$", "");

        // STEP 3: Collapse extra spaces
        query = query.replaceAll("\\s+", " ").trim();

        // STEP 4: Uppercase SQL keywords
        query = formatKeywordsToUpper(query);

        return query;
    }

    // Detect basic SQL injection keywords
    public static boolean detectSQLInjection(String query) {
        String lower = query.toLowerCase();
        return lower.contains(" or 1=1")
            || lower.contains("' or '1'='1")
            || lower.matches(".*(['\"]).*\\1.*--.*")
            || lower.contains("union select")
            || lower.contains("drop table");
    }

    private static String stripAIExtras(String input) {
        String query = input.trim();

        // Remove markdown code blocks
        query = query.replaceAll("```sql", "");
        query = query.replaceAll("(?i)```","");
        query = query.replaceAll("(?i)```", "");

        // Remove typical AI prefaces and suffixes
        query = query.replaceAll("(?mi)^\\s*(here is your query|try this|resulting query|this will return|hope this helps).*?$", "");

        return query.trim();
    }

    private static String removeSQLComments(String sql) {
        // Remove single-line comments (--) and (#)
        sql = sql.replaceAll("(?m)^\\s*(--|#).*?$", "");
        // Remove inline /* ... */ comments
        sql = sql.replaceAll("/\\*.*?\\*/", "");
        return sql;
    }

    private static String formatKeywordsToUpper(String query) {
        Pattern pattern = Pattern.compile("\\b(" + String.join("|", SQL_KEYWORDS) + ")\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    // Example usage
    public static void main(String[] args) {
        String aiInput = "```\n"
        		+ "SELECT *\n"
        		+ "FROM tbl_sale_header\n"
        		+ "JOIN tbl_sale_detail ON tbl_sale_header.InvoiceID = tbl_sale_detail.InvoiceID\n"
        		+ "JOIN tbl_product ON tbl_sale_detail.ProductID = tbl_product.ProductID\n"
        		+ "WHERE Name LIKE '%Dolo%' AND GrandTotal = (\n"
        		+ "  SELECT MAX(GrandTotal)\n"
        		+ "  FROM tbl_sale_header\n"
        		+ "  WHERE StoreID IN (\n"
        		+ "    SELECT StoreID\n"
        		+ "    FROM tbl_store\n"
        		+ "  )\n"
        		+ ");";

        String cleaned = cleanAndFormatAIQuery(aiInput);
        System.out.println("Cleaned and Formatted Query:");
        System.out.println(cleaned);

        System.out.println("Potential SQL Injection? " + detectSQLInjection(cleaned));
    }
}
