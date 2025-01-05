package bet.logame.app.facade.impl;

import bet.logame.app.facade.ProcessorFacade;
import bet.logame.app.service.ImageProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class ProcessorFacadeImpl implements ProcessorFacade {

    private final ImageProcessingService imageProcessingService;

    public ProcessorFacadeImpl(ImageProcessingService imageProcessingService) {
        this.imageProcessingService = imageProcessingService;
    }

    @Override
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
