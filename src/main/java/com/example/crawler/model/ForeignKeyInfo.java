package com.example.crawler.model;

public class ForeignKeyInfo {
    private String pkTable;
    private String pkColumn;
    private String fkTable;
    private String fkColumn;
    
    public String getPkTable() {
        return pkTable;
    }

    public void setPkTable(String pkTable) {
        this.pkTable = pkTable;
    }

    public String getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(String pkColumn) {
        this.pkColumn = pkColumn;
    }

    public String getFkTable() {
        return fkTable;
    }

    public void setFkTable(String fkTable) {
        this.fkTable = fkTable;
    }

    public String getFkColumn() {
        return fkColumn;
    }

    public void setFkColumn(String fkColumn) {
        this.fkColumn = fkColumn;
    }
}