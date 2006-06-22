package org.seasar.cms.framework.creator;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClassDesc {

    public static final String KIND_PAGE = "Page";

    public static final String KIND_DTO = "Dto";

    public static final String KIND_DAO = "Dao";

    public static final String KIND_BEAN = "Bean";

    public static final String KIND_UNKNOWN = "Unknown";

    private static final String[] AUTODETECTED_KINDS = new String[] {
        KIND_PAGE, KIND_DTO, KIND_DAO };

    private String name_;

    private String superclassName_;

    private Map propertyDescMap_ = new LinkedHashMap();

    private Map methodDescMap_ = new LinkedHashMap();

    private String kind_ = KIND_UNKNOWN;

    public ClassDesc(String name) {

        setName(name);
        for (int i = 0; i < AUTODETECTED_KINDS.length; i++) {
            if (name.endsWith(AUTODETECTED_KINDS[i])) {
                setKind(AUTODETECTED_KINDS[i]);
                break;
            }
        }
    }

    public String toString() {
        return "name=" + name_;
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getShortName() {

        int dot = name_.lastIndexOf('.');
        if (dot < 0) {
            return name_;
        } else {
            return name_.substring(dot + 1);
        }
    }

    public String getBaseName() {

        String shortName = getShortName();
        if (shortName.endsWith(kind_)) {
            return shortName.substring(0, shortName.length() - kind_.length());
        } else {
            return shortName;
        }
    }

    public String getPackageName() {

        int dot = name_.lastIndexOf('.');
        if (dot < 0) {
            return "";
        } else {
            return name_.substring(0, dot);
        }
    }

    public void addProperty(String name, int mode) {

        PropertyDesc descriptor = getPropertyDesc(name);
        if (descriptor == null) {
            descriptor = new PropertyDesc(name);
            propertyDescMap_.put(name, descriptor);
        }
        descriptor.addMode(mode);
    }

    public PropertyDesc getPropertyDesc(String name) {

        return (PropertyDesc) propertyDescMap_.get(name);
    }

    public void setPropertyDesc(PropertyDesc propertyDesc) {

        propertyDescMap_.put(propertyDesc.getName(), propertyDesc);
    }

    public PropertyDesc[] getPropertyDescs() {

        return (PropertyDesc[]) propertyDescMap_.values().toArray(
            new PropertyDesc[0]);
    }

    public String getKind() {
        return kind_;
    }

    public void setKind(String kind) {
        kind_ = kind;
    }

    public MethodDesc getMethodDesc(String name) {

        return (MethodDesc) methodDescMap_.get(name);
    }

    public void setMethodDesc(MethodDesc methodDesc) {

        methodDescMap_.put(methodDesc.getName(), methodDesc);
    }

    public MethodDesc[] getMethodDescs() {

        return (MethodDesc[]) methodDescMap_.values()
            .toArray(new MethodDesc[0]);
    }

    public String getSuperclassName() {

        return superclassName_;
    }

    public void setSuperclassName(String superclassName) {

        superclassName_ = superclassName;
    }
}
