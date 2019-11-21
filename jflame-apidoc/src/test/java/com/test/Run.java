package com.test;

import java.io.IOException;

import org.jflame.apidoc.ApiDoclet;

public class Run {

    public static void main(String[] args) throws IOException {
        String sourceRoot = "D:\\workspace2\\japidoc\\src\\test\\java";
        String packageRoot = "com.test.action";
        // List<String> docArgs = Arrays.asList("-private", "-encoding", "utf-8", "-doclet",
        // ApiDoclet.class.getName(),"-subpackages","com.test");
        // List<String> packages = EnvUtils.getPackageNames(sourceRoot, packageRoot);

        // packages.addAll(0, docArgs);
        String[] docArgs = { "-private","-encoding","utf-8","-doclet",ApiDoclet.class.getName(),"-sourcepath",
                sourceRoot,"-subpackages",packageRoot };
        com.sun.tools.javadoc.Main.execute(docArgs);

    }

}
