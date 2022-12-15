
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final int DECLARED_FIELDS_SIZE_INDEX = 0;


    public static void main(String[] args) throws ClassNotFoundException, IOException {
        Map<String, List<String>> reflectionInfoPerClass = new HashMap<>();
        //System.out.println(args[1] + " <--------------------------");

        List<Integer> val;
        Map<String, Integer> declaredFields = new HashMap<>(); // Declared Fields of Class and number of them
        Map<String, Integer> fieldsUpTo = new HashMap<>(); // Class name and its declared fields up to class.Object
        Map<String, Integer> declaredMethods = new HashMap<>(); // Class name and number of declared methods
        Map<String, Integer> methodsUpTo = new HashMap<>(); //Class name and number of methods up to class.Object
        Map<String, Integer> superTypes = new HashMap<>(); // Class name and number of its superclasses
        Map<String, List<Integer>> temp = new HashMap<>();
        Map<String, List<String>> classesRelationsVal = new HashMap<>(); // Class name and names of all its superclasses
        Map<String, Integer> numOfSubtypes = new HashMap<>();

        int N = 4; // In case I don't execute the program from bash
        String inputFile = "InputFile.txt";
        String outputFile = "OutputFile.txt";
        try {
            inputFile = args[0];
            outputFile = args[1];
            N = Integer.parseInt(args[2]);

            //https://stackoverflow.com/questions/1277880/how-can-i-get-the-count-of-line-in-a-file-in-an-efficient-way
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            int lines = 0;
            while (reader.readLine() != null) lines++;

            if (N > lines)
            {
                throw new IndexOutOfBoundsException("N greater than lines in file");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> classNames = findAllClassesUsingClassLoader(inputFile); // Get names of classes in file. note:(it returns me for some reason also java.lang.Object)
        System.out.println(classNames.toString());
        filterClassNames(classNames); // method to filter out java.lang.Object
        System.out.println(classNames.toString());

        // Get each classname and pass it in reflectOnType to find fields, methods, superclasses, subclasses
        int classNamesSize = classNames.size();
        for (int i = 0; i < classNamesSize; i++) {
            try {
                temp = reflectOnType(reflectionInfoPerClass, classNames.get(i), outputFile);
                // When reflectOnType returns take each key (class name) and the intefer list of values(fields, fieldsUpTo e.t.c)
                // and formulate the below hashmaps with name of each class as key and number of each
                // attribute as value (declared fields, total, fields..)
                for(String key: temp.keySet()){

                    declaredFields.put(key, temp.get(key).get(DECLARED_FIELDS_SIZE_INDEX));
                    fieldsUpTo.put(key, temp.get(key).get(1));
                    declaredMethods.put(key, temp.get(key).get(2));
                    methodsUpTo.put(key, temp.get(key).get(3));
                    superTypes.put(key, temp.get(key).get(4));
                }
                if (i == N - 1) {
                    break;
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found: " + e.getMessage());
            }
        }
         // Hashmap with class name as key and a list with its superclasses as value
         classesRelationsVal = classesRelations(inputFile ,N);

        // Initialization of numOfSubTypes --> key(name of class) value (number of subtypes)
        for(int i = 0; i < classNames.size(); i++){
            numOfSubtypes.put(classNames.get(i), 0);
        }

        String temp_key;
        List<String> temp_val;
        //System.out.println(classesRelationsVal);
        Set<String> subTypesForClassX = new HashSet<>();
        for(String key:classesRelationsVal.keySet())
        {

            for(String key_in:classesRelationsVal.keySet())
            {
                if(key_in.equals(key)) {
                    continue;
                }
                temp_val = classesRelationsVal.get(key_in); // get list with strings of class names
                filterClassNames(temp_val);
                if(temp_val.contains(key))
                {
//                    Integer numOfSubtypesKey = numOfSubtypes.get(key);
//                    int tempValIndexOfKey = temp_val.indexOf(key);
//                    numOfSubtypes.replace(key, numOfSubtypesKey + tempValIndexOfKey + 1);\
                    int pivotPointIdx = temp_val.indexOf(key);
                    if (pivotPointIdx > 0) {
                        for (int k = pivotPointIdx - 1; k >= 0; k--) {
                            subTypesForClassX.add(temp_val.get(k));
                        }
                        subTypesForClassX.add(key_in);
                    }
                }
            }
            int size = subTypesForClassX.size();
            numOfSubtypes.put(key, size);
            subTypesForClassX.clear();
        }

        writeToFile(getStringForFile(reverse_map(declaredFields))+"\n\n", getStringForFile(reverse_map(fieldsUpTo))+"\n\n", getStringForFile(reverse_map(declaredMethods))+"\n\n", getStringForFile(reverse_map(methodsUpTo))+"\n\n",
                getStringForFile(reverse_map(superTypes))+"\n\n", getStringForFile(reverse_map(numOfSubtypes))+"\n\n", outputFile);

        // Print the Keys (name of classes) in descending order,
        // according to the values associated with them.
        printAnswerMethod(reverse_map(declaredFields), reverse_map(fieldsUpTo), reverse_map(declaredMethods),
                reverse_map(methodsUpTo), reverse_map(superTypes), reverse_map(numOfSubtypes));


    }

    public static void printAnswerMethod(Map<String, Integer> s1, Map<String, Integer> s2, Map<String, Integer> s3,
                                   Map<String, Integer> s4, Map<String, Integer> s5, Map<String, Integer> s6)
    {
        System.out.println("---1_a---");
        printAnswer(reverse_map(s1));
        System.out.println("---1_b---");
        printAnswer(reverse_map(s2));
        System.out.println("---2_a---");
        printAnswer(reverse_map(s3));
        System.out.println("---2_b---");
        printAnswer(reverse_map(s4));
        System.out.println("---3---");
        printAnswer(reverse_map(s5));
        System.out.println("---4---");
        printAnswer(reverse_map(s6));
    }

    //Get hashmap keys and formulate them into a String to pass to writeToFile
    public static String getStringForFile(Map<String, Integer> input)
    {
        String res = "";
        for(String key:input.keySet())
        {
            res += key+": val: "+input.get(key) + "\n";
        }
        return res;
    }

    // filter out  "java.lang.Object"
    public static void filterClassNames(List<String> input) {
        input.remove("java.lang.Object");
//        List<String> res = new ArrayList<>();
//        for(int i = 0; i < input.size(); i++)
//        {
//            if(input.get(i) != "java.lang.Object" && !res.contains(input.get(i)))
//            {
//               res.add(input.get(i));
//            }
//        }
//        return res;
    }

    //https://howtodoinjava.com/java/sort/java-sort-map-by-values/
    public static Map<String, Integer> reverse_map(Map<String, Integer> input)
    {
        //LinkedHashMap preserve the ordering of elements in which they are inserted
        LinkedHashMap<String, Integer> reverseSortedMap = new LinkedHashMap<>();

        //Use Comparator.reverseOrder() for reverse ordering
        input.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
        return reverseSortedMap;
    }
    public static void printAnswer (Map<String, Integer> x)
    {
        for(String key:x.keySet())
        {
            System.out.println(key+": val: "+x.get(key));
        }
    }
    private static Map<String, List<Integer>> reflectOnType(Map<String, List<String>> classInfo, String typeName, String outputFile) throws ClassNotFoundException {

        Map<String, List<Integer>> results_temp = new HashMap<>(); // everytime that method get called we return Map<Classnam, [num_fields, num_total_fields, e.t.c]>
        Class c = Class.forName(typeName); //Extract class name
        String className = c.getName(); // Get String name of Class
        List<Field>  declaredFields = (List<Field>) getClassFields(c); //All Declared fields of the class (not up to Object)
        int declaredFieldsSize = declaredFields.size(); //number of declared fields of Class
        List<Method> declaredMethods = List.of(getDeclaredMethods(c).toArray(new Method[0])); //Get all declared methods of the class (not up to Object)
        int classMethodsSize = declaredMethods.size(); // get number of methods

        List<Class> superclasses = getAllSuperClasses(c); //Get class super classes/types
        int numberOfSuperClasses = superclasses.size(); // number of superclasses
        List<Field> allFieldsUpTo = (List<Field>) getFieldsUpTo(c, Object.class); //Get all fields of class and the fields of its super classes   //All Fields up to
        List<Method> allMethodsUpTo = (List<Method>) getMethodsUpTo(c, Object.class);

        results_temp.put(className, Arrays.asList(declaredFieldsSize, allFieldsUpTo.size(), classMethodsSize, allMethodsUpTo.size(), numberOfSuperClasses));

        return results_temp;
    }




   private static List<Class> getAllSuperClasses(Class c) {
       Class C = c;

       List<Class> allSuperClasses = new ArrayList<>();
       while (C != null) {
           C = C.getSuperclass();
           if (C != null) {
               allSuperClasses.add(C);
           }
       }
       return allSuperClasses;
   }


    private static List<Method> getDeclaredMethods(Class c) {
        return List.of(c.getDeclaredMethods());
    }


    public static List<String> findAllClassesUsingClassLoader(String filename) {
        try {
            return Files.readAllLines(Paths.get(filename));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Iterable<Field> getClassFields(Class<?> startClass) {
        return List.of(startClass.getDeclaredFields());
    }

//    https://stackoverflow.com/questions/17451506/list-all-private-fields-of-a-java-object
    public static Iterable<Field> getFieldsUpTo( Class<?> startClass, Class<?> exclusiveParent) {
        List<Field> currentClassFields = new ArrayList(List.of(startClass.getDeclaredFields()));
        Class<?> parentClass = startClass.getSuperclass(); // get superclass

        if (parentClass != null && (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
            List<Field> parentClassFields = (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    // Get declared and all methods up to class.Object
    public static Iterable<Method> getMethodsUpTo( Class<?> startClass, Class<?> exclusiveParent) {
        List<Method> currentClassMethods = new ArrayList(List.of(startClass.getDeclaredMethods()));
        Class<?> parentClass = startClass.getSuperclass(); // get superclass

        if (parentClass != null && (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
            List<Method> parentClassMethods = (List<Method>) getMethodsUpTo(parentClass, exclusiveParent);
            currentClassMethods.addAll(parentClassMethods);
        }

        return currentClassMethods;
    }

    // Return hashmap with key a string(name of class) and value a list(superclasses of subclass)
    public static Map<String, List<String>> classesRelations(String inputFile, Integer N) throws ClassNotFoundException {
        List<String> classNames = findAllClassesUsingClassLoader(inputFile); // get the classnames
        filterClassNames(classNames);
        Map<String, List<String>> classRelations = new HashMap<>();
        Class c;

        for(int i = 0; i < classNames.size(); i++){
            c = Class.forName(classNames.get(i));
            classRelations.put(c.getName(), getAllSuperClassesNames(c));
            if(i == N-1)
                break;
        }

        return classRelations;
    }

    private static List<String> getAllSuperClassesNames(Class c) {
        Class C = c;

        List<String> allSuperClasses = new ArrayList<>();
        while (C != null) {
            //System.out.println(C.getName());

            C = C.getSuperclass();
            if (C != null) {
                allSuperClasses.add(C.getName());
            }
        }
        return allSuperClasses;
    }

    public static void writeToFile(String s1, String s2, String s3, String s4, String s5, String s6, String file) throws IOException {
        Path filePath = Paths.get(file);
        try(BufferedWriter bf = Files.newBufferedWriter(filePath, StandardOpenOption.TRUNCATE_EXISTING)){
            Files.writeString(filePath, "---1_a---\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, s1, StandardOpenOption.APPEND);
            Files.writeString(filePath, "---1_b---\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, s2, StandardOpenOption.APPEND);
            Files.writeString(filePath, "---2_a---\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, s3, StandardOpenOption.APPEND);
            Files.writeString(filePath, "---2_b---\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, s4, StandardOpenOption.APPEND);
            Files.writeString(filePath, "---3---\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, s5, StandardOpenOption.APPEND);
            Files.writeString(filePath, "---4---\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, s6, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
