package com.web.gallery.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.io.Serializable;

/**
 * {@code .claude/rules/domain.md}に定義されたDomain（値オブジェクト）クラスのアーキテクチャルールを検証するテスト
 *
 * <p>{@code ImageFile}は{@code MultipartFile}をラップしており、{@code MultipartFile}自体が{@code
 * Serializable}を実装しないため、規約上明記された例外として除外する。
 */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class DomainArchitectureTest {

  private static final String SERIALIZABLE_EXCEPTION = "ImageFile";

  @ArchTest
  static final ArchRule domainClassShouldImplementSerializable =
      classes()
          .that()
          .resideInAPackage(Packages.DOMAIN + "..")
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .and(not(simpleName(SERIALIZABLE_EXCEPTION)))
          .should()
          .implement(Serializable.class)
          .as("domainパッケージのクラスはSerializableを実装すること（ImageFileを除く）");
}
