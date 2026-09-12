package com.web.gallery.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 単一の命名規則のみを定めた各パッケージのアーキテクチャルールを検証するテスト
 *
 * <ul>
 *   <li>{@code .claude/rules/enumeration.md} - {@code enumeration}パッケージは「Enum」サフィックス
 *   <li>{@code .claude/rules/exception.md} - {@code exception}パッケージは「Exception」サフィックス
 *   <li>{@code .claude/rules/type_handler.md} - {@code type_handler}パッケージは「TypeHandler」サフィックス
 * </ul>
 */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class NamingSuffixArchitectureTest {

  @ArchTest
  static final ArchRule enumerationClassNameShouldEndWithEnum =
      classes()
          .that()
          .resideInAPackage(Packages.ENUMERATION)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("Enum")
          .as("enumerationパッケージのクラス名は「Enum」で終わる必要がある");

  @ArchTest
  static final ArchRule exceptionClassNameShouldEndWithException =
      classes()
          .that()
          .resideInAPackage(Packages.EXCEPTION)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("Exception")
          .as("exceptionパッケージのクラス名は「Exception」で終わる必要がある");

  @ArchTest
  static final ArchRule typeHandlerClassNameShouldEndWithTypeHandler =
      classes()
          .that()
          .resideInAPackage(Packages.TYPE_HANDLER)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("TypeHandler")
          .as("type_handlerパッケージのクラス名は「TypeHandler」で終わる必要がある");
}
