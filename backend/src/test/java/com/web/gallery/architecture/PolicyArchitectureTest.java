package com.web.gallery.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Component;

/** {@code .claude/rules/policy.md}に定義されたPolicyクラス（ドメインサービス）のアーキテクチャルールを検証するテスト */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class PolicyArchitectureTest {

  @ArchTest
  static final ArchRule classNameShouldEndWithPolicy =
      classes()
          .that()
          .resideInAPackage(Packages.POLICY)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("Policy")
          .as("policyパッケージのクラス名は「Policy」で終わる必要がある");

  @ArchTest
  static final ArchRule policyShouldBeAnnotatedWithComponent =
      classes()
          .that()
          .resideInAPackage(Packages.POLICY)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .beAnnotatedWith(Component.class)
          .as("policyパッケージのクラスには@Componentを付与すること");

  @ArchTest
  static final ArchRule publicMethodsShouldHaveAllowedSignature =
      methods()
          .that()
          .areDeclaredInClassesThat(
              resideInAPackage(Packages.POLICY).and(ArchPredicates.TOP_LEVEL_CLASSES))
          .and()
          .arePublic()
          .should(
              MethodSignatureConditions.haveAllowedSignature(
                  new String[] {Packages.DOMAIN, Packages.ENUMERATION},
                  new String[] {Packages.DOMAIN},
                  false,
                  Integer.MAX_VALUE))
          .as("Policyのpublicメソッドは、引数がドメインクラス・Enumのみ、返り値がドメインクラス・Boolean・Integerのいずれかであること");
}
