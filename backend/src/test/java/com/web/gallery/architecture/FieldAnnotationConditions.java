package com.web.gallery.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * クラスのフィールドに付与されたアノテーションを検証する{@link ArchCondition}を生成するユーティリティ
 *
 * <p>Lombokは{@code compileOnly}依存のためテストコードのコンパイル・実行クラスパス上にアノテーションのClassオブジェクトが存在しない。
 * そのため完全修飾名の文字列でアノテーションを指定する。
 */
final class FieldAnnotationConditions {

  /**
   * クラスの少なくとも1つのフィールドに指定したアノテーションが付与されていることを検証する{@link ArchCondition}を生成する
   *
   * @param annotationFullName 検証対象のアノテーションの完全修飾名（例: {@code "lombok.NonNull"}）
   * @return 生成した{@link ArchCondition}
   */
  static ArchCondition<JavaClass> haveAtLeastOneFieldAnnotatedWith(String annotationFullName) {
    return new ArchCondition<JavaClass>(
        "have at least one field annotated with @" + annotationFullName) {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        boolean hasAnnotatedField =
            javaClass.getFields().stream()
                .anyMatch(field -> field.isAnnotatedWith(annotationFullName));
        if (!hasAnnotatedField) {
          events.add(
              SimpleConditionEvent.violated(
                  javaClass,
                  String.format(
                      "%sには@%sが付与されたフィールドが一つも存在しません",
                      javaClass.getSimpleName(), annotationFullName)));
        }
      }
    };
  }

  /**
   * クラスの少なくとも1つのフィールドに、指定したパッケージ配下のアノテーションが付与されていることを検証する{@link ArchCondition}を生成する
   *
   * @param annotationPackage 検証対象のアノテーションが属するパッケージ名
   * @return 生成した{@link ArchCondition}
   */
  static ArchCondition<JavaClass> haveAtLeastOneFieldAnnotatedWithAnnotationFromPackage(
      String annotationPackage) {
    return new ArchCondition<JavaClass>(
        "have at least one field annotated with an annotation from " + annotationPackage) {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        boolean hasAnnotatedField =
            javaClass.getFields().stream()
                .flatMap(field -> field.getAnnotations().stream())
                .anyMatch(
                    annotation ->
                        AllowedTypes.isInPackage(annotation.getRawType(), annotationPackage));
        if (!hasAnnotatedField) {
          events.add(
              SimpleConditionEvent.violated(
                  javaClass,
                  String.format(
                      "%sには%s配下のバリデーションアノテーションが付与されたフィールドが一つも存在しません",
                      javaClass.getSimpleName(), annotationPackage)));
        }
      }
    };
  }

  private FieldAnnotationConditions() {}
}
