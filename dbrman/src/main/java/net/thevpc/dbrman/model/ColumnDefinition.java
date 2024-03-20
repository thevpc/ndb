/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbrman.model;

import net.thevpc.dbrman.util.DbInfoModuleInstaller;
import net.thevpc.vio2.model.StoreDataType;
import net.thevpc.vio2.model.StoreFieldDefinition;
import net.thevpc.vio2.model.StoreStructId;
import net.thevpc.vio2.model.YesNo;

import java.util.Objects;

/**
 * @author vpc
 */
public class ColumnDefinition implements StoreFieldDefinition, Cloneable {
    static {
        DbInfoModuleInstaller.init();
    }

    private int index;
    private String columnName;
    private String label;
    private String javaClassName;
    private int precision;
    private int scale;
    private int sqlType;
    private String sqlTypeCode;
    private int displaySize;
    private int radix;
    private YesNo nullable;
    private String columnDef;
    private String sqlTypeName;
    private String schemaName;
    private String catalogName;
    private String tableName;
    private int sourceDataType;
    private String sourceDataTypeCode;
    private YesNo autoIncrement;
    private YesNo generatedColumns;
    private StoreDataType storeType;
    private int pkIndex;
    private boolean pk;
    private boolean fk;

    @Override
    public StoreDataType getStoreType() {
        return storeType;
    }

    public StoreFieldDefinition setStoreType(StoreDataType storeType) {
        this.storeType = storeType;
        return this;
    }


    public int getIndex() {
        return index;
    }

    public StoreFieldDefinition setIndex(int index) {
        this.index = index;
        return this;
    }

    public String getColumnName() {
        return columnName;
    }

    public StoreFieldDefinition setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public int getSqlType() {
        return sqlType;
    }

    public ColumnDefinition setSqlType(int sqlType) {
        this.sqlType = sqlType;
        return this;
    }

    public String getSqlTypeName() {
        return sqlTypeName;
    }

    public ColumnDefinition setSqlTypeName(String sqlTypeName) {
        this.sqlTypeName = sqlTypeName;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public ColumnDefinition setLabel(String label) {
        this.label = label;
        return this;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    public ColumnDefinition setDisplaySize(int displaySize) {
        this.displaySize = displaySize;
        return this;
    }

    public int getPrecision() {
        return precision;
    }

    public ColumnDefinition setPrecision(int precision) {
        this.precision = precision;
        return this;
    }

    public int getScale() {
        return scale;
    }

    public ColumnDefinition setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public ColumnDefinition setJavaClassName(String javaClassName) {
        this.javaClassName = javaClassName;
        return this;
    }

    public int getRadix() {
        return radix;
    }

    public ColumnDefinition setRadix(int radix) {
        this.radix = radix;
        return this;
    }

    public YesNo getNullable() {
        return nullable;
    }

    public ColumnDefinition setNullable(YesNo nullable) {
        this.nullable = nullable;
        return this;
    }

    public String getColumnDef() {
        return columnDef;
    }

    public ColumnDefinition setColumnDef(String columnDef) {
        this.columnDef = columnDef;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public ColumnDefinition setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public ColumnDefinition setCatalogName(String catalogName) {
        this.catalogName = catalogName;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public ColumnDefinition setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public int getSourceDataType() {
        return sourceDataType;
    }

    public ColumnDefinition setSourceDataType(int sourceDataType) {
        this.sourceDataType = sourceDataType;
        return this;
    }

    public YesNo getAutoIncrement() {
        return autoIncrement;
    }

    public ColumnDefinition setAutoIncrement(YesNo autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }

    public YesNo getGeneratedColumns() {
        return generatedColumns;
    }

    public ColumnDefinition setGeneratedColumns(YesNo generatedColumns) {
        this.generatedColumns = generatedColumns;
        return this;
    }


    public int getPkIndex() {
        return pkIndex;
    }

    public ColumnDefinition setPkIndex(int pkIndex) {
        this.pkIndex = pkIndex;
        return this;
    }

    public boolean isPk() {
        return pk;
    }

    public ColumnDefinition setPk(boolean pk) {
        this.pk = pk;
        return this;
    }

    public boolean isFk() {
        return fk;
    }

    public ColumnDefinition setFk(boolean fk) {
        this.fk = fk;
        return this;
    }

    public StoreStructId toTableId() {
        return new TableId(catalogName, schemaName, tableName);
    }

    public String getSqlTypeCode() {
        return sqlTypeCode;
    }

    public ColumnDefinition setSqlTypeCode(String sqlTypeCode) {
        this.sqlTypeCode = sqlTypeCode;
        return this;
    }

    public String getSourceDataTypeCode() {
        return sourceDataTypeCode;
    }

    public ColumnId toFieldId() {
        return getColumnId();
    }

    public ColumnId getColumnId() {
        return new ColumnId(
                catalogName, schemaName, tableName, columnName
        );
    }

    public ColumnDefinition setSourceDataTypeCode(String sourceDataTypeCode) {
        this.sourceDataTypeCode = sourceDataTypeCode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnDefinition that = (ColumnDefinition) o;
        return index == that.index && precision == that.precision && scale == that.scale && sqlType == that.sqlType && displaySize == that.displaySize && radix == that.radix && sourceDataType == that.sourceDataType && pkIndex == that.pkIndex && pk == that.pk && fk == that.fk && Objects.equals(columnName, that.columnName) && Objects.equals(label, that.label) && Objects.equals(javaClassName, that.javaClassName) && Objects.equals(sqlTypeCode, that.sqlTypeCode) && nullable == that.nullable && Objects.equals(columnDef, that.columnDef) && Objects.equals(sqlTypeName, that.sqlTypeName) && Objects.equals(schemaName, that.schemaName) && Objects.equals(catalogName, that.catalogName) && Objects.equals(tableName, that.tableName) && Objects.equals(sourceDataTypeCode, that.sourceDataTypeCode) && autoIncrement == that.autoIncrement && generatedColumns == that.generatedColumns && storeType == that.storeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, columnName, label, javaClassName, precision, scale, sqlType, sqlTypeCode, displaySize, radix, nullable, columnDef, sqlTypeName, schemaName, catalogName, tableName, sourceDataType, sourceDataTypeCode, autoIncrement, generatedColumns, storeType, pkIndex, pk, fk);
    }

    @Override
    public String toString() {
        return "StoreColumnDefinition{" +
                "index=" + index +
                ", columnName='" + columnName + '\'' +
                ", label='" + label + '\'' +
                ", javaClassName='" + javaClassName + '\'' +
                ", precision=" + precision +
                ", scale=" + scale +
                ", sqlType=" + sqlType +
                ", sqlTypeCode='" + sqlTypeCode + '\'' +
                ", displaySize=" + displaySize +
                ", radix=" + radix +
                ", nullable=" + nullable +
                ", columnDef='" + columnDef + '\'' +
                ", sqlTypeName='" + sqlTypeName + '\'' +
                ", schemaName='" + schemaName + '\'' +
                ", catalogName='" + catalogName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", sourceDataType=" + sourceDataType +
                ", sourceDataTypeCode='" + sourceDataTypeCode + '\'' +
                ", autoIncrement=" + autoIncrement +
                ", generatedColumns=" + generatedColumns +
                ", storeType=" + storeType +
                ", pkIndex=" + pkIndex +
                ", pk=" + pk +
                ", fk=" + fk +
                '}';
    }

    @Override
    public String getFieldName() {
        return columnName;
    }

    public String getFullName() {
        return toFieldId().getFullName();
    }

    public ColumnDefinition copy() {
        return clone();
    }

    @Override
    protected ColumnDefinition clone() {
        try {
            return (ColumnDefinition) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public ColumnDefinition setTableId(TableId tableId) {
        setCatalogName(tableId.getCatalogName());
        setSchemaName(tableId.getSchemaName());
        setTableName(tableId.getTableName());
        return this;
    }
}
