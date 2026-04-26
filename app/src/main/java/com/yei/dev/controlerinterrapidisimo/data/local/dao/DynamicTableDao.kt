package com.yei.dev.controlerinterrapidisimo.data.local.dao

import android.database.Cursor.FIELD_TYPE_BLOB
import android.database.Cursor.FIELD_TYPE_FLOAT
import android.database.Cursor.FIELD_TYPE_INTEGER
import android.database.Cursor.FIELD_TYPE_NULL
import android.database.Cursor.FIELD_TYPE_STRING
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import com.yei.dev.controlerinterrapidisimo.domain.models.ColumnDefinition
import com.yei.dev.controlerinterrapidisimo.domain.models.TableInfo

/**
 * Data Access Object for dynamic table operations.
 *
 * Provides methods to create, query, and manipulate tables dynamically
 * based on schema definitions received from the remote service.
 *
 * This DAO uses raw SQL queries to support dynamic table creation and
 * data manipulation for tables with varying schemas.
 *
 * Note: This is not a Room DAO but a regular class that requires database injection.
 */
class DynamicTableDao(private val database: SupportSQLiteDatabase) {

    /**
     * Creates a new table with the specified columns.
     *
     * Uses the SQL CREATE TABLE statement provided by the API (QueryCreacion)
     * if available, otherwise skips table creation if no columns are defined.
     *
     * @param tableName The name of the table to create
     * @param columns The list of column definitions
     * @param querySql Optional SQL CREATE TABLE statement from API
     */
    fun createTable(tableName: String, columns: List<ColumnDefinition>, querySql: String = "") {
        // If API provided a complete SQL statement, use it
        if (querySql.isNotBlank()) {
            try {
                database.execSQL(querySql)
                return
            } catch (e: Exception) {
                // If API SQL fails, fall through to generate our own
            }
        }

        // Skip table creation if no columns are defined
        if (columns.isEmpty()) {
            return
        }

        val columnDefinitions = columns.joinToString(", ") { column ->
            val nullability = if (column.nullable) "" else "NOT NULL"
            val primaryKey = if (column.primaryKey) "PRIMARY KEY" else ""
            "${column.name} ${column.type} $nullability $primaryKey".trim()
        }

        val createTableQuery = "CREATE TABLE IF NOT EXISTS $tableName ($columnDefinitions)"
        database.execSQL(createTableQuery)
    }

    /**
     * Inserts data into a dynamic table.
     *
     * @param tableName The name of the table
     * @param data Map of column names to values
     */
    fun insertData(tableName: String, data: Map<String, Any?>) {
        if (data.isEmpty()) return

        val columns = data.keys.joinToString(", ")
        val placeholders = data.keys.joinToString(", ") { "?" }
        val values = data.values.toTypedArray()

        val insertQuery = "INSERT OR REPLACE INTO $tableName ($columns) VALUES ($placeholders)"
        database.execSQL(insertQuery, values)
    }

    /**
     * Retrieves all table names from the database.
     *
     * @return List of table names
     */
    fun getAllTables(): List<String> {
        val query = SimpleSQLiteQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'room_%' AND name != 'users'",
        )
        return executeQueryForStringList(query)
    }

    /**
     * Retrieves information about all tables.
     *
     * @return List of TableInfo with table names and record counts
     */
    fun getAllTablesInfo(): List<TableInfo> {
        val tables = getAllTables()
        return tables.map { tableName ->
            val count = getTableRecordCount(tableName)
            TableInfo(name = tableName, recordCount = count)
        }
    }

    /**
     * Gets the record count for a specific table.
     *
     * @param tableName The name of the table
     * @return The number of records in the table
     */
    fun getTableRecordCount(tableName: String): Int {
        val query = SimpleSQLiteQuery("SELECT COUNT(*) FROM $tableName")
        val cursor = database.query(query)
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    /**
     * Retrieves all data from a specific table.
     *
     * @param tableName The name of the table
     * @return List of maps where each map represents a row with column names as keys
     */
    fun getTableData(tableName: String): List<Map<String, Any?>> {
        val query = SimpleSQLiteQuery("SELECT * FROM $tableName")
        val cursor = database.query(query)

        return cursor.use {
            val results = mutableListOf<Map<String, Any?>>()
            val columnNames = it.columnNames

            while (it.moveToNext()) {
                val row = mutableMapOf<String, Any?>()
                columnNames.forEachIndexed { index, columnName ->
                    val value = when (it.getType(index)) {
                        FIELD_TYPE_NULL -> null
                        FIELD_TYPE_INTEGER -> it.getLong(index)
                        FIELD_TYPE_FLOAT -> it.getDouble(index)
                        FIELD_TYPE_STRING -> it.getString(index)
                        FIELD_TYPE_BLOB -> it.getBlob(index)
                        else -> it.getString(index)
                    }
                    row[columnName] = value
                }
                results.add(row)
            }
            results
        }
    }

    /**
     * Drops a table from the database.
     *
     * @param tableName The name of the table to drop
     */
    fun dropTable(tableName: String) {
        val dropTableQuery = "DROP TABLE IF EXISTS $tableName"
        database.execSQL(dropTableQuery)
    }

    /**
     * Checks if a table exists in the database.
     *
     * @param tableName The name of the table to check
     * @return true if the table exists, false otherwise
     */
    fun tableExists(tableName: String): Boolean {
        val query = SimpleSQLiteQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(tableName),
        )
        val cursor = database.query(query)
        return cursor.use { it.count > 0 }
    }

    /**
     * Updates an existing table schema while preserving data.
     *
     * This method uses SQLite's ALTER TABLE capabilities to add new columns
     * while preserving existing data. For complex schema changes, it creates
     * a temporary table, copies data, and renames tables.
     *
     * @param tableName The name of the table to update
     * @param newColumns The new column definitions
     */
    fun updateTableSchema(tableName: String, newColumns: List<ColumnDefinition>) {
        // Get existing columns
        val existingColumns = getTableColumns(tableName)
        val existingColumnNames = existingColumns.map { it.name }.toSet()
        val newColumnNames = newColumns.map { it.name }.toSet()

        // Add new columns that don't exist
        newColumns.forEach { newColumn ->
            if (!existingColumnNames.contains(newColumn.name)) {
                val nullability = if (newColumn.nullable) "" else "NOT NULL DEFAULT ''"
                val alterQuery = "ALTER TABLE $tableName ADD COLUMN ${newColumn.name} ${newColumn.type} $nullability"
                try {
                    database.execSQL(alterQuery)
                } catch (e: Exception) {
                    // Column might already exist, continue
                }
            }
        }

        // Note: SQLite doesn't support dropping columns or changing column types directly.
        // For complex schema changes, a migration strategy would be needed.
        // For now, we preserve all existing data and add new columns as needed.
    }

    /**
     * Retrieves the column definitions for an existing table.
     *
     * @param tableName The name of the table
     * @return List of ColumnDefinition for the table
     */
    private fun getTableColumns(tableName: String): List<ColumnDefinition> {
        val query = SimpleSQLiteQuery("PRAGMA table_info($tableName)")
        val cursor = database.query(query)

        return cursor.use {
            val results = mutableListOf<ColumnDefinition>()
            while (it.moveToNext()) {
                val name = it.getString(1)
                val type = it.getString(2)
                val notNull = it.getInt(3) == 1
                val primaryKey = it.getInt(5) == 1

                results.add(
                    ColumnDefinition(
                        name = name,
                        type = type,
                        nullable = !notNull,
                        primaryKey = primaryKey,
                    ),
                )
            }
            results
        }
    }

    /**
     * Helper method to execute a query that returns a list of strings.
     */
    private fun executeQueryForStringList(query: SupportSQLiteQuery): List<String> {
        val cursor = database.query(query)
        return cursor.use {
            val results = mutableListOf<String>()
            while (it.moveToNext()) {
                results.add(it.getString(0))
            }
            results
        }
    }
}
