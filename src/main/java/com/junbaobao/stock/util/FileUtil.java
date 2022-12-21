package com.junbaobao.stock.util;

import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

public class FileUtil {


    @SneakyThrows
    public static InputStream getResourceFile(String path) {
        //加载资源文件
        ClassPathResource classPathResource = new ClassPathResource(path);
        return classPathResource.getInputStream();
    }

}
