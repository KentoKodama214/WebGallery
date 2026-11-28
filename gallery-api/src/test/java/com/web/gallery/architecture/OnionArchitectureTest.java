package com.web.gallery.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * オニオンアーキテクチャの4層（domain → application → infrastructure/presentation）の依存方向を検証するテスト。
 *
 * <p>同心円の内側は外側を知らないことを機械的に保証する。domainは何にも依存せず、applicationはdomainのみに、
 * infrastructureとpresentationはdomain・applicationに依存してよい。infrastructureとpresentationは互いに外側同士
 * （アダプタ同士）であり、一方向のみ許可する： presentationはinfrastructureに依存してよいが、逆は禁止する （{@code
 * .claude/rules/architecture-overview.md}に明記）。
 */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class OnionArchitectureTest {

  @ArchTest
  static final ArchRule domainShouldNotDependOnOtherLayers =
      noClasses()
          .that()
          .resideInAPackage(Packages.DOMAIN_ROOT + "..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              Packages.APPLICATION_ROOT + "..",
              Packages.INFRASTRUCTURE_ROOT + "..",
              Packages.PRESENTATION_ROOT + "..")
          .as("domainはapplication・infrastructure・presentationのいずれにも依存してはいけない（最内層）");

  @ArchTest
  static final ArchRule applicationShouldNotDependOnOuterLayers =
      noClasses()
          .that()
          .resideInAPackage(Packages.APPLICATION_ROOT + "..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              Packages.INFRASTRUCTURE_ROOT + "..", Packages.PRESENTATION_ROOT + "..")
          .as("applicationはinfrastructure・presentationに依存してはいけない");

  @ArchTest
  static final ArchRule infrastructureShouldNotDependOnPresentation =
      noClasses()
          .that()
          .resideInAPackage(Packages.INFRASTRUCTURE_ROOT + "..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage(Packages.PRESENTATION_ROOT + "..")
          .as("infrastructureはpresentationに依存してはいけない（presentationからinfrastructureへの一方向のみ許可）");
}
