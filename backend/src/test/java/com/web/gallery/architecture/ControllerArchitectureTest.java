package com.web.gallery.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.web.gallery.constant.ApiRoutes;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * {@code .claude/rules/controller.md}に定義されたController層のアーキテクチャルールを検証するテスト
 *
 * <p>レイヤー間の依存方向（Controller→Repository禁止等）は{@link com.web.gallery.ArchitectureTest}で検証済みのため、本クラスでは
 * 命名規則、追加の禁止import、Responseファクトリメソッド経由の生成、APIルートの一元管理を検証する。
 */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class ControllerArchitectureTest {

  private static final Set<Class<? extends Annotation>> MAPPING_ANNOTATIONS =
      Set.of(
          GetMapping.class,
          PostMapping.class,
          PutMapping.class,
          DeleteMapping.class,
          RequestMapping.class);

  private static final Set<String> API_ROUTE_VALUES = loadApiRouteValues();

  @ArchTest
  static final ArchRule classNameShouldEndWithControllerOrControllerAdvice =
      classes()
          .that()
          .resideInAPackage(Packages.CONTROLLER)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("Controller")
          .orShould()
          .haveSimpleNameEndingWith("ControllerAdvice")
          .as("controllerパッケージのクラス名は「Controller」または「ControllerAdvice」で終わる必要がある");

  @ArchTest
  static final ArchRule controllerShouldNotDependOnMapperEntityDtoOrServiceImpl =
      noClasses()
          .that()
          .resideInAPackage(Packages.CONTROLLER)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              Packages.MAPPER + "..",
              Packages.ENTITY + "..",
              Packages.DTO + "..",
              Packages.SERVICE_IMPL + "..")
          .as("controllerはmapper/entity/dto/service.implパッケージに依存してはいけない");

  @ArchTest
  static final ArchRule responseShouldBeCreatedThroughFactoryMethod =
      noClasses()
          .that()
          .resideInAPackage(Packages.CONTROLLER)
          .should(notDirectlyConstructResponseObjects())
          .as("controllerはResponseクラスをnew/builder()で直接生成せず、from/ofファクトリメソッドを経由すること");

  @ArchTest
  static final ArchRule mappingPathShouldBeDefinedInApiRoutes =
      methods()
          .that()
          .areDeclaredInClassesThat(
              resideInAPackage(Packages.CONTROLLER).and(ArchPredicates.TOP_LEVEL_CLASSES))
          .should(haveMappingPathDefinedInApiRoutes())
          .as("@GetMapping等のパスはApiRoutesクラスの定数値のいずれかと一致すること");

  private static ArchCondition<JavaClass> notDirectlyConstructResponseObjects() {
    return new ArchCondition<JavaClass>(
        "not directly construct classes in " + Packages.CONTROLLER_RESPONSE) {
      @Override
      public void check(JavaClass controllerClass, ConditionEvents events) {
        for (JavaConstructorCall call : controllerClass.getConstructorCallsFromSelf()) {
          JavaClass targetOwner = call.getTargetOwner();
          if (AllowedTypes.isInPackage(targetOwner, Packages.CONTROLLER_RESPONSE)) {
            events.add(
                SimpleConditionEvent.violated(
                    controllerClass,
                    String.format(
                        "%sはResponseクラス(%s)のコンストラクタを直接呼び出しています: %s",
                        controllerClass.getSimpleName(),
                        targetOwner.getSimpleName(),
                        call.getDescription())));
          }
        }
        for (JavaMethodCall call : controllerClass.getMethodCallsFromSelf()) {
          JavaClass targetOwner = call.getTargetOwner();
          if (AllowedTypes.isInPackage(targetOwner, Packages.CONTROLLER_RESPONSE)
              && "builder".equals(call.getTarget().getName())) {
            events.add(
                SimpleConditionEvent.violated(
                    controllerClass,
                    String.format(
                        "%sはResponseクラス(%s)のbuilder()を直接呼び出しています: %s",
                        controllerClass.getSimpleName(),
                        targetOwner.getSimpleName(),
                        call.getDescription())));
          }
        }
      }
    };
  }

  private static ArchCondition<JavaMethod> haveMappingPathDefinedInApiRoutes() {
    return new ArchCondition<JavaMethod>("have mapping path defined in ApiRoutes") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        for (Annotation annotation : method.reflect().getAnnotations()) {
          if (!MAPPING_ANNOTATIONS.contains(annotation.annotationType())) {
            continue;
          }
          for (String path : extractPaths(annotation)) {
            if (!path.isEmpty() && !API_ROUTE_VALUES.contains(path)) {
              events.add(
                  SimpleConditionEvent.violated(
                      method,
                      String.format(
                          "%s#%sのマッピングパス「%s」はApiRoutesクラスの定数値のいずれとも一致しません",
                          method.getOwner().getSimpleName(), method.getName(), path)));
            }
          }
        }
      }
    };
  }

  private static String[] extractPaths(Annotation annotation) {
    try {
      Method valueMethod = annotation.annotationType().getMethod("value");
      return (String[]) valueMethod.invoke(annotation);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  private static Set<String> loadApiRouteValues() {
    Set<String> values = new HashSet<>();
    for (Field field : ApiRoutes.class.getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers())
          && Modifier.isFinal(field.getModifiers())
          && field.getType() == String.class) {
        try {
          values.add((String) field.get(null));
        } catch (IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      }
    }
    return values;
  }
}
