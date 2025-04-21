package com.example.crawler.service;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import org.springframework.stereotype.Service;
import com.example.crawler.model.*;
import java.util.List;
@Service
public class ModelGeneratorService {
    private final List<Class<?>> generatedModels = new java.util.ArrayList<>();

    public List<Class<?>> getGeneratedModels() {
        return generatedModels;
    }

    public void generateModels(List<TableInfo> schemas) {
        for (TableInfo tbl : schemas) {
            DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(Object.class)
                .name("models." + capitalize(tbl.getName()));

            for (ColumnInfo col : tbl.getColumns()) {
                Class<?> fieldType = mapSqlType(col.getType());
                String fieldName = decapitalize(col.getName());
                String methodSuffix = capitalize(fieldName);

                builder = builder
                    .defineField(fieldName, fieldType, java.lang.reflect.Modifier.PRIVATE)
                    .defineMethod("get" + methodSuffix, fieldType, java.lang.reflect.Modifier.PUBLIC)
                    .intercept(FieldAccessor.ofBeanProperty())
                    .defineMethod("set" + methodSuffix, void.class, java.lang.reflect.Modifier.PUBLIC)
                    .withParameter(fieldType)
                    .intercept(FieldAccessor.ofBeanProperty());
            }

            Class<?> generatedClass = builder.make()
                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();

            generatedModels.add(generatedClass);
            
        }
    }

    private String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String decapitalize(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private Class<?> mapSqlType(String sqlType) {
        switch (sqlType.toUpperCase()) {
            case "VARCHAR":
            case "TEXT": return String.class;
            case "INT":
            case "INTEGER": return Integer.class;
            case "BIGINT": return Long.class;
            case "DATE":
            case "TIMESTAMP": return java.util.Date.class;
            case "BOOLEAN": return Boolean.class;
            default: return String.class;
        }
    }
}
