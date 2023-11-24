/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.model;

import java.util.Objects;

/**
 * @author vpc
 */
public class StoreColumnDefinition implements Cloneable {
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

    public StoreDataType getStoreType() {
        return storeType;
    }

    public StoreColumnDefinition setStoreType(StoreDataType storeType) {
        this.storeType = storeType;
        return this;
    }


    public int getIndex() {
        return index;
    }

    public StoreColumnDefinition setIndex(int index) {
        this.index = index;
        return this;
    }

    public String getColumnName() {
        return columnName;
    }

    public StoreColumnDefinition setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public int getSqlType() {
        return sqlType;
    }

    public StoreColumnDefinition setSqlType(int sqlType) {
        this.sqlType = sqlType;
        return this;
    }

    public String getSqlTypeName() {
        return sqlTypeName;
    }

    public StoreColumnDefinition setSqlTypeName(String sqlTypeName) {
        this.sqlTypeName = sqlTypeName;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public StoreColumnDefinition setLabel(String label) {
        this.label = label;
        return this;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    public StoreColumnDefinition setDisplaySize(int displaySize) {
        this.displaySize = displaySize;
        return this;
    }

    public int getPrecision() {
        return precision;
    }

    public StoreColumnDefinition setPrecision(int precision) {
        this.precision = precision;
        return this;
    }

    public int getScale() {
        return scale;
    }

    public StoreColumnDefinition setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public StoreColumnDefinition setJavaClassName(String javaClassName) {
        this.javaClassName = javaClassName;
        return this;
    }

    public int getRadix() {
        return radix;
    }

    public StoreColumnDefinition setRadix(int radix) {
        this.radix = radix;
        return this;
    }

    public YesNo getNullable() {
        return nullable;
    }

    public StoreColumnDefinition setNullable(YesNo nullable) {
        this.nullable = nullable;
        return this;
    }

    public String getColumnDef() {
        return columnDef;
    }

    public StoreColumnDefinition setColumnDef(String columnDef) {
        this.columnDef = columnDef;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public StoreColumnDefinition setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public StoreColumnDefinition setCatalogName(String catalogName) {
        this.catalogName = catalogName;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public StoreColumnDefinition setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public int getSourceDataType() {
        return sourceDataType;
    }

    public StoreColumnDefinition setSourceDataType(int sourceDataType) {
        this.sourceDataType = sourceDataType;
        return this;
    }

    public YesNo getAutoIncrement() {
        return autoIncrement;
    }

    public StoreColumnDefinition setAutoIncrement(YesNo autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }

    public YesNo getGeneratedColumns() {
        return generatedColumns;
    }

    public StoreColumnDefinition setGeneratedColumns(YesNo generatedColumns) {
        this.generatedColumns = generatedColumns;
        return this;
    }


    public int getPkIndex() {
        return pkIndex;
    }

    public StoreColumnDefinition setPkIndex(int pkIndex) {
        this.pkIndex = pkIndex;
        return this;
    }

    public boolean isPk() {
        return pk;
    }

    public StoreColumnDefinition setPk(boolean pk) {
        this.pk = pk;
        return this;
    }

    public boolean isFk() {
        return fk;
    }

    public StoreColumnDefinition setFk(boolean fk) {
        this.fk = fk;
        return this;
    }

    public TableId toTableId() {
        return new TableId(catalogName, schemaName, tableName);
    }

    public String getSqlTypeCode() {
        return sqlTypeCode;
    }

    public StoreColumnDefinition setSqlTypeCode(String sqlTypeCode) {
        this.sqlTypeCode = sqlTypeCode;
        return this;
    }

    public String getSourceDataTypeCode() {
        return sourceDataTypeCode;
    }

    public StoreColumnDefinition setSourceDataTypeCode(String sourceDataTypeCode) {
        this.sourceDataTypeCode = sourceDataTypeCode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreColumnDefinition that = (StoreColumnDefinition) o;
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

    public StoreColumnDefinition copy() {
        return clone();
    }

    @Override
    protected StoreColumnDefinition clone() {
        try {
            return (StoreColumnDefinition) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
