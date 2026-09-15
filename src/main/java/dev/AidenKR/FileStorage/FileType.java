package dev.AidenKR.FileStorage;

public enum FileType {

    DETECT(-1),
    PROPERTIES(0),
    CNF(0),
    JSON(1),
    YAML(2),
    SERIALIZED(4),
    ENUM(5),
    ENUMERATION(5);

    private final int code;

    FileType(int code) {
        this.code = code;
    }

    public static String extension(FileType type) {

        return switch (type) {
            case PROPERTIES -> ".properties";
            case CNF -> ".cnf";

            case JSON -> ".json";
            case YAML -> ".yaml";

            case SERIALIZED -> ".sl";

            case ENUM -> ".enum";
            case ENUMERATION -> ".enum";

            case DETECT -> "";
        };
    }

    public int code() {
        return code;
    }
}