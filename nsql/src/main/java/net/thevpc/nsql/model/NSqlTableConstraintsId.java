/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.model;


import net.thevpc.nsql.util.WithFullName;
import net.thevpc.nuts.util.NBlankable;

/**
 * @author vpc
 */
public class NSqlTableConstraintsId implements WithFullName {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String constraintsName;
    private NSqlTableConstraintsType constraintsType;

    public NSqlTableConstraintsId(String catalogName, String schemaName, String tableName, String constraintsName, NSqlTableConstraintsType constraintsType) {
        this.schemaName = schemaName;
        this.catalogName = catalogName;
        this.tableName = tableName;
        this.constraintsName = constraintsName;
        this.constraintsType = constraintsType;
    }

    public NSqlTableConstraintsType getConstraintsType() {
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
        final NSqlTableConstraintsId other = (NSqlTableConstraintsId) obj;
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
        if (!NBlankable.isBlank(catalogName)) {
            if (first) {
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(catalogName);
        }
        if (!NBlankable.isBlank(schemaName)) {
            if (first) {
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(schemaName);
        }
        if (!NBlankable.isBlank(tableName)) {
            if (first) {
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(tableName);
        }
        if (!NBlankable.isBlank(constraintsName)) {
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
        if (!NBlankable.isBlank(catalogName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("catalog='" + catalogName + '\'');
        }
        if (!NBlankable.isBlank(schemaName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("schema='" + schemaName + '\'');
        }
        if (!NBlankable.isBlank(tableName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("table='" + tableName + '\'');
        }
        if (!NBlankable.isBlank(constraintsName)) {
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

//    public StoreStructId getStructId() {
//        return new TableIdAsStoreStructId(getTableId());
//    }

    public NSqlTableId getTableId() {
        return new NSqlTableId(catalogName, schemaName, tableName);
    }

    public NSqlSchemaId getSchemaId() {
        return new NSqlSchemaId(catalogName, schemaName);
    }

    public NSqlCatalogId getCatalogId() {
        return new NSqlCatalogId(catalogName);
    }

}
