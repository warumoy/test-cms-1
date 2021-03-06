package org.seasar.cms.ymir.extension.creator.impl;

import java.util.Map;

import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;

import com.example.page.TestPageBase;

public class ClassDescImplTest extends SourceCreatorImplTestBase {

    public void testGetInstanceName() throws Exception {

        String actual = new ClassDescImpl("com.example.dto.TestDto")
                .getInstanceName();
        assertEquals("testDto", actual);
    }

    public void testMerge() throws Exception {

        ClassDesc actual = new ClassDescImpl("com.example.page.TestPage");
        PropertyDesc pd = new PropertyDescImpl("param1");
        pd.setMode(PropertyDesc.READ);
        actual.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param2");
        pd.setMode(PropertyDesc.WRITE);
        actual.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param3");
        pd.setTypeDesc("java.lang.String");
        pd.getTypeDesc().setExplicit(true);
        actual.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param4");
        pd.setTypeDesc("java.lang.String[]");
        actual.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param6");
        pd.setTypeDesc("java.lang.Integer");
        actual.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param7");
        pd.setTypeDesc("java.lang.String");
        actual.setPropertyDesc(pd);
        MethodDesc md = new MethodDescImpl("method");
        md.setReturnTypeDesc("java.lang.String");
        actual.setMethodDesc(md);
        md = new MethodDescImpl("method2");
        md.setReturnTypeDesc("java.lang.String", true);
        actual.setMethodDesc(md);
        md = new MethodDescImpl("method3");
        md.setReturnTypeDesc("java.lang.String", true);
        actual.setMethodDesc(md);
        md = new MethodDescImpl("method4");
        md.setReturnTypeDesc("java.lang.String");
        actual.setMethodDesc(md);

        ClassDesc cd2 = new ClassDescImpl("com.example.page.TestPage");
        cd2.setSuperclass(TestPageBase.class);
        pd = new PropertyDescImpl("param1");
        pd.setTypeDesc("java.lang.Integer");
        pd.getTypeDesc().setExplicit(true);
        pd.setMode(PropertyDesc.WRITE);
        cd2.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param2");
        pd.setTypeDesc("java.lang.Integer");
        pd.getTypeDesc().setExplicit(true);
        pd.setMode(PropertyDesc.READ);
        cd2.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param3");
        pd.setTypeDesc("java.lang.Integer");
        pd.getTypeDesc().setExplicit(true);
        cd2.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param5");
        pd.setTypeDesc("java.lang.Integer[]");
        pd.getTypeDesc().setExplicit(true);
        cd2.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param7");
        pd.setTypeDesc("java.lang.Integer");
        cd2.setPropertyDesc(pd);
        md = new MethodDescImpl("method");
        md.setReturnTypeDesc("java.lang.Integer", true);
        md.setBodyDesc(new BodyDescImpl("body"));
        cd2.setMethodDesc(md);
        md = new MethodDescImpl("method2");
        md.setReturnTypeDesc("java.lang.Integer");
        md.setBodyDesc(new BodyDescImpl("body"));
        cd2.setMethodDesc(md);
        md = new MethodDescImpl("method3");
        md.setReturnTypeDesc("java.lang.Integer", true);
        md.setBodyDesc(new BodyDescImpl("body"));
        cd2.setMethodDesc(md);
        md = new MethodDescImpl("method4");
        md.setReturnTypeDesc("java.lang.Integer");
        md.setBodyDesc(new BodyDescImpl("body"));
        cd2.setMethodDesc(md);

        actual.merge(cd2);

        assertEquals("com.example.page.TestPageBase", actual
                .getSuperclassName());
        assertEquals(7, actual.getPropertyDescs().length);
        assertEquals(PropertyDesc.READ | PropertyDesc.WRITE, actual
                .getPropertyDesc("param1").getMode());
        assertEquals("Integer", actual.getPropertyDesc("param1").getTypeDesc()
                .getName());
        assertEquals(PropertyDesc.READ | PropertyDesc.WRITE, actual
                .getPropertyDesc("param2").getMode());
        assertEquals("Integer", actual.getPropertyDesc("param2").getTypeDesc()
                .getName());
        assertEquals("プロパティのtypeが両方ともexplicitである場合はもともとのtypeが優先されること",
                "String", actual.getPropertyDesc("param3").getTypeDesc()
                        .getName());
        assertEquals(PropertyDesc.READ | PropertyDesc.WRITE, actual
                .getPropertyDesc("param1").getMode());
        assertTrue(actual.getPropertyDesc("param4").getTypeDesc().getName()
                .endsWith("[]"));
        assertEquals("Integer[]", actual.getPropertyDesc("param5")
                .getTypeDesc().getName());
        assertEquals("Integer", actual.getPropertyDesc("param6").getTypeDesc()
                .getName());
        assertEquals("プロパティのtypeが両方ともimplicitである場合はもともとのtypeが優先されること",
                "String", actual.getPropertyDesc("param7").getTypeDesc()
                        .getName());
        MethodDesc actualMd = actual
                .getMethodDesc(new MethodDescImpl("method"));
        assertNotNull(actualMd);
        assertEquals("body", ((Map) actualMd.getBodyDesc().getRoot())
                .get("body"));
        assertEquals("Integer", actual.getMethodDesc(
                new MethodDescImpl("method")).getReturnTypeDesc().getName());
        assertEquals("String", actual.getMethodDesc(
                new MethodDescImpl("method2")).getReturnTypeDesc().getName());
        assertEquals("メソッドの返り値のtypeが両方ともexplicitである場合はもともとのtypeが優先されること",
                "String", actual.getMethodDesc(new MethodDescImpl("method3"))
                        .getReturnTypeDesc().getName());
        assertEquals("メソッドの返り値のtypeが両方ともimplicitである場合はもともとのtypeが優先されること",
                "String", actual.getMethodDesc(new MethodDescImpl("method4"))
                        .getReturnTypeDesc().getName());
    }
}
