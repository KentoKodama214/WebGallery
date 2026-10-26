package com.web.gallery.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Set;

/** クラスに特定名のメソッドが存在することを検証する{@link ArchCondition}を生成するユーティリティ */
final class MethodExistenceConditions {

  /**
   * クラスにいずれかの名前のstatic public メソッドが存在することを検証する{@link ArchCondition}を生成する
   *
   * @param methodNames 許容するメソッド名の一覧
   * @return 生成した{@link ArchCondition}
   */
  static ArchCondition<JavaClass> haveStaticFactoryMethod(Set<String> methodNames) {
    return new ArchCondition<JavaClass>("have a static factory method named " + methodNames) {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        boolean hasFactoryMethod =
            javaClass.getMethods().stream()
                .anyMatch(
                    method ->
                        methodNames.contains(method.getName())
                            && method.getModifiers().contains(JavaModifier.STATIC)
                            && method.getModifiers().contains(JavaModifier.PUBLIC));
        if (!hasFactoryMethod) {
          events.add(
              SimpleConditionEvent.violated(
                  javaClass,
                  String.format(
                      "%sには%sのいずれかの名前のpublic staticファクトリメソッドが存在しません",
                      javaClass.getSimpleName(), methodNames)));
        }
      }
    };
  }

  private MethodExistenceConditions() {}
}
