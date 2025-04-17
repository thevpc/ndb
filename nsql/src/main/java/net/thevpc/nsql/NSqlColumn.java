package net.thevpc.nsql;

import net.thevpc.nsql.model.NSqlColumnId;
import net.thevpc.nsql.model.NSqlTableId;
import net.thevpc.nsql.model.YesNo;
import net.thevpc.nsql.util.WithFullName;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NUplet;

import java.sql.PreparedStatement;
import java.util.Objects;

public class NSqlColumn implements Cloneable, WithFullName {
    public YesNo autoIncrement;
    public String fieldName;
    public String columnName;
    public Integer columnLength;
    public Integer scale;
    public Integer precision;
    public YesNo nullable;
    public Boolean id;
    public Boolean enabled;
    public NSqlColumnType columnType;
    public Integer index;
    public Integer insertIndex;
    private YesNo generatedColumn;
    private String label;
    private String javaClassName;
    private int sqlType;
    private String sqlTypeCode;
    private int displaySize;
    private int radix;
    private String columnDef;
    private String sqlTypeName;
    private String schemaName;
    private String catalogName;
    private String tableName;
    private int sourceDataType;
    private String sourceDataTypeCode;
    private int pkIndex;
    private boolean pk;
    private boolean fk;

    public YesNo getGeneratedColumn() {
        return generatedColumn;
    }

    public NSqlColumn setGeneratedColumn(YesNo generatedColumn) {
        this.generatedColumn = generatedColumn;
        return this;
    }

    public Integer getScale() {
        return scale;
    }

    public NSqlColumn setScale(Integer scale) {
        this.scale = scale;
        return this;
    }

    public Integer getPrecision() {
        return precision;
    }

    public NSqlColumn setPrecision(Integer precision) {
        this.precision = precision;
        return this;
    }

    public YesNo getNullable() {
        return nullable;
    }

    public NSqlColumn setNullable(YesNo nullable) {
        this.nullable = nullable;
        return this;
    }

    public Boolean getId() {
        return id;
    }

    public NSqlColumn setId(Boolean id) {
        this.id = id;
        return this;
    }

    public NSqlColumnType getColumnType() {
        return columnType;
    }

    public Integer getColumnLength() {
        return columnLength;
    }

    public NSqlColumn setColumnLength(Integer columnLength) {
        this.columnLength = columnLength;
        return this;
    }

    public NSqlColumn setColumnType(NSqlColumnType columnType) {
        this.columnType = columnType;
        return this;
    }

    public Integer getInsertIndex() {
        return insertIndex;
    }

    public NSqlColumn setInsertIndex(Integer insertIndex) {
        this.insertIndex = insertIndex;
        return this;
    }

    public YesNo getAutoIncrement() {
        return autoIncrement;
    }

    public NSqlColumn setAutoIncrement(YesNo autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }

    public String getFieldName() {
        return fieldName;
    }

    public NSqlColumn setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public String getColumnName() {
        return columnName;
    }

    public NSqlColumn setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public Integer getIndex() {
        return index;
    }

    public NSqlColumn setIndex(Integer index) {
        this.index = index;
        return this;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public NSqlColumn setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public void set(Object value, int index, PreparedStatement ps) {
        NPreparedStatementHelper.set(value, index, columnType, ps);
    }

    public static NSqlColumn of(NElement columnDefinition, NSqlColumn defaultDef) {
        String fieldName = defaultDef.getFieldName();
        String columnName = null;
        Integer columnLength = null;
        Integer scale = null;
        Integer precision = null;
        Boolean id = null;
        YesNo nullable = null;
        YesNo auto = null;
        NSqlColumnType columnType = null;
        Boolean enabled = null;
        if (columnDefinition != null) {
            enabled = NSqlTsonUtils.booleanOfField("enabled", columnDefinition, true);
            columnName = NSqlTsonUtils.stringOfField("name", columnDefinition, true);

            NElement type = NSqlTsonUtils.fieldByNameOf("type", columnDefinition);
            if (type.isPair() && type.asPair().get().key().isAnyString()) {
                columnType = NSqlColumnType.parse(type.asPair().get().key().asStringValue().get());
            } else if (type.isNamedUplet()) {
                NUpletElement f = type.asUplet().get();
                columnType = NSqlColumnType.parse(f.name());
                if (columnType != null) {
                    switch (columnType) {
                        case STRING: {
                            if (f.params() != null) {
                                for (NElement arg : f.params()) {
                                    if (arg.isNumber()) {
                                        if (columnLength == null) {
                                            columnLength = arg.asIntValue().get();
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        case BIGINT: {
                            if (f.params() != null) {
                                for (NElement arg : f.params()) {
                                    if (arg.isNumber()) {
                                        if (precision == null) {
                                            precision = arg.asIntValue().get();
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        case BIGDECIMAL: {
                            if (f.params() != null) {
                                for (NElement arg : f.params()) {
                                    if (arg.isNumber()) {
                                        if (precision == null) {
                                            precision = arg.asIntValue().get();
                                        } else if (scale == null) {
                                            scale = arg.asIntValue().get();
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            } else if (type.isNamedArray()) {
                columnType = NSqlColumnType.parse(type.toArray().get().name());
            } else if (type.isNamedObject()) {
                columnType = NSqlColumnType.parse(type.toObject().get().name());
            } else if (type.isAnyString()) {
                columnType = NSqlColumnType.parse(type.asStringValue().get());
            } else {
                throw new IllegalArgumentException("invalid type");
            }
            id = NSqlTsonUtils.booleanOfField("id", columnDefinition, false);
            nullable = NSqlTsonUtils.booleanOfField("nullable", columnDefinition, false) ? YesNo.YES : YesNo.NO;
            auto = NSqlTsonUtils.booleanOfField("auto", columnDefinition, false) ? YesNo.YES : YesNo.NO;
        }
        if (NBlankable.isBlank(columnName)) {
            columnName = defaultDef.getColumnName();
        }
        if (id == null) {
            id = defaultDef.getId();
        }
        if (auto == null) {
            auto = defaultDef.getAutoIncrement();
        }
        if (nullable == null) {
            nullable = defaultDef.getNullable();
        }
        if (columnLength == null) {
            columnLength = defaultDef.getColumnLength();
        }
        if (precision == null) {
            precision = defaultDef.getPrecision();
        }
        if (scale == null) {
            scale = defaultDef.getScale();
        }
        if (columnType == null) {
            columnType = defaultDef.getColumnType();
        }
        if (enabled == null) {
            enabled = defaultDef.getEnabled();
        }
        if (enabled == null) {
            enabled = true;
        }
        if (true) {
            if (id == null) {
                id = false;
            }
            if (nullable == null) {
                nullable = (!id) ? YesNo.YES : YesNo.NO;
            }
            if (auto == null) {
                auto = YesNo.NO;
            }
            if (columnType == NSqlColumnType.BIGINT) {
                if (scale == null) {
                    scale = 0;
                }
            }
            if (NBlankable.isBlank(columnName)) {
                columnName = NNameFormat.SNAKE_CASE.format(fieldName);
            }
        }

        return new NSqlColumn()
                .setFieldName(fieldName)
                .setColumnName(columnName)
                .setColumnType(columnType)
                .setColumnLength(columnLength)
                .setScale(scale)
                .setPrecision(precision)
                .setNullable(nullable)
                .setId(id)
                .setEnabled(enabled)
                .setAutoIncrement(auto)
                ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NSqlColumn sqlColumn = (NSqlColumn) o;
        return sqlType == sqlColumn.sqlType && displaySize == sqlColumn.displaySize && radix == sqlColumn.radix && sourceDataType == sqlColumn.sourceDataType && pkIndex == sqlColumn.pkIndex && pk == sqlColumn.pk && fk == sqlColumn.fk && autoIncrement == sqlColumn.autoIncrement && Objects.equals(fieldName, sqlColumn.fieldName) && Objects.equals(columnName, sqlColumn.columnName) && Objects.equals(columnLength, sqlColumn.columnLength) && Objects.equals(scale, sqlColumn.scale) && Objects.equals(precision, sqlColumn.precision) && Objects.equals(nullable, sqlColumn.nullable) && Objects.equals(id, sqlColumn.id) && Objects.equals(enabled, sqlColumn.enabled) && columnType == sqlColumn.columnType && Objects.equals(index, sqlColumn.index) && Objects.equals(insertIndex, sqlColumn.insertIndex) && generatedColumn == sqlColumn.generatedColumn && Objects.equals(label, sqlColumn.label) && Objects.equals(javaClassName, sqlColumn.javaClassName) && Objects.equals(sqlTypeCode, sqlColumn.sqlTypeCode) && Objects.equals(columnDef, sqlColumn.columnDef) && Objects.equals(sqlTypeName, sqlColumn.sqlTypeName) && Objects.equals(schemaName, sqlColumn.schemaName) && Objects.equals(catalogName, sqlColumn.catalogName) && Objects.equals(tableName, sqlColumn.tableName) && Objects.equals(sourceDataTypeCode, sqlColumn.sourceDataTypeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(autoIncrement, fieldName, columnName, columnLength, scale, precision, nullable, id, enabled, columnType, index, insertIndex, generatedColumn, label, javaClassName, sqlType, sqlTypeCode, displaySize, radix, columnDef, sqlTypeName, schemaName, catalogName, tableName, sourceDataType, sourceDataTypeCode, pkIndex, pk, fk);
    }

    public String resolveFieldOrColumnName() {
        if (fieldName != null) {
            return fieldName;
        }
        return columnName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SqlColumn(" + resolveFieldOrColumnName() + "){");
        sb.append("columnName=" + columnName);
        sb.append(", columnType=" + columnType + (columnLength == null ? "" : ("(" + columnLength + ")")));
        if (id != null) {
            if (id) {
                sb.append(", id");
            } else {
                sb.append(", !id");
            }
        }
        if (autoIncrement != null) {
            if (autoIncrement == YesNo.YES) {
                sb.append(", auto");
            } else if (autoIncrement == YesNo.NO) {
                sb.append(", auto");
            } else {
                sb.append(", !auto");
            }
        }
        if (enabled != null) {
            if (enabled) {
                sb.append(", enabled");
            } else {
                sb.append(", !enabled");
            }
        }
        if (index != null) {
            sb.append(", index=" + index);
        }
        if (insertIndex != null) {
            sb.append(", insertIndex=" + insertIndex);
        }
        sb.append("}");
        return sb.toString();
    }

    public String getLabel() {
        return label;
    }

    public NSqlColumn setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public NSqlColumn setJavaClassName(String javaClassName) {
        this.javaClassName = javaClassName;
        return this;
    }

    public int getSqlType() {
        return sqlType;
    }

    public NSqlColumn setSqlType(int sqlType) {
        this.sqlType = sqlType;
        return this;
    }

    public String getSqlTypeCode() {
        return sqlTypeCode;
    }

    public NSqlColumn setSqlTypeCode(String sqlTypeCode) {
        this.sqlTypeCode = sqlTypeCode;
        return this;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    public NSqlColumn setDisplaySize(int displaySize) {
        this.displaySize = displaySize;
        return this;
    }

    public int getRadix() {
        return radix;
    }

    public NSqlColumn setRadix(int radix) {
        this.radix = radix;
        return this;
    }

    public String getColumnDef() {
        return columnDef;
    }

    public NSqlColumn setColumnDef(String columnDef) {
        this.columnDef = columnDef;
        return this;
    }

    public String getSqlTypeName() {
        return sqlTypeName;
    }

    public NSqlColumn setSqlTypeName(String sqlTypeName) {
        this.sqlTypeName = sqlTypeName;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public NSqlColumn setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public NSqlColumn setCatalogName(String catalogName) {
        this.catalogName = catalogName;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public NSqlColumn setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public int getSourceDataType() {
        return sourceDataType;
    }

    public NSqlColumn setSourceDataType(int sourceDataType) {
        this.sourceDataType = sourceDataType;
        return this;
    }

    public String getSourceDataTypeCode() {
        return sourceDataTypeCode;
    }

    public NSqlColumn setSourceDataTypeCode(String sourceDataTypeCode) {
        this.sourceDataTypeCode = sourceDataTypeCode;
        return this;
    }

    public int getPkIndex() {
        return pkIndex;
    }

    public NSqlColumn setPkIndex(int pkIndex) {
        this.pkIndex = pkIndex;
        return this;
    }

    public boolean isPk() {
        return pk;
    }

    public NSqlColumn setPk(boolean pk) {
        this.pk = pk;
        return this;
    }

    public boolean isFk() {
        return fk;
    }

    public NSqlColumn setFk(boolean fk) {
        this.fk = fk;
        return this;
    }

    public NSqlColumn copy() {
        return clone();
    }

    @Override
    protected NSqlColumn clone() {
        try {
            return (NSqlColumn) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public NSqlColumn setTableId(NSqlTableId tableId) {
        setCatalogName(tableId.getCatalogName());
        setSchemaName(tableId.getSchemaName());
        setTableName(tableId.getTableName());
        return this;
    }

    public NSqlTableId toTableId() {
        return new NSqlTableId(catalogName, schemaName, tableName);
    }

    public NSqlColumnId getColumnId() {
        return new NSqlColumnId(
                catalogName, schemaName, tableName, columnName
        );
    }

    @Override
    public String getFullName() {
        return getColumnId().getFullName();
    }
}
