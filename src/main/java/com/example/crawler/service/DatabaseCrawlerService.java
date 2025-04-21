package com.example.crawler.service;

import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import com.example.crawler.model.*;

@Service
public class DatabaseCrawlerService {
    private final DataSource dataSource;

    public DatabaseCrawlerService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<TableInfo> crawlSchema(String schemaPattern) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData md = conn.getMetaData();
            List<TableInfo> tables = new ArrayList<>();
            try (ResultSet rs = md.getTables(null, schemaPattern, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tbl = rs.getString("TABLE_NAME");
                    TableInfo ti = new TableInfo();
                    ti.setName(tbl);
                    ti.setColumns(getColumns(md, tbl));
                    ti.setPrimaryKeys(getPrimaryKeys(md, tbl));
                    ti.setForeignKeys(getForeignKeys(md, tbl));
                    ti.setIndexes(getIndexes(md, tbl));
                    tables.add(ti);
                }
            }
            return tables;
        }
    }

    private List<ColumnInfo> getColumns(DatabaseMetaData md, String tableName) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        try (ResultSet rs = md.getColumns(null, null, tableName, "%")) {
            while (rs.next()) {
                ColumnInfo column = new ColumnInfo();
                column.setName(rs.getString("COLUMN_NAME"));
                column.setType(rs.getString("TYPE_NAME"));
                column.setSize(rs.getInt("COLUMN_SIZE"));
                column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                columns.add(column);
            }
        }
        return columns;
    }

    private List<String> getPrimaryKeys(DatabaseMetaData md, String tableName) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();
        try (ResultSet rs = md.getPrimaryKeys(null, null, tableName)) {
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        }
        return primaryKeys;
    }

    private List<ForeignKeyInfo> getForeignKeys(DatabaseMetaData md, String tableName) throws SQLException {
        List<ForeignKeyInfo> foreignKeys = new ArrayList<>();
        try (ResultSet rs = md.getImportedKeys(null, null, tableName)) {
            while (rs.next()) {
                ForeignKeyInfo fk = new ForeignKeyInfo();
                fk.setPkTable(rs.getString("PKTABLE_NAME"));
                fk.setPkColumn(rs.getString("PKCOLUMN_NAME"));
                fk.setFkTable(rs.getString("FKTABLE_NAME"));
                fk.setFkColumn(rs.getString("FKCOLUMN_NAME"));
                foreignKeys.add(fk);
            }
        }
        return foreignKeys;
    }

    private List<IndexInfo> getIndexes(DatabaseMetaData md, String tableName) throws SQLException {
        Map<String, IndexInfo> indexMap = new HashMap<>();
        try (ResultSet rs = md.getIndexInfo(null, null, tableName, false, false)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (indexName == null) continue;

                IndexInfo index;
                if (indexMap.containsKey(indexName)) {
                    index = indexMap.get(indexName);
                } else {
                    index = new IndexInfo();
                    index.setName(indexName);
                    index.setUnique(!rs.getBoolean("NON_UNIQUE"));
                    index.setColumns(new ArrayList<>());
                    indexMap.put(indexName, index);
                }

                index.getColumns().add(rs.getString("COLUMN_NAME"));
            }
        }
        return new ArrayList<>(indexMap.values());
    }
}