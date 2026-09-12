package com.web.gallery.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Set;

/** {@code .claude/rules/response.md}に定義されたResponseクラスのアーキテクチャルールを検証するテスト */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class ResponseArchitectureTest {

  @ArchTest
  static final ArchRule classNameShouldEndWithResponse =
      classes()
          .that()
          .resideInAPackage(Packages.CONTROLLER_RESPONSE)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("Response")
          .as("controller.responseパッケージのクラス名は「Response」で終わる必要がある");

  @ArchTest
  static final ArchRule responseShouldNotHaveDomainModelDtoOrEntityFields =
      classes()
          .that()
          .resideInAPackage(Packages.CONTROLLER_RESPONSE)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should(
              FieldTypeConditions.notHaveFieldsOfType(
                  Packages.DOMAIN, Packages.MODEL, Packages.DTO, Packages.ENTITY))
          .as("Responseクラスのプロパティにドメインクラス・Modelクラス・Dtoクラス・Entityクラスを使用してはいけない");

  @ArchTest
  static final ArchRule responseShouldHaveFactoryMethod =
      classes()
          .that()
          .resideInAPackage(Packages.CONTROLLER_RESPONSE)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should(MethodExistenceConditions.haveStaticFactoryMethod(Set.of("from", "of")))
          .as("Responseクラスにはfrom()またはof()の静的ファクトリメソッドを実装すること");
}
