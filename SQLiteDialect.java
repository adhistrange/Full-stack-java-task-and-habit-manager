package com.example.taskhabitmanager.config;

import java.sql.Types;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;

public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();
        registerColumnType(Types.BIT, "boolean");
        registerColumnType(Types.INTEGER, "integer");
        registerColumnType(Types.VARCHAR, "varchar");
        registerColumnType(Types.BIGINT, "bigint");
        registerColumnType(Types.DOUBLE, "double");
        registerColumnType(Types.FLOAT, "float");
        registerColumnType(Types.DECIMAL, "decimal");
        registerColumnType(Types.BLOB, "blob");
        registerColumnType(Types.CLOB, "clob");
        // Map DATE/TIMESTAMP to text ISO strings if needed
        registerColumnType(Types.DATE, "date");
        registerColumnType(Types.TIMESTAMP, "datetime");
    }

    @Override
    public boolean supportsIdentityColumns() {
        return true;
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new IdentityColumnSupportImpl() {
            @Override
            public String getIdentitySelectString(String table, String column, int type) {
                return "select last_insert_rowid()";
            }

            @Override
            public boolean supportsIdentityColumns() {
                return true;
            }

            @Override
            public String getIdentityColumnString(int type) {
                // "integer" primary key autoincrement
                return "integer";
            }
        };
    }

    @Override
    public boolean hasAlterTable() {
        return false;
    }

    @Override
    public boolean supportsTemporaryTables() {
        return false;
    }

    @Override
    public String getAddColumnString() {
        return "add column";
    }

    @Override
    public boolean supportsCurrentTimestampSelection() {
        return true;
    }

    @Override
    public boolean isCurrentTimestampSelectStringCallable() {
        return false;
    }

    @Override
    public String getCurrentTimestampSelectString() {
        return "select current_timestamp";
    }

    @Override
    public boolean supportsUnionAll() {
        return true;
    }

    @Override
    public NameQualifierSupport getNameQualifierSupport() {
        return NameQualifierSupport.NONE;
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    // other methods keep reasonable defaults
}
