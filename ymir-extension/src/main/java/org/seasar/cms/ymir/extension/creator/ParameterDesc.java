package org.seasar.cms.ymir.extension.creator;

public interface ParameterDesc extends Cloneable {

    Object clone();

    TypeDesc getTypeDesc();

    void setTypeDesc(TypeDesc typeDesc);

    String getName();

    void setName(String name);
}
