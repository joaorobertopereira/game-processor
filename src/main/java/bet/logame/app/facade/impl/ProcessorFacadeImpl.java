// src/main/java/bet/logame/app/facade/impl/ProcessorFacadeImpl.java

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
    public void processImages(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            try {
                imageProcessingService.processImages(directory);
            } catch (Exception e) {
                log.error("Erro ao processar arquivos no diretório: {}", directoryPath, e);
            }
            imageProcessingService.closeSqlFileWriter();
        } else {
            log.warn("O diretório não existe ou não é um diretório: {}", directoryPath);
        }
    }
}