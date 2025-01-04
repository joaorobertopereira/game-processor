package bet.logame.app.service;

import bet.logame.app.domain.SisCassinoJogo;
import bet.logame.app.domain.repository.SisCassinoJogoRepository;
import bet.logame.app.utils.FileNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Optional;

@Service
@Slf4j
public class ImageProcessingService {

    private final SisCassinoJogoRepository repository;
    private final RestTemplate restTemplate;

    @Value("${bucket.download-url-base}")
    private String downloadBaseUrl;
    @Value("${bucket.upload-base-url}")
    private String uploadBaseUrl;
    @Value("${bucket.default-folder}")
    private String defaultFolder;

    public ImageProcessingService(SisCassinoJogoRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    public void processImages() throws Exception {
        File directory = new File("src/main/resources/image/");
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".png"));
            if (files != null) {
                for (File file : files) {
                    processImage(file);
                }
            } else {
                log.warn("Nenhum arquivo PNG encontrado no diretório.");
            }
        } else {
            log.warn("Diretório inválido.");
        }
    }

    public void processImage(File file) throws Exception {
        log.info("Processando arquivo: {}", file.getName());
        String fileName = file.getName();
        String[] parts = fileName.split("-");
        if (parts.length < 4) {
            log.warn("Nome do arquivo inválido: {}", fileName);
            return;
        }
        String gameId = parts[0];
        String provedor = parts[1];
        String formato = parts[2];
        String color = parts[3].substring(0, 6);

        Optional<SisCassinoJogo> optionalJogo = repository.findByGameidAndProvedor(gameId, provedor);
        if (optionalJogo.isPresent()) {
            SisCassinoJogo jogo = optionalJogo.get();
            String newFileName = FileNameGenerator.gerarNomeArquivo();
            String downloadLink = String.format("%s/%s/%s", downloadBaseUrl, defaultFolder, newFileName);

            if (formato.equalsIgnoreCase("vertical")) {
                jogo.setImagemHorizontal(downloadLink);
            } else if (formato.equalsIgnoreCase("square")) {
                jogo.setImagemQuadrada(downloadLink);
            }

            jogo.setColor(color);
            repository.save(jogo);

            // Renomear o arquivo
            File newFile = new File(file.getParent(), newFileName);
            if (file.renameTo(newFile)) {
                log.info("Arquivo renomeado para: {}", newFileName);
                uploadFile(newFile, newFileName);
            } else {
                log.warn("Falha ao renomear o arquivo: {}", fileName);
            }
        } else {
            log.warn("Jogo não encontrado para gameId: {} e provedor: {}", gameId, provedor);
        }
    }

    private void uploadFile(File file, String newFileName) {
        if (file.exists()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            FileSystemResource fileResource = new FileSystemResource(file);
            HttpEntity<FileSystemResource> requestEntity = new HttpEntity<>(fileResource, headers);

            String url = String.format("%s/%s/%s", uploadBaseUrl, defaultFolder, newFileName);
            restTemplate.put(url, requestEntity, String.class);
            log.info("Arquivo enviado para o bucket: {}", url);
        } else {
            log.warn("Arquivo de imagem não encontrado: {}", file.getName());
        }
    }
}