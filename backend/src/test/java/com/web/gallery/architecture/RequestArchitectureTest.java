package com.web.gallery.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** {@code .claude/rules/request.md}に定義されたRequestクラスのアーキテクチャルールを検証するテスト */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class RequestArchitectureTest {

  @ArchTest
  static final ArchRule classNameShouldEndWithRequest =
      classes()
          .that()
          .resideInAPackage(Packages.CONTROLLER_REQUEST)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("Request")
          .as("controller.requestパッケージのクラス名は「Request」で終わる必要がある");

  @ArchTest
  static final ArchRule requestShouldNotHaveDomainModelDtoOrEntityFields =
      classes()
          .that()
          .resideInAPackage(Packages.CONTROLLER_REQUEST)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should(
              FieldTypeConditions.notHaveFieldsOfType(
                  Packages.DOMAIN, Packages.MODEL, Packages.DTO, Packages.ENTITY))
          .as("Requestクラスのプロパティにドメインクラス・Modelクラス・Dtoクラス・Entityクラスを使用してはいけない");

  @ArchTest
  static final ArchRule requestShouldHaveValidationAnnotation =
      classes()
          .that()
          .resideInAPackage(Packages.CONTROLLER_REQUEST)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should(
              FieldAnnotationConditions.haveAtLeastOneFieldAnnotatedWithAnnotationFromPackage(
                  "jakarta.validation.constraints"))
          .as("Requestクラスのプロパティには最低1つバリデーションアノテーション（jakarta.validation.constraints配下）を付与すること");
}
