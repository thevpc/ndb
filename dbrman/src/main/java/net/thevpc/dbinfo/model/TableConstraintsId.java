/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbinfo.model;

import net.thevpc.dbinfo.util.DbInfoModuleInstaller;
import net.thevpc.vio2.model.StoreFieldId;
import net.thevpc.vio2.util.StringUtils;

/**
 * @author vpc
 */
public class TableConstraintsId implements StoreFieldId {
    static {
        DbInfoModuleInstaller.init();
    }

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String constraintsName;
    private TableConstraintsType constraintsType;

    public TableConstraintsId(String catalogName, String schemaName, String tableName, String constraintsName, TableConstraintsType constraintsType) {
        this.schemaName = schemaName;
        this.catalogName = catalogName;
        this.tableName = tableName;
        this.constraintsName = constraintsName;
        this.constraintsType = constraintsType;
    }

    public TableConstraintsType getConstraintsType() {
        return constraintsType;
    }

    public String getConstraintsName() {
        return constraintsName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getTableName() {
        return tableName;
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.schemaName != null ? this.schemaName.hashCode() : 0);
        hash = 67 * hash + (this.catalogName != null ? this.catalogName.hashCode() : 0);
        hash = 67 * hash + (this.tableName != null ? this.tableName.hashCode() : 0);
        hash = 67 * hash + (this.constraintsName != null ? this.constraintsName.hashCode() : 0);
        hash = 67 * hash + (this.constraintsType != null ? this.constraintsType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TableConstraintsId other = (TableConstraintsId) obj;
        if ((this.schemaName == null) ? (other.schemaName != null) : !this.schemaName.equals(other.schemaName)) {
            return false;
        }
        if ((this.catalogName == null) ? (other.catalogName != null) : !this.catalogName.equals(other.catalogName)) {
            return false;
        }
        if ((this.tableName == null) ? (other.tableName != null) : !this.tableName.equals(other.tableName)) {
            return false;
        }
        if ((this.catalogName == null) ? (other.catalogName != null) : !this.catalogName.equals(other.catalogName)) {
            return false;
        }
        if ((this.constraintsType == null) ? (other.constraintsType != null) : !this.constraintsType.equals(other.constraintsType)) {
            return false;
        }
        return true;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if (!StringUtils.isBlank(catalogName)) {
            if (first) {
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(catalogName);
        }
        if (!StringUtils.isBlank(schemaName)) {
            if (first) {
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(schemaName);
        }
        if (!StringUtils.isBlank(tableName)) {
            if (first) {
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(tableName);
        }
        if (!StringUtils.isBlank(constraintsName)) {
            if (first) {
                first = false;
            } else {
                sb.append("::");
            }
            sb.append(constraintsName);
        }
        if (constraintsType!=null) {
            if (first) {
                first = false;
            } else {
                sb.append("@");
            }
            sb.append(constraintsType);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Constraint(");
        boolean first = true;
        if (!StringUtils.isBlank(catalogName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("catalog='" + catalogName + '\'');
        }
        if (!StringUtils.isBlank(schemaName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("schema='" + schemaName + '\'');
        }
        if (!StringUtils.isBlank(tableName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("table='" + tableName + '\'');
        }
        if (!StringUtils.isBlank(constraintsName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("constraintsName='" + constraintsName + '\'');
        }
        if (constraintsType!=null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("constraintsType='" + constraintsType + '\'');
        }
        sb.append(")");
        return sb.toString();
    }

    public TableId getStructId() {
        return getTableId();
    }

    public TableId getTableId() {
        return new TableId(catalogName, schemaName, tableName);
    }

    public SchemaId getSchemaId() {
        return new SchemaId(catalogName, schemaName);
    }

    public CatalogId getCatalogId() {
        return new CatalogId(catalogName);
    }

}
