package org.seasar.cms.ymir;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.cms.ymir.scope.Scope;
import org.seasar.kvasir.util.io.IORuntimeException;

public class ScopeAttribute {

    private String name_;

    private Scope scope_;

    private Method writeMethod_;

    private Method readMethod_;

    private static final Log log_ = LogFactory.getLog(ScopeAttribute.class);

    public ScopeAttribute(String name, Scope scope, Method writeMethod,
            Method readMethod) {
        name_ = name;
        scope_ = scope;
        writeMethod_ = writeMethod;
        readMethod_ = readMethod;
    }

    public void injectTo(Object component) {
        Object value = scope_.getAttribute(name_);
        if (value != null) {
            try {
                writeMethod_.invoke(component, new Object[] { value });
            } catch (IllegalArgumentException ex) {
                // 型が合わなかった場合は単に無視する。
                log_.warn("Can't inject scope attribute: scope=" + scope_
                        + ", attribute name=" + name_ + ", value=" + value
                        + ", write method=" + writeMethod_, ex);
            } catch (Throwable t) {
                throw new IORuntimeException(
                        "Can't inject scope attribute: scope=" + scope_
                                + ", attribute name=" + name_ + ", value="
                                + value + ", write method=" + writeMethod_, t);
            }
        }
    }

    public void outjectFrom(Object component) {
        Object value;
        try {
            value = readMethod_.invoke(component, new Object[0]);
        } catch (Throwable t) {
            throw new IORuntimeException(
                    "Can't outject scope attribute: scope=" + scope_
                            + ", attribute name=" + name_ + ", read method="
                            + readMethod_, t);
        }
        scope_.setAttribute(name_, value);
    }
}
