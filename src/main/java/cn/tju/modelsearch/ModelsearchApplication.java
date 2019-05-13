package cn.tju.modelsearch;

import cn.tju.modelsearch.dao.modelMapper;
import cn.tju.modelsearch.domain.ModelSql;
import cn.tju.modelsearch.service.ESClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class ModelsearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModelsearchApplication.class, args);

	}

}
