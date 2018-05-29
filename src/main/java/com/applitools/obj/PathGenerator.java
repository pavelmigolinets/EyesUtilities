package com.applitools.obj;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathGenerator {

    private static final Pattern FILE_PATTERN_PATTERN = Pattern.compile("^(?<path>.*)\\/file:(?<file>.*\\.\\{file_ext\\})$");
    private static String PARAM_TEMPLATE_REGEX = "\\{%s\\}";

    private String path_template;
    private String file_template;

    public PathGenerator(String pathTemplate) {
        Matcher matcher = FILE_PATTERN_PATTERN.matcher(pathTemplate);

        if (matcher.matches()) {
            path_template = matcher.group(1);
            file_template = matcher.group(2);
        } else {
            path_template = pathTemplate;
            file_template = "";
        }
    }

    private PathGenerator(String pathTemplate, String fileTemplate) {
        path_template = pathTemplate;
        file_template = fileTemplate;
    }

    public PathGenerator build(String param, String value) {
        return new PathGenerator(new String(param.replaceAll(param, value)));
    }

    public PathGenerator build(Map<String, String> tokens) {
        String pathTempl = new String(path_template);
        String fileTempl = new String(file_template);
        for (Map.Entry<String, String> token : tokens.entrySet()) {
            pathTempl = pathTempl.replaceAll(String.format(PARAM_TEMPLATE_REGEX, token.getKey()), token.getValue());
            fileTempl = fileTempl.replaceAll(String.format(PARAM_TEMPLATE_REGEX, token.getKey()), token.getValue());
        }
        return new PathGenerator(pathTempl, fileTempl);
    }

    public File generatePath() {
        return new File(path_template);
    }

    //    public File generatePath(String child) {
    //        return new File(path_template, child);
    //    }

    public File generateFile() {
        if (StringUtils.isEmpty(file_template))
            throw new RuntimeException("file_template is empty");
        File pwd = new File(System.getProperty("user.dir"));
        return new File(
                pwd.toURI().relativize(new File(path_template, file_template).toURI()).getPath()
        );
    }

    public void ensureTargetFolder() {
        File outFolder = generatePath();
        if (!outFolder.exists() && !outFolder.mkdirs())
            throw new RuntimeException(
                    String.format("Unable to create output folder for path: %s", outFolder.toString()));
    }
}
