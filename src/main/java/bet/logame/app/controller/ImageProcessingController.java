package bet.logame.app.controller;

import bet.logame.app.facade.ProcessorFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/jobs")
public class ImageProcessingController {

    private final ProcessorFacade processorFacade;

    public ImageProcessingController(ProcessorFacade processorFacade) {
        this.processorFacade = processorFacade;
    }

    @GetMapping("/process-images")
    public String processImages() {
        return processorFacade.processImages();
    }
}