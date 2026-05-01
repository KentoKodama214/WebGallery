package com.web.gallary.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.web.gallary.enumuration.DirectionEnum;
import com.web.gallary.enumuration.SortPhotoEnum;

import org.springframework.core.convert.converter.Converter;

/**
 * Web MVCの設定クラス
 * <p>
 * クエリパラメータからEnumへの変換を行うConverterを登録する
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new StringToDirectionEnumConverter());
		registry.addConverter(new StringToSortPhotoEnumConverter());
	}

	/**
	 * クエリパラメータの文字列を{@link DirectionEnum}に変換するConverter
	 */
	private static class StringToDirectionEnumConverter implements Converter<String, DirectionEnum> {
		@Override
		public DirectionEnum convert(String source) {
			return DirectionEnum.getOrDefault(source);
		}
	}

	/**
	 * クエリパラメータの文字列を{@link SortPhotoEnum}に変換するConverter
	 */
	private static class StringToSortPhotoEnumConverter implements Converter<String, SortPhotoEnum> {
		@Override
		public SortPhotoEnum convert(String source) {
			return SortPhotoEnum.getOrDefault(source);
		}
	}
}
