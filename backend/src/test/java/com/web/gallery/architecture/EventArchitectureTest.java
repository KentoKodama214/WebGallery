package com.web.gallery.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** {@code .claude/rules/event.md}に定義されたドメインイベント・リスナークラスのアーキテクチャルールを検証するテスト */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class EventArchitectureTest {

  @ArchTest
  static final ArchRule eventClassShouldOnlyHaveDomainFields =
      classes()
          .that()
          .resideInAPackage(Packages.EVENT)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .and()
          .haveSimpleNameEndingWith("Event")
          .should(FieldTypeConditions.haveOnlyFieldsOfType(Packages.DOMAIN))
          .as("Eventクラスのプロパティはドメインクラスのみとすること");

  @ArchTest
  static final ArchRule listenerClassShouldBeAnnotatedWithComponent =
      classes()
          .that()
          .resideInAPackage(Packages.EVENT)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .and()
          .haveSimpleNameEndingWith("Listener")
          .should()
          .beAnnotatedWith(Component.class)
          .as("Listenerクラスには@Componentを付与すること");

  @ArchTest
  static final ArchRule listenerMethodsShouldBeAnnotatedWithTransactionalEventListenerAfterCommit =
      methods()
          .that()
          .areDeclaredInClassesThat(
              resideInAPackage(Packages.EVENT)
                  .and(ArchPredicates.TOP_LEVEL_CLASSES)
                  .and(simpleNameEndingWith("Listener")))
          .and()
          .arePublic()
          .should(beAnnotatedWithTransactionalEventListenerAfterCommit())
          .as("Listenerのハンドリングメソッドには@TransactionalEventListener(phase = AFTER_COMMIT)を付与すること");

  private static ArchCondition<JavaMethod> beAnnotatedWithTransactionalEventListenerAfterCommit() {
    return new ArchCondition<JavaMethod>(
        "be annotated with @TransactionalEventListener(phase = AFTER_COMMIT)") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        TransactionalEventListener annotation =
            method.tryGetAnnotationOfType(TransactionalEventListener.class).orElse(null);
        if (annotation == null) {
          events.add(
              SimpleConditionEvent.violated(
                  method,
                  String.format(
                      "%s#%sには@TransactionalEventListenerが付与されていません",
                      method.getOwner().getSimpleName(), method.getName())));
        } else if (annotation.phase() != TransactionPhase.AFTER_COMMIT) {
          events.add(
              SimpleConditionEvent.violated(
                  method,
                  String.format(
                      "%s#%sの@TransactionalEventListenerのphaseがAFTER_COMMITではありません",
                      method.getOwner().getSimpleName(), method.getName())));
        }
      }
    };
  }
}
