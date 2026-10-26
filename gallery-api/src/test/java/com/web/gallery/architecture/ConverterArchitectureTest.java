package com.web.gallery.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Component;

/**
 * {@code .claude/rules/converter.md}に定義されたConverterクラスのアーキテクチャルールを検証するテスト
 *
 * <p>Converterは、presentation層のRequestクラスからapplication層のModelクラスへの変換のみを責務とする。
 */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class ConverterArchitectureTest {

  @ArchTest
  static final ArchRule classNameShouldEndWithConverter =
      classes()
          .that()
          .resideInAPackage(Packages.CONVERTER + "..")
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("Converter")
          .as("converterパッケージのクラス名は「Converter」で終わる必要がある");

  @ArchTest
  static final ArchRule classShouldBeAnnotatedWithComponent =
      classes()
          .that()
          .resideInAPackage(Packages.CONVERTER + "..")
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .beAnnotatedWith(Component.class)
          .as("converterパッケージのクラスには@Componentを付与すること");

  @ArchTest
  static final ArchRule converterShouldNotDependOnForbiddenPackages =
      noClasses()
          .that()
          .resideInAPackage(Packages.CONVERTER + "..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              Packages.MAPPER + "..",
              Packages.ENTITY + "..",
              Packages.DTO + "..",
              Packages.REPOSITORY_IMPL + "..",
              Packages.SERVICE_IMPL + "..")
          .as("converterはmapper・entity・dto・repository.impl・service.implパッケージに依存してはいけない");
}
