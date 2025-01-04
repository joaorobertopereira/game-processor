package bet.logame.app.domain.service;

import bet.logame.app.domain.SisCassinoJogo;
import bet.logame.app.domain.repository.SisCassinoJogoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

@Service
@Slf4j
public class SisCassinoJogosService {

    private final SisCassinoJogoRepository repository;
    private final RestTemplate restTemplate;

    @Value("${bucket.upload-base-url}")
    private String uploadBaseUrl;

    @Value("${bucket.default-folder}")
    private String defaultFolder;

    public SisCassinoJogosService(SisCassinoJogoRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    public void processGames(List<SisCassinoJogo> jogos) throws Exception {
        for (SisCassinoJogo jogo : jogos) {
            if (jogo != null) {
                // Atualize o banco de dados
                log.info("Salvando jogo: {}", jogo.getGameid());
                repository.save(jogo);

                // Faça o upload da imagem
                log.info("Fazendo upload da imagem: {}", jogo.getGameid());
                String fileName = jogo.getImagemHorizontal() != null ? jogo.getImagemHorizontal() : jogo.getImagemQuadrada();
                if (fileName != null) {
                    File file = new File("src/main/resources/image/" + fileName);
                    if (file.exists()) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.IMAGE_PNG);

                        FileSystemResource fileResource = new FileSystemResource(file);
                        HttpEntity<FileSystemResource> requestEntity = new HttpEntity<>(fileResource, headers);

                        String url = String.format("%s/%s/%s", uploadBaseUrl, defaultFolder, fileName);
                        restTemplate.put(url, requestEntity, String.class);
                    } else {
                        log.warn("Arquivo de imagem não encontrado: {}", fileName);
                    }
                } else {
                    log.warn("Nome do arquivo de imagem é nulo para o jogo: {}", jogo.getGameid());
                }
            }
        }
    }
}