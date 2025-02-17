package bet.logame.app.service;

import bet.logame.app.utils.FileNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class ImageProcessingService {

    private final RestTemplate restTemplate;
    private FileWriter sqlFileWriter;
    private int processedCount;
    private int errorCount;

    private static final String DOWNLOAD_BASE_URL = "https://lr08x848qha5.objectstorage.sa-saopaulo-1.oci.customer-oci.com/p/mSvQH4TctRSaYT-9jzWZAJ-eUYw3NdUhY4JE49KoQqkRjFXlIq5gRSEfPQepvJRq/n/lr08x848qha5/b/logame-api-images/o";
    private static final String UPLOAD_BASE_URL = "https://lr08x848qha5.objectstorage.sa-saopaulo-1.oci.customer-oci.com/p/i48CYVpGJG5MFIOxIkTWvLw3O1i9FlP-Hz5s9y1PpmKX5MAQxAuFC8R8vof1ROu0/n/lr08x848qha5/b/logame-api-images/o";
    private static final String DEFAULT_FOLDER = "Casino/Games";

    public ImageProcessingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        initializeSqlFileWriter();
        this.processedCount = 0;
        this.errorCount = 0;
    }

    private void initializeSqlFileWriter() {
        try {
            File sqlDirectory = new File("sql");
            if (!sqlDirectory.exists()) {
                sqlDirectory.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            File sqlFile = new File(sqlDirectory, "update_" + timestamp + ".sql");
            sqlFileWriter = new FileWriter(sqlFile, true);
        } catch (IOException e) {
            log.error("Erro ao inicializar o escritor de arquivos SQL", e);
        }
    }

    public void processImages(File directory) throws Exception {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".webp"));
        if (files != null) {
            for (File file : files) {
                try {
                    processImage(file);
                    processedCount++;
                } catch (Exception e) {
                    log.error("Erro ao processar arquivo: {}", file.getName(), e);
                    errorCount++;
                }
            }
            log.info("Total de arquivos processados: {}", processedCount);
            log.info("Total de arquivos com erro: {}", errorCount);
        } else {
            log.warn("Nenhum arquivo encontrado no diretório: {}", directory.getPath());
        }
    }

    private void processImage(File file) throws Exception {
        log.info("Processando a Imagem: {}", file.getName());
        String fileName = file.getName();
        String[] parts = fileName.split("_");
        if (parts.length < 4) {
            log.warn("Nome de arquivo inválido: {}", fileName);
            return;
        }
        String gameId = parts[0];
        String fornecedor = parts[1];
        String formato = parts[2];
        String color = parts[3].substring(0, 7);

        String newFileName = FileNameGenerator.gerarNomeArquivo();
        String downloadLink = String.format("%s/%s/%s", DOWNLOAD_BASE_URL, DEFAULT_FOLDER, newFileName);

        // Generate SQL update command
        generateSqlUpdateCommand(gameId, fornecedor, downloadLink, formato);
        generateSqlUpdateColorCommand(gameId, fornecedor, color);

        // Rename the file
        File newFile = new File(file.getParent(), newFileName);
        if (file.renameTo(newFile)) {
            log.info("Arquivo renomeado para: {}", newFileName);
            uploadFile(newFile, newFileName);
        } else {
            log.warn("Falha ao renomear o arquivo: {}", fileName);
        }
    }

    private void generateSqlUpdateCommand(String gameId, String fornecedor, String downloadLink, String formato) {
        String sql;
        if (formato.equalsIgnoreCase("vertical")) {
            sql = String.format(
                    "UPDATE sis_cassino_jogos SET imagem_horizontal='%s' WHERE gameid='%s' AND fornecedor='%s';",
                    downloadLink, gameId, fornecedor
            );
        } else if (formato.equalsIgnoreCase("square")) {
            sql = String.format(
                    "UPDATE sis_cassino_jogos SET imagem_quadrada='%s' WHERE gameid='%s' AND fornecedor='%s';",
                    downloadLink, gameId, fornecedor
            );
        } else {
            return; // No update needed for other formats
        }

        writeSqlToFile(sql);
    }

    private void generateSqlUpdateColorCommand(String gameId, String fornecedor, String color) {
        String sql = String.format(
                "UPDATE sis_cassino_jogos SET color='%s' WHERE gameid='%s' AND fornecedor='%s';",
                color, gameId, fornecedor
        );

        writeSqlToFile(sql);
    }

    private void writeSqlToFile(String sql) {
        try {
            sqlFileWriter.write(sql + System.lineSeparator());
            log.info("Comando SQL gerado: {}", sql);
        } catch (IOException e) {
            log.error("Erro ao escrever o comando SQL no arquivo", e);
        }
    }

    private void uploadFile(File file, String newFileName) {
        if (file.exists()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            FileSystemResource fileResource = new FileSystemResource(file);
            HttpEntity<FileSystemResource> requestEntity = new HttpEntity<>(fileResource, headers);

            String url = String.format("%s/%s/%s", UPLOAD_BASE_URL, DEFAULT_FOLDER, newFileName);
            restTemplate.put(url, requestEntity, String.class);
            log.info("Imagem enviada: {}", url);

            // Move the file to the 'processado' directory
            File processedDir = new File(file.getParent(), "processado");
            if (!processedDir.exists()) {
                processedDir.mkdirs();
            }
            File processedFile = new File(processedDir, file.getName());
            try {
                Files.move(file.toPath(), processedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log.info("Arquivo movido para o diretorio 'processado': {}", processedFile.getPath());
            } catch (IOException e) {
                log.error("Falha ao mover o arquivo para o diretorio 'processado': {}", file.getName(), e);
            }
        } else {
            log.warn("Arquivo de imagem nao encontrado: {}", file.getName());
        }
    }

    public void closeSqlFileWriter() {
        try {
            if (sqlFileWriter != null) {
                sqlFileWriter.close();
            }
        } catch (IOException e) {
            log.error("Erro ao fechar o escritor de arquivos SQL", e);
        }
    }
}