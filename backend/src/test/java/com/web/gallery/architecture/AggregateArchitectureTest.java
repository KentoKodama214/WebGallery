package com.web.gallery.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** {@code .claude/rules/aggregate.md}に定義された集約（Aggregate）クラスのアーキテクチャルールを検証するテスト */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class AggregateArchitectureTest {

  @ArchTest
  static final ArchRule constructorsShouldBePrivate =
      constructors()
          .that()
          .areDeclaredInClassesThat(
              resideInAPackage(Packages.AGGREGATE).and(ArchPredicates.TOP_LEVEL_CLASSES))
          .should()
          .bePrivate()
          .as("集約ルートクラスのコンストラクタはprivateとすること");

  @ArchTest
  static final ArchRule shouldNotHavePublicSetterMethods =
      methods()
          .that()
          .areDeclaredInClassesThat(
              resideInAPackage(Packages.AGGREGATE).and(ArchPredicates.TOP_LEVEL_CLASSES))
          .and()
          .haveNameStartingWith("set")
          .should()
          .notBePublic()
          .as("集約ルートクラスはpublicなsetterメソッドを公開してはいけない")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule aggregateShouldNotDependOnForbiddenPackages =
      noClasses()
          .that()
          .resideInAPackage(Packages.AGGREGATE)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              Packages.CONTROLLER + "..",
              Packages.MAPPER + "..",
              Packages.ENTITY + "..",
              Packages.DTO + "..",
              Packages.REPOSITORY_IMPL + "..")
          .as("aggregateはcontroller配下・mapper・entity・dto・repository.implパッケージに依存してはいけない");
}
