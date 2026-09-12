package com.web.gallery.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;

/**
 * {@code .claude/rules/repository.md}に定義されたRepository層のアーキテクチャルールを検証するテスト
 *
 * <p>レイヤー間の依存方向・Impl同士の依存禁止は{@link com.web.gallery.ArchitectureTest}で検証済みのため、本クラスでは命名規則、
 * インターフェース-実装の1対1対応、Springアノテーション、追加の禁止import、メソッドシグネチャを検証する。
 *
 * <p>{@code SchedulerLockRepository#tryLock}は{@code enumeration}パッケージのロック名を引数に取るため、 引数の許容型には{@code
 * enumeration}パッケージも含める（{@code repository.md}に明記）。
 */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class RepositoryArchitectureTest {

  @ArchTest
  static final ArchRule interfaceNameShouldEndWithRepository =
      classes()
          .that()
          .resideInAPackage(Packages.REPOSITORY)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("Repository")
          .as("repositoryパッケージのインターフェース名は「Repository」で終わる必要がある");

  @ArchTest
  static final ArchRule implNameShouldEndWithRepositoryImpl =
      classes()
          .that()
          .resideInAPackage(Packages.REPOSITORY_IMPL)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("RepositoryImpl")
          .as("repository.implパッケージのクラス名は「RepositoryImpl」で終わる必要がある");

  @ArchTest
  static void repositoryInterfaceAndImplShouldBePaired(JavaClasses classes) {
    InterfaceImplPairing.verify(classes, Packages.REPOSITORY, Packages.REPOSITORY_IMPL, "Impl");
  }

  @ArchTest
  static final ArchRule implShouldBeAnnotatedWithRepository =
      classes()
          .that()
          .resideInAPackage(Packages.REPOSITORY_IMPL)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .beAnnotatedWith(Repository.class)
          .as("repository.implパッケージのクラスには@Repositoryを付与すること");

  @ArchTest
  static final ArchRule repositoryShouldNotDependOnForbiddenPackages =
      noClasses()
          .that()
          .resideInAPackage(Packages.REPOSITORY + "..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(Packages.CONTROLLER + "..", Packages.SERVICE + "..")
          .as("repositoryはcontroller配下・serviceパッケージに依存してはいけない");

  @ArchTest
  static final ArchRule interfaceMethodsShouldHaveAllowedSignature =
      methods()
          .that()
          .areDeclaredInClassesThat(
              resideInAPackage(Packages.REPOSITORY).and(ArchPredicates.TOP_LEVEL_CLASSES))
          .should(
              MethodSignatureConditions.haveAllowedSignature(
                  new String[] {
                    Packages.MODEL, Packages.AGGREGATE, Packages.DOMAIN, Packages.ENUMERATION
                  },
                  new String[] {Packages.MODEL, Packages.AGGREGATE, Packages.DOMAIN},
                  true,
                  3))
          .as(
              "Repositoryインターフェースのメソッドは、引数がModel・集約・ドメインクラス・Enumのみ、"
                  + "返り値がModel・集約・ドメインクラス・Boolean・Integer・voidのいずれかで、引数は3個以下であること");
}
