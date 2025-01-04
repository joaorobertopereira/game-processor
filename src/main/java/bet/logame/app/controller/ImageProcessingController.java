package bet.logame.app.controller;

import bet.logame.app.service.ImageProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@Slf4j
@RequestMapping("/jobs")
public class ImageProcessingController {

    private final ImageProcessingService imageProcessingService;

    @Autowired
    public ImageProcessingController(ImageProcessingService imageProcessingService) {
        this.imageProcessingService = imageProcessingService;
    }

    @GetMapping("/process-images")
    public String processImages() {
        String directoryPath = "src/main/resources/image/";
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".png"));
            if (files != null) {
                for (File file : files) {
                    try {
                        imageProcessingService.processImage(file);
                    } catch (Exception e) {
                        log.error("Erro ao processar arquivo: {}", file.getName(), e);
                    }
                }
                return "Processamento de imagens concluído.";
            } else {
                return "Nenhum arquivo PNG encontrado no diretório.";
            }
        } else {
            return "Diretório inválido.";
        }
    }
}