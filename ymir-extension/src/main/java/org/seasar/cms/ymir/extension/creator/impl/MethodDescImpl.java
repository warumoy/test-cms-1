package org.seasar.cms.ymir.extension.creator.impl;

import java.lang.reflect.Method;

import org.seasar.cms.ymir.extension.creator.AbstractAnnotatedDesc;
import org.seasar.cms.ymir.extension.creator.BodyDesc;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.ParameterDesc;
import org.seasar.cms.ymir.extension.creator.TypeDesc;

public class MethodDescImpl extends AbstractAnnotatedDesc implements MethodDesc {

    private String name_;

    private ParameterDesc[] parameterDescs_ = new ParameterDesc[0];

    private TypeDesc returnTypeDesc_ = new TypeDescImpl(TypeDesc.TYPE_VOID);

    private BodyDesc bodyDesc_;

    private String evaluatedBody_;

    public MethodDescImpl(String name) {

        name_ = name;
    }

    public MethodDescImpl(Method method) {

        name_ = method.getName();
        returnTypeDesc_ = new TypeDescImpl(method.getReturnType());
        Class[] types = method.getParameterTypes();
        parameterDescs_ = new ParameterDesc[types.length];
        for (int i = 0; i < types.length; i++) {
            parameterDescs_[i] = new ParameterDescImpl(types[i]);
        }
    }

    public Object clone() {

        MethodDescImpl cloned;
        try {
            cloned = (MethodDescImpl) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
        if (parameterDescs_ != null) {
            cloned.parameterDescs_ = new ParameterDesc[parameterDescs_.length];
            for (int i = 0; i < parameterDescs_.length; i++) {
                cloned.parameterDescs_[i] = (ParameterDesc) parameterDescs_[i]
                        .clone();
            }
        }
        if (returnTypeDesc_ != null) {
            cloned.returnTypeDesc_ = (TypeDesc) returnTypeDesc_.clone();
        }
        if (bodyDesc_ != null) {
            cloned.bodyDesc_ = (BodyDesc) bodyDesc_.clone();
        }

        return cloned;
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append(returnTypeDesc_).append(" ").append(name_).append("(");
        String delim = "";
        for (int i = 0; i < parameterDescs_.length; i++) {
            sb.append(delim).append(parameterDescs_[i]);
            delim = ", ";
        }
        sb.append(")");
        return sb.toString();
    }

    public String getName() {

        return name_;
    }

    public TypeDesc getReturnTypeDesc() {

        return returnTypeDesc_;
    }

    public void setReturnTypeDesc(TypeDesc returnTypeDesc) {

        returnTypeDesc_ = returnTypeDesc;
    }

    public void setReturnTypeDesc(String typeName) {

        setReturnTypeDesc(typeName, false);
    }

    public void setReturnTypeDesc(String typeName, boolean explicit) {

        setReturnTypeDesc(new TypeDescImpl(typeName, explicit));
    }

    public ParameterDesc[] getParameterDescs() {

        return parameterDescs_;
    }

    public void setParameterDescs(ParameterDesc[] parameterDescs) {

        parameterDescs_ = parameterDescs;
    }

    public BodyDesc getBodyDesc() {

        return bodyDesc_;
    }

    public void setBodyDesc(BodyDesc bodyDesc) {

        bodyDesc_ = bodyDesc;
    }

    public String getEvaluatedBody() {

        return evaluatedBody_;
    }

    public void setEvaluatedBody(String evaluatedBody) {

        evaluatedBody_ = evaluatedBody;
    }
}
