package bet.logame.app.facade;

import org.springframework.stereotype.Component;

@Component
public interface ProcessorFacade {
    void processImages(String directoryPath);
}
