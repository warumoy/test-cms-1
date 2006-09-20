package org.seasar.cms.ymir.extension.creator.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.TypeDesc;
import org.seasar.cms.ymir.impl.SingleApplication;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.kvasir.util.io.IOUtils;

public class SourceCreatorImplTest extends SourceCreatorImplTestBase {

    public void testGetComponentName() throws Exception {

        String actual = target_.getComponentName("/index.html",
                Request.METHOD_GET);

        assertEquals("indexPage", actual);
    }

    public void testGetClassName1() throws Exception {

        assertNull(target_.getClassName(null));
    }

    public void testGetClassName2() throws Exception {

        String actual = target_.getClassName("indexPage");

        assertEquals("com.example.web.IndexPage", actual);
    }

    public void testGetClassName3() throws Exception {

        String actual = target_.getClassName("hoehoePage");

        assertEquals("com.example.web.HoehoePage", actual);
    }

    public void testGetClassName4() throws Exception {

        assertNull(target_.getClassName("hoehoe"));
    }

    public void testGetWelcomeFile() throws Exception {

        assertEquals("index.html", target_.getWelcomeFile());
    }

    public void testGetClassDesc1() throws Exception {

        assertNull(target_.getClassDesc("hoge"));
    }

    public void testGetClassDesc2() throws Exception {

        ClassDesc actual = target_.getClassDesc("com.example.web.IndexPage");

        assertNotNull(actual);
        assertEquals("com.example.web.IndexPage", actual.getName());
        assertNull(actual.getSuperclassName());
        assertEquals(0, actual.getPropertyDescs().length);
        assertEquals(0, actual.getMethodDescs().length);
    }

    public void testWriteSourceFile1() throws Exception {

        ClassDesc classDesc = constructClassDesc();
        File testPage = new File(ResourceUtil.getBuildDir(getClass()),
                classDesc.getName().replace('.', '/') + ".java");
        File testPageBase = new File(ResourceUtil.getBuildDir(getClass()),
                classDesc.getName().replace('.', '/') + "Base.java");

        testPage.delete();

        target_.writeSourceFile(classDesc, null);

        assertTrue(testPage.exists());
        assertTrue(testPageBase.exists());
    }

    public void testWriteSourceFile2() throws Exception {

        ClassDesc classDesc = constructClassDesc();
        File testPage = new File(ResourceUtil.getBuildDir(getClass()),
                classDesc.getName().replace('.', '/') + ".java");
        File testPageBase = new File(ResourceUtil.getBuildDir(getClass()),
                classDesc.getName().replace('.', '/') + "Base.java");

        testPage.getParentFile().mkdirs();
        testPageBase.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(testPage);
        os.write(32);
        os.close();
        os = new FileOutputStream(testPageBase);
        os.write(32);
        os.close();

        target_.writeSourceFile(classDesc, null);

        String actual = IOUtils.readString(new FileInputStream(testPage),
                "UTF-8", false);
        assertEquals(" ", actual);
        actual = IOUtils.readString(new FileInputStream(testPageBase), "UTF-8",
                false);
        assertFalse(" ".equals(actual));
    }

    public void testGatherClassDescs() throws Exception {

        File sourceDir = clean(new File(ResourceUtil.getBuildDir(getClass())
                .getParentFile(), "src"));
        getSourceCreator().getConfiguration().setProperty(
                SingleApplication.KEY_SOURCEDIRECTORY,
                sourceDir.getCanonicalPath());

        Map<String, ClassDesc> classDescMap = new LinkedHashMap<String, ClassDesc>();
        target_.gatherClassDescs(classDescMap, new PathMetaDataImpl(
                "/test.html", Request.METHOD_GET, false, "testPage",
                "com.example.web.TestPage", null, null, null,
                getSourceCreator().getTemplateFile("/test.html")));
        ClassDesc[] actual = (ClassDesc[]) classDescMap.values().toArray(
                new ClassDesc[0]);

        assertNotNull(actual);
        assertEquals(2, actual.length);
        assertEquals("com.example.web.TestPage", actual[0].getName());
        assertEquals("com.example.dto.EntityDto", actual[1].getName());
        MethodDesc md = actual[0].getMethodDesc(new MethodDescImpl("_get"));
        assertNotNull(md);
        assertEquals(TypeDesc.TYPE_VOID, md.getReturnTypeDesc().getName());
        assertNotNull(actual[0].getMethodDesc(new MethodDescImpl("_render")));
    }
}
