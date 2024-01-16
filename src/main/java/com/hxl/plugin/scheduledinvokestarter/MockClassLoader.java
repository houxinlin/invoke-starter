package com.hxl.plugin.scheduledinvokestarter;

import com.hxl.plugin.scheduledinvokestarter.utils.VersionUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;


public class MockClassLoader extends URLClassLoader {
    private MockClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public static MockClassLoader newMockClassLoader() {
        try {
            byte[] bytes5 = StreamUtils.copyToByteArray(ClassLoader.getSystemClassLoader().getResourceAsStream("spring-test-5.3.30.jar"));
            byte[] bytes6 = StreamUtils.copyToByteArray(ClassLoader.getSystemClassLoader().getResourceAsStream("spring-test-6.0.13.jar"));
            Files.write(Paths.get(Config.getLibPath(), Config.SPRING_TEST_5), bytes5);
            Files.write(Paths.get(Config.getLibPath(), Config.SPRING_TEST_6), bytes6);

            URL[] urls = VersionUtils.isSpringBoot3Dot0() ? new URL[]{new File(Paths.get(Config.getLibPath(), Config.SPRING_TEST_6).toString()).toURI().toURL()} :
                    new URL[]{new File(Paths.get(Config.getLibPath(), Config.SPRING_TEST_5).toString()).toURI().toURL()};
            return new MockClassLoader(urls, ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            if (name.endsWith("Dispatcher")) {
                InputStream resourceAsStream = getParent().getResourceAsStream(name.replace(".", "/") + ".class");
                byte[] bytes = StreamUtils.copyToByteArray(resourceAsStream);
                return defineClass(name, bytes, 0, bytes.length);
            }
            return findClass(name);
        } catch (Exception e) {
        }
        return super.loadClass(name, resolve);
    }
}
