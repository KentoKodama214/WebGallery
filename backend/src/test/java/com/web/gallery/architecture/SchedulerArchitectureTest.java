package com.web.gallery.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.web.gallery.constant.Consts;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@code .claude/rules/scheduler.md}に定義されたSchedulerクラスのアーキテクチャルールを検証するテスト
 *
 * <p>{@code @RequiredArgsConstructor}のLombokアノテーション規約は{@code RetentionPolicy.SOURCE}のためバイトコード解析である
 * ArchUnitでは検証不可能であり、本クラスの対象外とする（{@code backend-architecture-checker}によるレビューで担保する）。
 */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class SchedulerArchitectureTest {

  private static final String CONDITIONAL_ON_PROPERTY_PREFIX = "app.scheduler";

  @ArchTest
  static final ArchRule classNameShouldEndWithScheduler =
      classes()
          .that()
          .resideInAPackage(Packages.SCHEDULER)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("Scheduler")
          .as("schedulerパッケージのクラス名は「Scheduler」で終わる必要がある");

  @ArchTest
  static final ArchRule schedulerShouldBeAnnotatedWithComponent =
      classes()
          .that()
          .resideInAPackage(Packages.SCHEDULER)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .beAnnotatedWith(Component.class)
          .as("schedulerパッケージのクラスには@Componentを付与すること");

  @ArchTest
  static final ArchRule scheduledMethodShouldUseAsiaTokyoZone =
      methods()
          .that()
          .areAnnotatedWith(Scheduled.class)
          .and()
          .areDeclaredInClassesThat(
              resideInAPackage(Packages.SCHEDULER).and(ArchPredicates.TOP_LEVEL_CLASSES))
          .should(useAsiaTokyoZone())
          .as("@Scheduledのzoneは常にAsia/Tokyo（Consts.ZONE_ID_ASIA_TOKYO）を指定すること");

  @ArchTest
  static final ArchRule conditionalOnPropertyShouldUseSchedulerPrefix =
      classes()
          .that()
          .resideInAPackage(Packages.SCHEDULER)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .and()
          .areAnnotatedWith(ConditionalOnProperty.class)
          .should(useSchedulerConditionalOnPropertyPrefix())
          .as("@ConditionalOnPropertyのprefixは「app.scheduler」を指定すること");

  @ArchTest
  static final ArchRule schedulerShouldNotDependOnForbiddenPackages =
      noClasses()
          .that()
          .resideInAPackage(Packages.SCHEDULER)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              Packages.CONTROLLER + "..",
              Packages.REPOSITORY + "..",
              Packages.MAPPER + "..",
              Packages.ENTITY + "..",
              Packages.DTO + "..",
              Packages.AGGREGATE + "..",
              Packages.SERVICE_IMPL + "..")
          .as(
              "schedulerはcontroller配下・repository・mapper・entity・dto・aggregate・service.implパッケージに依存してはいけない");

  @ArchTest
  static final ArchRule schedulerShouldNotDependOnOtherScheduler =
      noClasses()
          .that()
          .resideInAPackage(Packages.SCHEDULER)
          .should()
          .dependOnClassesThat(
              resideInAPackage(Packages.SCHEDULER).and(ArchPredicates.TOP_LEVEL_CLASSES))
          .as("Schedulerは他のSchedulerクラスに依存してはいけない");

  private static ArchCondition<JavaMethod> useAsiaTokyoZone() {
    return new ArchCondition<JavaMethod>("use zone = Asia/Tokyo") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        Scheduled annotation = method.getAnnotationOfType(Scheduled.class);
        if (!Consts.ZONE_ID_ASIA_TOKYO.equals(annotation.zone())) {
          events.add(
              SimpleConditionEvent.violated(
                  method,
                  String.format(
                      "%s#%sの@ScheduledのzoneがAsia/Tokyoではありません（実際: %s）",
                      method.getOwner().getSimpleName(), method.getName(), annotation.zone())));
        }
      }
    };
  }

  private static ArchCondition<JavaClass> useSchedulerConditionalOnPropertyPrefix() {
    return new ArchCondition<JavaClass>(
        "use @ConditionalOnProperty(prefix = \"" + CONDITIONAL_ON_PROPERTY_PREFIX + "\")") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        ConditionalOnProperty annotation =
            javaClass.getAnnotationOfType(ConditionalOnProperty.class);
        if (!CONDITIONAL_ON_PROPERTY_PREFIX.equals(annotation.prefix())) {
          events.add(
              SimpleConditionEvent.violated(
                  javaClass,
                  String.format(
                      "%sの@ConditionalOnPropertyのprefixが「%s」ではありません（実際: %s）",
                      javaClass.getSimpleName(),
                      CONDITIONAL_ON_PROPERTY_PREFIX,
                      annotation.prefix())));
        }
      }
    };
  }
}
