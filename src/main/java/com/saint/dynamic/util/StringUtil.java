package com.saint.dynamic.util;

/**
 * String工具类
 *
 * @author Saint
 */
public class StringUtil {

    /**
     * 将蛇形格式转换成驼峰式
     *
     * @param snake 蛇形字符串，形如“create_at”
     * @return 返回驼峰式属性，"createAt"
     */
    public static String snakeToCamelCase(String snake) {
        StringBuilder camelCaseProperty = new StringBuilder();
        String[] snakeProperty = snake.split("_");
        int len = snakeProperty.length;
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                Character initial = snakeProperty[i].charAt(0);
                String upperInitial = initial.toString().toUpperCase();
                String initialProperty = snakeProperty[i].replaceFirst(initial.toString(), upperInitial);
                camelCaseProperty.append(initialProperty);
            } else {
                camelCaseProperty.append(snakeProperty[i]);
            }
        }

        return camelCaseProperty.toString();
    }
}
