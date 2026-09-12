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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code .claude/rules/service.md}に定義されたService層のアーキテクチャルールを検証するテスト
 *
 * <p>レイヤー間の依存方向は{@link com.web.gallery.ArchitectureTest}で検証済みのため、本クラスでは命名規則、インターフェース-実装の1対1対応、
 * Springアノテーション、追加の禁止import、メソッドシグネチャを検証する。
 */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class ServiceArchitectureTest {

  @ArchTest
  static final ArchRule interfaceNameShouldEndWithService =
      classes()
          .that()
          .resideInAPackage(Packages.SERVICE)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("Service")
          .as("serviceパッケージのインターフェース名は「Service」で終わる必要がある");

  @ArchTest
  static final ArchRule implNameShouldEndWithServiceImpl =
      classes()
          .that()
          .resideInAPackage(Packages.SERVICE_IMPL)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("ServiceImpl")
          .as("service.implパッケージのクラス名は「ServiceImpl」で終わる必要がある");

  @ArchTest
  static void serviceInterfaceAndImplShouldBePaired(JavaClasses classes) {
    InterfaceImplPairing.verify(classes, Packages.SERVICE, Packages.SERVICE_IMPL, "Impl");
  }

  @ArchTest
  static final ArchRule implShouldBeAnnotatedWithService =
      classes()
          .that()
          .resideInAPackage(Packages.SERVICE_IMPL)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .beAnnotatedWith(Service.class)
          .as("service.implパッケージのクラスには@Serviceを付与すること");

  @ArchTest
  static final ArchRule publicMethodsShouldBeAnnotatedWithTransactional =
      methods()
          .that()
          .areDeclaredInClassesThat(
              resideInAPackage(Packages.SERVICE_IMPL).and(ArchPredicates.TOP_LEVEL_CLASSES))
          .and()
          .arePublic()
          .should()
          .beAnnotatedWith(Transactional.class)
          .as("service.implのpublicメソッドには@Transactionalを付与すること");

  @ArchTest
  static final ArchRule serviceShouldNotDependOnForbiddenPackages =
      noClasses()
          .that()
          .resideInAPackage(Packages.SERVICE + "..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              Packages.CONTROLLER + "..",
              Packages.MAPPER + "..",
              Packages.ENTITY + "..",
              Packages.DTO + "..",
              Packages.REPOSITORY_IMPL + "..")
          .as("serviceはcontroller配下・mapper・entity・dto・repository.implパッケージに依存してはいけない");

  @ArchTest
  static final ArchRule interfaceMethodsShouldHaveAllowedSignature =
      methods()
          .that()
          .areDeclaredInClassesThat(
              resideInAPackage(Packages.SERVICE).and(ArchPredicates.TOP_LEVEL_CLASSES))
          .should(
              MethodSignatureConditions.haveAllowedSignature(
                  new String[] {Packages.MODEL, Packages.AGGREGATE, Packages.DOMAIN},
                  new String[] {Packages.MODEL, Packages.AGGREGATE, Packages.DOMAIN},
                  true,
                  3))
          .as(
              "Serviceインターフェースのメソッドは、引数がModel・集約・ドメインクラスのみ、"
                  + "返り値がModel・集約・ドメインクラス・Boolean・Integer・voidのいずれかで、引数は3個以下であること");
}
