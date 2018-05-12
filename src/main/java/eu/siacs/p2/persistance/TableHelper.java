package eu.siacs.p2.persistance;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TableHelper {

    public static String name(Class clazz) {
        return clazz.getSimpleName().toLowerCase(Locale.ENGLISH);
    }

    public static void create(Sql2o sql2o, Class clazz) {
        List<String> indices = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CREATE TABLE IF NOT EXISTS ").append(name(clazz)).append(" (");
        Field[] fields = clazz.getDeclaredFields();
        for(int i = 0; i <fields.length; ++i) {
            Class type = fields[i].getType();
            PrimaryKey primaryKeyAnnotation = fields[i].getAnnotation(PrimaryKey.class);
            Index index = fields[i].getAnnotation(Index.class);
            stringBuilder.append(fields[i].getName()).append(' ');
            if (type.equals(int.class) || type.equals(long.class)) {
                stringBuilder.append("NUMBER");
            } else {
                stringBuilder.append("TEXT");
            }
            if (primaryKeyAnnotation != null) {
                stringBuilder.append(" PRIMARY KEY");
                if (primaryKeyAnnotation.replace()) {
                    stringBuilder.append(" ON CONFLICT REPLACE");
                }
            }
            if (index != null) {
                indices.add(fields[i].getName());
            }
            if (i != fields.length -1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(");");
        try (Connection connection = sql2o.open()) {
            connection.createQuery(stringBuilder.toString()).executeUpdate();
            for(String field : indices) {
                connection.createQuery("CREATE INDEX IF NOT EXISTS "+field+"_idx ON "+name(clazz)+"("+field+")").executeUpdate();
            }
        }
    }

}
