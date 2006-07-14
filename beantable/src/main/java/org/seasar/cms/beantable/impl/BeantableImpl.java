package org.seasar.cms.beantable.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.seasar.cms.beantable.Beantable;
import org.seasar.cms.beantable.JDBCType;
import org.seasar.cms.beantable.annotation.ColumnDetail;
import org.seasar.cms.beantable.annotation.Constraint;
import org.seasar.cms.beantable.annotation.Index;
import org.seasar.cms.beantable.annotation.PrimaryKey;
import org.seasar.cms.beantable.annotation.Unique;
import org.seasar.cms.database.identity.ColumnMetaData;
import org.seasar.cms.database.identity.ConstraintMetaData;
import org.seasar.cms.database.identity.Identity;
import org.seasar.cms.database.identity.IndexMetaData;
import org.seasar.cms.database.identity.TableMetaData;
import org.seasar.cms.database.identity.impl.HsqlIdentity;
import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.framework.log.Logger;

/**
 * <p>
 * <b>同期化：</b> このクラスはスレッドセーフではありません。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
public class BeantableImpl implements Beantable {

    private DataSource ds_;

    private Identity identity_;

    private Class<?> beanClass_;

    private BeanInfo beanInfo_;

    private Set<String> actualJdbcTypeNameSet_ = new HashSet<String>();

    private Map<JDBCType, String> actualJdbcTypeNameByTypeMap_ = new HashMap<JDBCType, String>();

    private TableMetaData table_;

    private boolean activated_;

    private Logger logger_ = Logger.getLogger(getClass());

    private static final Map<String, JDBCType[]> JDBCTYPES_MAP = new HashMap<String, JDBCType[]>();

    public BeantableImpl() {
    }

    public BeantableImpl(Class<?> beanClass) {
        setBeanClass(beanClass);
    }

    static {
        prepareJdbcTypesMap();
    }

    static void prepareJdbcTypesMap() {
        JDBCTYPES_MAP.put(String.class.getName(), new JDBCType[] {
            JDBCType.LONGVARCHAR, JDBCType.VARCHAR });
        JDBCTYPES_MAP.put(BigDecimal.class.getName(),
            new JDBCType[] { JDBCType.NUMERIC });
        JDBCTYPES_MAP.put(Boolean.TYPE.getName(),
            new JDBCType[] { JDBCType.BIT });
        JDBCTYPES_MAP.put(Boolean.class.getName(),
            new JDBCType[] { JDBCType.BIT });
        JDBCTYPES_MAP.put(Byte.TYPE.getName(), new JDBCType[] {
            JDBCType.TINYINT, JDBCType.SMALLINT, JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Byte.class.getName(), new JDBCType[] {
            JDBCType.TINYINT, JDBCType.SMALLINT, JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Short.TYPE.getName(), new JDBCType[] {
            JDBCType.SMALLINT, JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Short.class.getName(), new JDBCType[] {
            JDBCType.SMALLINT, JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Integer.TYPE.getName(),
            new JDBCType[] { JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Integer.class.getName(),
            new JDBCType[] { JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Long.TYPE.getName(), new JDBCType[] {
            JDBCType.BIGINT, JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Long.class.getName(), new JDBCType[] {
            JDBCType.BIGINT, JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Float.TYPE.getName(),
            new JDBCType[] { JDBCType.REAL });
        JDBCTYPES_MAP.put(Float.class.getName(),
            new JDBCType[] { JDBCType.REAL });
        JDBCTYPES_MAP.put(Double.TYPE.getName(),
            new JDBCType[] { JDBCType.DOUBLE });
        JDBCTYPES_MAP.put(Double.class.getName(),
            new JDBCType[] { JDBCType.DOUBLE });
        JDBCTYPES_MAP.put(byte[].class.getName(),
            new JDBCType[] { JDBCType.LONGVARBINARY });
        JDBCTYPES_MAP.put(Date.class.getName(),
            new JDBCType[] { JDBCType.DATE });
        JDBCTYPES_MAP.put(Time.class.getName(),
            new JDBCType[] { JDBCType.TIME });
        JDBCTYPES_MAP.put(Timestamp.class.getName(),
            new JDBCType[] { JDBCType.TIMESTAMP });
        JDBCTYPES_MAP.put(Clob.class.getName(),
            new JDBCType[] { JDBCType.CLOB });
        JDBCTYPES_MAP.put(Blob.class.getName(),
            new JDBCType[] { JDBCType.BLOB });
        JDBCTYPES_MAP.put(Array.class.getName(),
            new JDBCType[] { JDBCType.ARRAY });
        JDBCTYPES_MAP.put(Struct.class.getName(),
            new JDBCType[] { JDBCType.STRUCT });
        JDBCTYPES_MAP.put(Ref.class.getName(), new JDBCType[] { JDBCType.REF });
        JDBCTYPES_MAP.put(Object.class.getName(),
            new JDBCType[] { JDBCType.JAVA_OBJECT });

        JDBCTYPES_MAP.put(java.util.Date.class.getName(), new JDBCType[] {
            JDBCType.DATETIME, JDBCType.DATE });
    }

    public void activate() throws SQLException {

        if (activated_) {
            return;
        }

        gatherDatabaseMetaData();
        table_ = createTableMetaData();

        activated_ = true;
    }

    public Class getBeanClass() {

        return beanClass_;
    }

    public void setBeanClass(Class<?> beanClass) {

        beanClass_ = beanClass;
        try {
            beanInfo_ = Introspector.getBeanInfo(beanClass_);
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
    }

    void gatherDatabaseMetaData() throws SQLException {

        actualJdbcTypeNameSet_.clear();
        actualJdbcTypeNameByTypeMap_.clear();

        Connection con = null;
        ResultSet rs = null;
        try {
            con = ds_.getConnection();
            DatabaseMetaData metaData = con.getMetaData();
            rs = metaData.getTypeInfo();
            while (rs.next()) {
                String typeName = rs.getString("TYPE_NAME");
                JDBCType jdbcType = JDBCType
                    .getInstance(rs.getInt("DATA_TYPE"));
                actualJdbcTypeNameSet_.add(typeName);
                if (!actualJdbcTypeNameByTypeMap_.containsKey(jdbcType)) {
                    actualJdbcTypeNameByTypeMap_.put(jdbcType, typeName);
                }
            }
        } finally {
            DbUtils.closeQuietly(con, null, rs);
        }

        if (identity_ instanceof HsqlIdentity) {
            if (!actualJdbcTypeNameSet_.contains("DATETIME")) {
                actualJdbcTypeNameSet_.add("DATETIME");
            }
        }
    }

    TableMetaData createTableMetaData() {

        TableMetaData table = new TableMetaData(gatherTableName());
        table.setColumns(gatherColumnMetaData(gatherNoPersistentProperties()));
        table.setConstraints(gatherConstraintMetaData());
        table.setIndexes(gatherIndexMetaData());
        return table;
    }

    String gatherTableName() {

        String tableName = null;

        Bean bean = beanClass_.getAnnotation(Bean.class);
        if (bean != null) {
            tableName = bean.table();
        }
        if (tableName == null || tableName.length() == 0) {
            tableName = beanClass_.getName();
            int dot = tableName.lastIndexOf('.');
            if (dot >= 0) {
                tableName = tableName.substring(dot + 1);
            }
            int dollar = tableName.lastIndexOf('$');
            if (dollar >= 0) {
                tableName = tableName.substring(dollar + 1);
            }
            tableName = tableName.toUpperCase();
        }

        return tableName;
    }

    Set<String> gatherNoPersistentProperties() {

        Set<String> noPersistentPropertySet = new HashSet<String>();
        Bean bean = beanClass_.getAnnotation(Bean.class);
        if (bean != null) {
            String[] noPersistentProperty = bean.noPersistentProperty();
            for (int i = 0; i < noPersistentProperty.length; i++) {
                noPersistentPropertySet.add(noPersistentProperty[i]);
            }
        }
        // java.lang.Objectのメソッドは抜いておく。
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(Object.class);
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            noPersistentPropertySet.add(pds[i].getName().toUpperCase());
        }

        return noPersistentPropertySet;
    }

    ColumnMetaData[] gatherColumnMetaData(Set<String> noPersistentPropertySet) {

        PropertyDescriptor[] descriptors = beanInfo_.getPropertyDescriptors();
        List<ColumnMetaData> columnMetaDataList = new ArrayList<ColumnMetaData>(
            descriptors.length);
        for (int i = 0; i < descriptors.length; i++) {
            String propertyName = descriptors[i].getName().toUpperCase();
            if (noPersistentPropertySet.contains(propertyName)) {
                continue;
            }

            ColumnMetaData columnMetaData = new ColumnMetaData();
            columnMetaData.setPropertyDescriptor(descriptors[i]);
            columnMetaData.setName(propertyName);
            String jdbcTypeName = getSuitableJDBCTypeName(descriptors[i]
                .getPropertyType().getName());
            if (jdbcTypeName != null) {
                columnMetaData.setJdbcTypeName(jdbcTypeName);
            }
            if (descriptors[i].getPropertyType().isPrimitive()) {
                columnMetaData.setNotNull(true);
            }

            applyAnnotations(columnMetaData, descriptors[i].getReadMethod());
            applyAnnotations(columnMetaData, descriptors[i].getWriteMethod());

            columnMetaDataList.add(columnMetaData);
        }

        return columnMetaDataList.toArray(new ColumnMetaData[0]);
    }

    void applyAnnotations(ColumnMetaData columnMetaData, Method method) {

        if (method == null) {
            return;
        }
        ColumnDetail column = getColumnAnnotation(method);
        if (column != null) {
            String columnName = column.name();
            if (columnName != null) {
                columnMetaData.setName(columnName);
            }

            String jdbcTypeName = null;
            JDBCType jdbcType = column.type();
            if (jdbcType != null) {
                jdbcTypeName = actualJdbcTypeNameByTypeMap_.get(jdbcType);
            }
            if (jdbcTypeName != null) {
                columnMetaData.setJdbcTypeName(jdbcTypeName);
            }

            String defaultValue = column.defaultValue();
            if (defaultValue != null) {
                columnMetaData.setDefault(defaultValue);
            }

            Constraint[] constraint = column.constraint();
            for (int i = 0; i < constraint.length; i++) {
                if (constraint[i] == Constraint.NOT_NULL) {
                    columnMetaData.setNotNull(true);
                } else if (constraint[i] == Constraint.PRIMARY_KEY) {
                    columnMetaData.setPrimaryKey(true);
                    columnMetaData.setNotNull(true);
                } else if (constraint[i] == Constraint.UNIQUE) {
                    columnMetaData.setUnique(true);
                    columnMetaData.setNotNull(true);
                } else {
                    throw new RuntimeException("Unsupported constraint: "
                        + constraint[i]);
                }
            }

            boolean index = column.index();
            columnMetaData.setIndexCreated(index);
        }

        Id id = method.getAnnotation(Id.class);
        if (id != null) {
            columnMetaData.setPrimaryKey(true);
            columnMetaData.setNotNull(true);
            switch (id.value()) {
            case IDENTITY:
                columnMetaData.setId(true);
                break;

            case ASSIGNED:
                break;

            case SEQUENCE:
                columnMetaData.setId(true);
                String sequenceName = id.sequenceName();
                if (sequenceName == null) {
                    throw new IllegalStateException(
                        "IdType is specified as SEQUENCE but sequenceName is not specified: method="
                            + method);
                }
                columnMetaData.setSequenceName(sequenceName);
                break;

            default:
                if (logger_.isInfoEnabled()) {
                    logger_.info("[SKIP] Unsupported Id annotation value: "
                        + id.value() + ": method=" + method);
                }
            }
        }
    }

    ColumnDetail getColumnAnnotation(Method method) {

        ColumnDetail column = method.getAnnotation(ColumnDetail.class);
        if (column == null) {
            final org.seasar.dao.annotation.tiger.Column s2DaoColumn = method
                .getAnnotation(org.seasar.dao.annotation.tiger.Column.class);
            if (s2DaoColumn != null) {
                column = new ColumnDetail() {
                    public String name() {
                        return s2DaoColumn.value();
                    }

                    public JDBCType type() {
                        return null;
                    }

                    public String defaultValue() {
                        return null;
                    }

                    public Constraint[] constraint() {
                        return new Constraint[0];
                    }

                    public boolean index() {
                        return false;
                    }

                    public Class<? extends Annotation> annotationType() {
                        return null;
                    }
                };
            }
        } else if (column.name() == null) {
            final org.seasar.dao.annotation.tiger.Column s2DaoColumn = method
                .getAnnotation(org.seasar.dao.annotation.tiger.Column.class);
            if (s2DaoColumn != null) {
                final ColumnDetail origColumn = column;
                column = new ColumnDetail() {
                    public String name() {
                        return s2DaoColumn.value();
                    }

                    public JDBCType type() {
                        return origColumn.type();
                    }

                    public String defaultValue() {
                        return origColumn.defaultValue();
                    }

                    public Constraint[] constraint() {
                        return origColumn.constraint();
                    }

                    public boolean index() {
                        return origColumn.index();
                    }

                    public Class<? extends Annotation> annotationType() {
                        return null;
                    }
                };
            }
        }
        return column;
    }

    ConstraintMetaData[] gatherConstraintMetaData() {

        List<ConstraintMetaData> constraintList = new ArrayList<ConstraintMetaData>();

        do {
            PrimaryKey primaryKey = beanClass_.getAnnotation(PrimaryKey.class);
            if (primaryKey == null) {
                break;
            }
            String value = primaryKey.value();
            if (value == null) {
                break;
            }
            ConstraintMetaData constraint = new ConstraintMetaData();
            constraint.setName(ConstraintMetaData.PRIMARY_KEY);
            constraint.setColumnNames(toStringArray(value));
            constraintList.add(constraint);

            Unique unique = beanClass_.getAnnotation(Unique.class);
            if (unique == null) {
                break;
            }
            String[] values = unique.value();
            if (values.length == 0) {
                break;
            }
            for (int i = 0; i < values.length; i++) {
                constraint = new ConstraintMetaData();
                constraint.setName(ConstraintMetaData.UNIQUE);
                constraint.setColumnNames(toStringArray(values[i]));
                constraintList.add(constraint);
            }
        } while (false);

        return constraintList.toArray(new ConstraintMetaData[0]);
    }

    String[] toStringArray(String value) {
        if (value == null) {
            return new String[0];
        }
        List<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(value, ",");
        while (st.hasMoreTokens()) {
            String tkn = st.nextToken().trim();
            if (tkn.length() > 0) {
                list.add(tkn);
            }
        }
        return list.toArray(new String[0]);
    }

    IndexMetaData[] gatherIndexMetaData() {

        List<IndexMetaData> indexList = new ArrayList<IndexMetaData>();

        do {
            Index index = beanClass_.getAnnotation(Index.class);
            if (index == null) {
                break;
            }
            String[] values = index.value();
            if (values.length == 0) {
                break;
            }
            for (int i = 0; i < values.length; i++) {
                IndexMetaData metaData = new IndexMetaData();
                metaData.setColumnNames(toStringArray(values[i]));
                indexList.add(metaData);
            }
        } while (false);

        return indexList.toArray(new IndexMetaData[0]);
    }

    public TableMetaData getTableMetaData() {

        return table_;
    }

    public boolean update() throws SQLException {

        return update(true);
    }

    public boolean update(boolean correctTableSchema) throws SQLException {

        if (!activated_) {
            throw new IllegalStateException("Not activated");
        }

        if (!identity_.existsTable(table_.getName())) {
            return createTable();
        } else if (correctTableSchema) {
            return correctTableSchema();
        }
        return false;
    }

    public boolean createTable() throws SQLException {

        return identity_.createTable(table_);
    }

    public boolean createTable(boolean force) throws SQLException {

        return identity_.createTable(table_, force);
    }

    public boolean correctTableSchema() throws SQLException {

        return identity_.correctTableSchema(table_);
    }

    public boolean correctTableSchema(boolean force) throws SQLException {

        return identity_.correctTableSchema(table_, force);
    }

    public boolean dropTable() throws SQLException {

        return identity_.dropTable(table_);
    }

    public boolean dropTable(boolean force) throws SQLException {

        return identity_.dropTable(table_, force);
    }

    String getSuitableJDBCTypeName(String javaTypeName) {
        JDBCType[] jdbcTypes = JDBCTYPES_MAP.get(javaTypeName);
        if (jdbcTypes == null) {
            return null;
        }
        for (int i = 0; i < jdbcTypes.length; i++) {
            String typeName = jdbcTypes[i].getName();
            if (actualJdbcTypeNameSet_.contains(typeName)) {
                return typeName;
            } else {
                typeName = (String) actualJdbcTypeNameByTypeMap_
                    .get(new Integer(jdbcTypes[i].getType()));
                if (typeName != null) {
                    return typeName;
                }
            }
        }
        return "VARCHAR";
    }

    public void setDataSource(DataSource ds) {
        ds_ = ds;
    }

    public void setIdentity(Identity identity) {
        identity_ = identity;
    }
}