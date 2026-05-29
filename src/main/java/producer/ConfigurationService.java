package producer;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigurationService {
    private final Dotenv dotenv;

    public ConfigurationService() {
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
    }

    public String get(String key, String fallbackEnvKey) {
        String value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        value = System.getenv(fallbackEnvKey != null ? fallbackEnvKey : key);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        return null;
    }
}
