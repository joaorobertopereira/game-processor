package bet.logame.app.utils;

import de.huxhorn.sulky.ulid.ULID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileNameGenerator {
    private static final ULID ulid = new ULID();

    public static String gerarNomeArquivo() {
        log.info("Gerando nome de arquivo");
        return ulid.nextULID() + ".png";
    }
}
