package com.web.gallery.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * {@code .claude/rules/dto.md}に定義されたDTOクラスのアーキテクチャルールを検証するテスト
 *
 * <p>{@code @Data}のみというLombokアノテーション規約は{@code RetentionPolicy.SOURCE}のためバイトコード解析であるArchUnitでは
 * 検証不可能であり、本クラスの対象外とする（{@code backend-architecture-checker}によるレビューで担保する）。
 */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class DtoArchitectureTest {

  @ArchTest
  static final ArchRule classNameShouldEndWithDto =
      classes()
          .that()
          .resideInAPackage(Packages.DTO)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("Dto")
          .as("dtoパッケージのクラス名は「Dto」で終わる必要がある");

  @ArchTest
  static final ArchRule dtoShouldNotHaveDomainModelOrEntityFields =
      classes()
          .that()
          .resideInAPackage(Packages.DTO)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should(
              FieldTypeConditions.notHaveFieldsOfType(
                  Packages.DOMAIN, Packages.MODEL, Packages.ENTITY))
          .as("Dtoクラスのプロパティにドメインクラス・Modelクラス・Entityクラスを使用してはいけない");
}
