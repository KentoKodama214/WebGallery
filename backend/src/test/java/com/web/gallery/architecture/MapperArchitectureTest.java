package com.web.gallery.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/** {@code .claude/rules/mapper.md}に定義されたMapper層のアーキテクチャルールを検証するテスト */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class MapperArchitectureTest {

  @ArchTest
  static final ArchRule classNameShouldEndWithMapper =
      classes()
          .that()
          .resideInAPackage(Packages.MAPPER)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should()
          .haveSimpleNameEndingWith("Mapper")
          .as("mapperパッケージのインターフェース名は「Mapper」で終わる必要がある");

  @ArchTest
  static final ArchRule mapperShouldHaveCorrespondingXmlFile =
      classes()
          .that()
          .resideInAPackage(Packages.MAPPER)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .should(haveCorrespondingXmlFile())
          .as("Mapperインターフェースには対応するXMLファイルがresources/com/web/gallery/mapper/配下に存在すること");

  private static ArchCondition<JavaClass> haveCorrespondingXmlFile() {
    return new ArchCondition<JavaClass>("have a corresponding XML file") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        String resourcePath = "com/web/gallery/mapper/" + javaClass.getSimpleName() + ".xml";
        if (getClass().getClassLoader().getResource(resourcePath) == null) {
          events.add(
              SimpleConditionEvent.violated(
                  javaClass,
                  String.format(
                      "%sに対応するXMLファイル(%s)が存在しません", javaClass.getSimpleName(), resourcePath)));
        }
      }
    };
  }
}
