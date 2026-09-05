package com.web.gallery;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * レイヤードアーキテクチャ（Controller → Service → Repository → Mapper）の依存方向を検証するテスト。
 *
 * <p>従来{@code scripts/check-architecture.sh}で行っていたレイヤー間・レイヤー内の依存関係チェックをArchUnitで検証する。
 * DBやSpringコンテキストを必要とせずクラスパス解析のみで完結するため、単体テストとして{@code unitTest}タスクで実行される。
 */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  private static final String CONTROLLER_PKG = "com.web.gallery.controller";
  private static final String SERVICE_PKG = "com.web.gallery.service";
  private static final String REPOSITORY_PKG = "com.web.gallery.repository";

  @ArchTest
  static final ArchRule controllerShouldNotDependOnRepository =
      noClasses()
          .that()
          .resideInAPackage(CONTROLLER_PKG)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(REPOSITORY_PKG + "..")
          .as("Controllerはrepositoryパッケージに依存してはいけない（スキップ違反）");

  @ArchTest
  static final ArchRule serviceShouldNotDependOnController =
      noClasses()
          .that()
          .resideInAPackage(SERVICE_PKG + "..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage(CONTROLLER_PKG)
          .as("Serviceはcontrollerパッケージに依存してはいけない（逆方向の依存）");

  @ArchTest
  static final ArchRule repositoryShouldNotDependOnController =
      noClasses()
          .that()
          .resideInAPackage(REPOSITORY_PKG + "..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage(CONTROLLER_PKG)
          .as("Repositoryはcontrollerパッケージに依存してはいけない（逆方向の依存）");

  @ArchTest
  static final ArchRule repositoryShouldNotDependOnService =
      noClasses()
          .that()
          .resideInAPackage(REPOSITORY_PKG + "..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage(SERVICE_PKG + "..")
          .as("Repositoryはserviceパッケージに依存してはいけない（逆方向の依存）");

  // CommonRestControllerAdviceは@RestControllerAdvice(assignableTypes = {...})で
  // 横断的関心事として全Controllerクラスを列挙しており、
  // 本ルールが禁止したい「Controllerが他Controllerの機能を呼び出す」設計とは性質が異なるため対象外とする
  @ArchTest
  static final ArchRule controllerShouldNotDependOnOtherController =
      noClasses()
          .that()
          .resideInAPackage(CONTROLLER_PKG)
          .and()
          .areNotAnnotatedWith(RestControllerAdvice.class)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(CONTROLLER_PKG)
          .as("Controllerは他のControllerに依存してはいけない（同レイヤー間の依存）");

  @ArchTest
  static final ArchRule serviceImplShouldOnlyDependOnOwnInterface =
      classes()
          .that()
          .resideInAPackage(SERVICE_PKG + ".impl")
          .should(onlyDependOnOwnInterfaceWithin(SERVICE_PKG))
          .as("ServiceImplは自身が実装するServiceインターフェース以外のserviceパッケージに依存してはいけない（同レイヤー間の依存）");

  @ArchTest
  static final ArchRule repositoryImplShouldOnlyDependOnOwnInterface =
      classes()
          .that()
          .resideInAPackage(REPOSITORY_PKG + ".impl")
          .should(onlyDependOnOwnInterfaceWithin(REPOSITORY_PKG))
          .as("RepositoryImplは自身が実装するRepositoryインターフェース以外のrepositoryパッケージに依存してはいけない（同レイヤー間の依存）");

  /**
   * 指定したレイヤーパッケージ配下のImplクラスが、自身が{@code
   * implements}しているインターフェース以外の同レイヤーパッケージ配下のクラスに依存していないことを検証するConditionを生成する。
   *
   * @param layerPackage 対象レイヤーの基底パッケージ（例: "com.web.gallery.service"）
   * @return 生成したArchCondition
   */
  private static ArchCondition<JavaClass> onlyDependOnOwnInterfaceWithin(String layerPackage) {
    DescribedPredicate<JavaClass> inLayer = resideInAPackage(layerPackage + "..");
    return new ArchCondition<JavaClass>("only depend on its own interface within " + layerPackage) {
      @Override
      public void check(JavaClass implClass, ConditionEvents events) {
        var ownInterfaces = implClass.getRawInterfaces();
        for (Dependency dependency : implClass.getDirectDependenciesFromSelf()) {
          JavaClass target = dependency.getTargetClass();
          if (belongToSameTopLevelClass(implClass, target)) {
            continue;
          }
          if (inLayer.test(target) && !ownInterfaces.contains(target)) {
            String message =
                String.format(
                    "%sは自身が実装するインターフェース以外の%s配下のクラス(%s)に依存しています: %s",
                    implClass.getSimpleName(),
                    layerPackage,
                    target.getFullName(),
                    dependency.getDescription());
            events.add(SimpleConditionEvent.violated(implClass, message));
          }
        }
      }
    };
  }

  /**
   * 2つのクラスが同一のトップレベルクラスに属するかどうかを判定する。
   *
   * <p>匿名クラス・メンバークラス（{@code TransactionSynchronization}の実装等）は外側のクラスへの暗黙の参照（{@code
   * this$0}フィールド等）を持つが、これは同一クラス内で完結する参照であり、レイヤー間・レイヤー内の依存関係チェックの対象外とする。
   *
   * @param a 比較対象のクラス
   * @param b 比較対象のクラス
   * @return 同一のトップレベルクラスに属する場合true
   */
  private static boolean belongToSameTopLevelClass(JavaClass a, JavaClass b) {
    return topLevelClassOf(a).equals(topLevelClassOf(b));
  }

  private static JavaClass topLevelClassOf(JavaClass javaClass) {
    JavaClass current = javaClass;
    while (current.getEnclosingClass().isPresent()) {
      current = current.getEnclosingClass().get();
    }
    return current;
  }
}
