package app;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class ReflectionExample {
    private static String typeWithMostFields, mostProperties, mostMethods, mostConcreteClasses, mostSuperClassesAndInterfaces;
    public static void main(String[] args) {
        Set<Class> classNames = findAllClassesUsingClassLoader("app");
//        List<String> classNames = List.of("app.Classroom", "app.ClassRoomManager", "app.HelloWorld");
        classNames.forEach(className -> {
            try {
                reflectOnType(className.getTypeName());
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found: " + e.getMessage());
            }
        });
        System.out.println("Provide a full qualified class name");
    }

    private static void reflectOnType(String typeName) throws ClassNotFoundException {
        Class c = Class.forName(typeName);
        List<Field> fields = (List<Field>) getFieldsUpTo(c, Object.class);

        Method[] methods = c.getDeclaredMethods();
        System.out.println("The " + typeName + " has " + methods.length + " methods");
        System.out.println("These are the following: " + mapMethodsToString(methods));
        System.out.println("The " + typeName + " has " + fields.size() + " fields");
        System.out.println("These are the following: " + mapFieldsToString(fields) + "\n\n");
        Field[] declaredFields = c.getDeclaredFields();
        int length = declaredFields.length;
        int a = 5;
    }

    private static String mapFieldsToString(List<Field> fields) {
        List<String> fieldStrings = new ArrayList<>();
        for (Field field: fields) {
            String methodStr = mapFieldToString(field);
            fieldStrings.add(methodStr);
        }
        return String.join(", ", fieldStrings);
    }

    private static String mapMethodsToString(Method[] methods) {
        List<String> methodStrings = new ArrayList<>();
        for (Method m: methods) {
            String methodStr = mapMethodToString(m);
            methodStrings.add(methodStr);
        }
        return String.join(", ", methodStrings);
    } 

    private static String mapMethodToString(Method m) {
        return m.getName();
    }

    private static String mapFieldToString(Field field) {
        return field.getName();
    }


    //https://www.baeldung.com/java-find-all-classes-in-package
    public static Set<Class> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    private static Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }


//    https://stackoverflow.com/questions/17451506/list-all-private-fields-of-a-java-object
    public static Iterable<Field> getFieldsUpTo( Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        List<Field> currentClassFields = new ArrayList(List.of(startClass.getDeclaredFields()));
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
            List<Field> parentClassFields = (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }
}
