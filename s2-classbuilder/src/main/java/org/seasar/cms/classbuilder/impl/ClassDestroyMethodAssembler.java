package org.seasar.cms.classbuilder.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.seasar.cms.classbuilder.S2ContainerPreparer;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.IllegalMethodRuntimeException;
import org.seasar.framework.container.MethodDef;
import org.seasar.framework.container.assembler.DefaultDestroyMethodAssembler;


public class ClassDestroyMethodAssembler extends
    DefaultDestroyMethodAssembler
{
    public ClassDestroyMethodAssembler(ComponentDef componentDef)
    {
        super(componentDef);

    }


    @Override
    public void assemble(Object component)
        throws IllegalMethodRuntimeException
    {
        if (component == null) {
            return;
        }
        BeanDesc beanDesc = getBeanDesc(component);
        int size = getComponentDef().getDestroyMethodDefSize();
        for (int i = 0; i < size; ++i) {
            MethodDef methodDef = getComponentDef().getDestroyMethodDef(i);
            Method method = methodDef.getMethod();
            if (method != null
                && S2ContainerPreparer.class.isAssignableFrom(method
                    .getDeclaringClass())) {
                invokePreparerMethod(component, methodDef);
            } else {
                invoke(beanDesc, component, methodDef);
            }
        }
    }


    void invokePreparerMethod(Object component, MethodDef methodDef)
    {
        try {
            methodDef.getMethod().invoke(methodDef.getArgs()[0],
                new Object[] { component });
        } catch (IllegalArgumentException ex) {
            throw new IllegalMethodRuntimeException(
                getComponentClass(component), getComponentDef()
                    .getComponentName(), ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalMethodRuntimeException(
                getComponentClass(component), getComponentDef()
                    .getComponentName(), ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalMethodRuntimeException(
                getComponentClass(component), getComponentDef()
                    .getComponentName(), ex);
        }
    }
}
