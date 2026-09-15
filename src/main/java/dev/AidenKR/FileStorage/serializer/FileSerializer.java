package dev.AidenKR.FileStorage.serializer;

import java.io.IOException;

public interface FileSerializer {

    String serialize(Object object) throws IOException;

    <T> T deserialize(String data, Class<T> clazz) throws IOException;
}
