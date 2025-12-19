package com.github.azeroth.hotfixes.db2;

import com.github.azeroth.dbc.DbcObjects;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CodeGenerator {


    public String fileCode = """
            package com.github.jtrinity.dbc.domain;
            
            import com.github.jtrinity.common.LocalizedString;
            import com.github.jtrinity.cache.DbcEntity;
            import com.github.jtrinity.dbc.db2.Db2Field;
            import com.github.jtrinity.dbc.db2.Db2File;
            import com.github.jtrinity.dbc.db2.Db2Type;
            
            import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
            import lombok.Getter;
            import lombok.Setter;
            import lombok.ToString;
            
            
            @Getter
            @Setter
            @ToString
            
            
            @Table(name = "%s")
            @Db2File(name = "%s", layoutHash = %s, indexField = %s, parentIndexField = %s)
            public class %s implements DbcEntity {
            %s
            }
            """;

    public static final String PATH_JAVA = "C:\\Users\\Jorgie\\Documents\\CodeRepo\\PandariaCommunityServer\\";
    public static final String PATH_CPLUSPLUS = "\\\\wsl.localhost\\Ubuntu-22.04\\home\\jorgie\\CoreRepo\\LegionCommunityServer\\";

    private static class FileMate {
        String name;
        String dataId;
        String layoutHash;
        String indexField;
        String parentIndexField;
        List<String[]> fields = new ArrayList<>();
    }


    void generate() throws IOException {

        Set<String> sets = Set.of();

        Pattern fileName = Pattern.compile(".+Store\\(\\\"(\\w+.db2)\\\", (\\w+)LoadInfo", Pattern.CASE_INSENSITIVE);
        Pattern structMeta = Pattern.compile("struct (\\w+)Meta", Pattern.CASE_INSENSITIVE);
        Pattern structMetaItem = Pattern.compile(".+instance\\(([-|\\d]+), \\d+, (\\w+), \\w+, +\\w+. ([-|\\d]+)\\)", Pattern.CASE_INSENSITIVE);

        Pattern name = Pattern.compile("struct\\s+(\\w+)LoadInfo", Pattern.CASE_INSENSITIVE);

        Pattern type = Pattern.compile("\\s+\\{\\s+(false|true),\\s+FT_(\\w+),\\s+\"(\\w+)\"\\s}", Pattern.CASE_INSENSITIVE);


        Path path = Paths.get(PATH_CPLUSPLUS + "src\\server\\game\\DataStores\\DB2Stores.cpp");
        Map<String, FileMate> dbFileName = new LinkedHashMap<>();
        for (String line : Files.readAllLines(path)) {
            Matcher nameMatcher = fileName.matcher(line);
            if (nameMatcher.find()) {
                String db2file = nameMatcher.group(1);
                String entryName = nameMatcher.group(2);
                FileMate fileMate = new FileMate();
                fileMate.name = db2file;
                dbFileName.put(entryName, fileMate);

            }
        }

        path = Paths.get(PATH_CPLUSPLUS + "src\\server\\game\\DataStores\\DB2Metadata.h");
        String entryName = "";
        for (String line : Files.readAllLines(path)) {
            Matcher nameMatcher = structMeta.matcher(line);
            if (nameMatcher.find()) {
                entryName = nameMatcher.group(1);
                entryName = entryName.replace("_", "");
                continue;
            }
            Matcher matcher = structMetaItem.matcher(line);
            if (matcher.find()) {
                FileMate fileMate = dbFileName.get(entryName);
                if (fileMate == null) {
                    continue;
                }

                fileMate.indexField = matcher.group(1);
                fileMate.layoutHash = matcher.group(2);
                fileMate.parentIndexField = matcher.group(3);
            }
        }


        path = Paths.get(PATH_CPLUSPLUS + "src\\server\\game\\DataStores\\DB2LoadInfo.h");


        entryName = "";
        for (String line : Files.readAllLines(path)) {
            Matcher nameMatcher = name.matcher(line);
            if (nameMatcher.find()) {
                entryName = nameMatcher.group(1);
                continue;
            }
            Matcher matcher = type.matcher(line);
            if (matcher.find()) {
                FileMate fileMate = dbFileName.get(entryName);
                if (fileMate == null) {
                    throw new IllegalStateException(entryName);
                }
                fileMate.fields.add(new String[]{matcher.group(1), matcher.group(2), matcher.group(3)});
            }
        }

        Map<String, String> classFix = new LinkedHashMap<>();
        Map<String, String> fieldFix = new HashMap<>();
        fieldFix.put("ID", "id");
        fieldFix.put("Class", "klass");

        Function<String, String> fieldNormalize = (value) -> {
            if (fieldFix.get(value) != null) {
                return fieldFix.get(value);
            }
            ;
            String s = Character.toLowerCase(value.charAt(0)) + value.substring(1);
            s = s.replace("modifier", "Modifier");
            return s;
        };

        dbFileName.forEach((key, value) -> {

            if (!sets.contains(key)) {
                return;
            }

            if (classFix.containsKey(key)) {
                key = classFix.get(key);
            } else if (key.endsWith("s")) {
                key = key.substring(0, key.length() - 1);
            }

            Path sourceFile = Paths.get(PATH_JAVA + "service-hotfixes\\src\\main\\java\\com\\github\\jtrinity\\dbc\\domain\\", key + ".java");


            StringBuilder texxt = new StringBuilder();

            for (int i = 0; i < value.fields.size(); i++) {
                String[] field = value.fields.get(i);

                String javaType = switch (field[1]) {
                    case "STRING" -> "LocalizedString";
                    case "STRING_NOT_LOCALIZED" -> "String";
                    case "FLOAT" -> "Float";
                    case "INT" -> "Integer";
                    case "BYTE" -> "Byte";
                    case "SHORT" -> "Short";
                    case "LONG" -> "Long";
                    default -> throw new IllegalStateException("Unexpected value: " + field[1]);
                };

                if ("ID".equalsIgnoreCase(field[2])) {
                    texxt.append("""
                                @Id
                                
                                @Column("ID")
                                @Db2Field(fieldIndex = %d, type = Db2Type.INT)
                                private int id;
                            
                            """.formatted(i)
                    );
                } else {
                    texxt.append("""
                            
                                @Column("%s")
                                @Db2Field(fieldIndex = %d, type = Db2Type.%s, signed = %s)
                                private %s %s;
                            
                            """.formatted(field[2], i, field[1], field[0], javaType, fieldNormalize.apply(field[2]))
                    );
                }
            }
            texxt.append("""
                        @Id
                        
                        @Column("VerifiedBuild")
                        private int verifiedBuild;
                    
                    """
            );


            String formatted = fileCode.formatted(key.toLowerCase(), value.name, value.layoutHash, value.indexField, value.parentIndexField, key, texxt.toString());


            System.out.println(key + "(" + key + ".class),");


            try {
                Files.writeString(sourceFile, formatted);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        });

    }

    @Test
    public void generate2() {
        for (DbcObjects value : DbcObjects.values()) {
            String methodName = Character.toLowerCase(value.name().charAt(0)) + value.name().substring(1);
            String text = """
                        default DbcEntityStore<%s> %s() {
                            return getEntityStore(DbcObjects.%s);
                        }
                    
                        default %s %s(Integer id) {
                            return %s().get(id);
                        }
                    """.formatted(value.name(), methodName, value.name(), value.name(), methodName, methodName);

            System.out.println(text);
        }
    }
}