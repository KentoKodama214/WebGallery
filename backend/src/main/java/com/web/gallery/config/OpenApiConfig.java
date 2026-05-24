package com.web.gallery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * OpenAPI仕様の設定クラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Configuration
public class OpenApiConfig {

	/**
	 * OpenAPI仕様のBeanを生成する
	 * @return	{@link OpenAPI}
	 */
	@Bean
	OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("WebGallery API")
						.version("v1")
						.description("フォトギャラリーWebアプリケーション WebGallery のAPI仕様"))
				.components(new Components()
						.addSecuritySchemes("Bearer", new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("JWT認証。ログインAPIで取得したアクセストークンを設定してください。")));
	}
}
