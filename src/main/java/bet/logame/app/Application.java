package bet.logame.app;

import bet.logame.app.facade.ProcessorFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class Application {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	@Autowired
	private ProcessorFacade processorFacade;

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(Application.class, args);
		Application app = context.getBean(Application.class);
		logger.info("Inicio do Processamento ...");
		app.processImages();
		logger.info("Final do Processamento ...");
		SpringApplication.exit(context, () -> 0);
	}

	private void processImages() {
		Path imageDir = Paths.get("image");
		if (!Files.exists(imageDir) || !Files.isDirectory(imageDir)) {
			logger.error("O diretório de imagens nao existe ou nao é um diretorio.");
			return;
		}

		try {
			long fileCount = Files.list(imageDir)
					.filter(Files::isRegularFile)
					.filter(file -> file.toString().endsWith(".jpg") || file.toString().endsWith(".png"))
					.count();

			logger.info("Total de imagens a processar: {}", fileCount);
			processorFacade.processImages(imageDir.toString());
			logger.info("Processamento de imagens concluido.");
		} catch (Exception e) {
			logger.error("Erro ao processar as imagens no diretorio: {}", imageDir, e);
		}
	}
}