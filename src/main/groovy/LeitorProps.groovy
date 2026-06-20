import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.file.RegularFileProperty
import java.util.Properties

abstract class LeitorProps implements ValueSource<Map<String, String>, Params> {
    // O arquivo .properties preprocessado no gradle
    interface Params extends ValueSourceParameters {
        RegularFileProperty getPropertiesFile()
    }

    @Override
    Map<String, String> obtain() {
        Properties props = new Properties()
        File fileObj = parameters.propertiesFile.get().asFile
        
        if (fileObj.exists()) {
            fileObj.withInputStream { props.load(it) }
        }
        // Retorna um Map limpo
        return props.collectEntries { k, v -> [(k.toString()): v.toString()] }
    }
}