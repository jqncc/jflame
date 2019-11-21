package org.jflame.apidoc.util;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yucan.zhang
 */
public final class EnvUtils {

    /**
     * 查找源码文件夹下包名
     *
     * @param sourceRoot 源码文件夹
     * @param packageRoot 指定要查找的根包名,即从该包开始查找,可为null
     * @return
     * @throws IOException
     */
    public static List<String> getPackageNames(String sourceRoot, String packageRoot) throws IOException {
        List<String> packageNames = new ArrayList<>();
        Path sourceRootPath = Paths.get(sourceRoot);
        Path searchPath;
        if (packageRoot != null) {
            searchPath = Paths.get(sourceRoot, packageRoot.replace('.', File.separatorChar));
        } else {
            searchPath = sourceRootPath;
        }
        Files.walkFileTree(searchPath, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // 只找存在java文件的包
                String[] javafiles = dir.toFile().list((File d, String name) -> {
                    return name.endsWith(".java");
                });
                if (ArrayUtils.isNotEmpty(javafiles)) {
                    packageNames.add(sourceRootPath.relativize(dir).toString().replace(File.separatorChar, '.'));
                }
                return CONTINUE;
            }
        });
        return packageNames;
    }
}
