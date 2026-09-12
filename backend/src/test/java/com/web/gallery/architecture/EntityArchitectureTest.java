package com.web.gallery.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * {@code .claude/rules/entity.md}に定義されたEntityクラスのアーキテクチャルールを検証するテスト
 *
 * <p>{@code @Data}/{@code @Builder}のLombokアノテーション規約は{@code RetentionPolicy.SOURCE}のためバイトコード解析である
 * ArchUnitでは検証不可能であり、本クラスの対象外とする（{@code backend-architecture-checker}によるレビューで担保する）。
 */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class EntityArchitectureTest {

  @ArchTest
  static final ArchRule entityShouldNotHaveDomainModelOrDtoFields =
      classes()
          .that()
          .resideInAPackage(Packages.ENTITY)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should(
              FieldTypeConditions.notHaveFieldsOfType(
                  Packages.DOMAIN, Packages.MODEL, Packages.DTO))
          .as("Entityクラスのプロパティにドメインクラス・Modelクラス・Dtoクラスを使用してはいけない");
}
