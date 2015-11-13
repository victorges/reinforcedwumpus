import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

class QTableStorageManager {
    private static final Class<?> STORAGE_CLASS;
    private static final Field DATA_FIELD;

    static {
        Class<?> storageClass = null;
        Field dataField = null;
        try {
            storageClass = Class.forName("QTableStorage");
            dataField = storageClass.getField("DATA");
        } catch (ReflectiveOperationException e) {
            // use default value above
        }
        STORAGE_CLASS = storageClass;
        DATA_FIELD = dataField;
    }

    public static boolean isStaticStorageAvailable() {
        return STORAGE_CLASS != null && DATA_FIELD != null;
    }

    public static InputStream getStaticContent() {
        try {
            return new ByteArrayInputStream(((String)DATA_FIELD.get(null)).getBytes());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
