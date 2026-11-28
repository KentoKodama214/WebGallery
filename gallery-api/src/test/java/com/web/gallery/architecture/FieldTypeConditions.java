package com.web.gallery.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/** クラスのフィールド（プロパティ）の型が禁止パッケージに属していないことを検証する{@link ArchCondition}を生成するユーティリティ */
final class FieldTypeConditions {

  /**
   * フィールドの型がいずれの禁止パッケージにも属していないことを検証する{@link ArchCondition}を生成する
   *
   * @param forbiddenPackages 禁止するパッケージ名の一覧
   * @return 生成した{@link ArchCondition}
   */
  static ArchCondition<JavaClass> notHaveFieldsOfType(String... forbiddenPackages) {
    return new ArchCondition<JavaClass>("not have fields typed in forbidden packages") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        for (JavaField field : javaClass.getFields()) {
          JavaClass fieldType = field.getRawType();
          if (AllowedTypes.isInAnyPackage(fieldType, forbiddenPackages)) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "%sのフィールド%s(%s)の型は許可されていません",
                        javaClass.getSimpleName(), field.getName(), fieldType.getName())));
          }
        }
      }
    };
  }

  /**
   * すべてのフィールドの型が、いずれかの許可パッケージに属していることを検証する{@link ArchCondition}を生成する
   *
   * @param allowedPackages 許可するパッケージ名の一覧
   * @return 生成した{@link ArchCondition}
   */
  static ArchCondition<JavaClass> haveOnlyFieldsOfType(String... allowedPackages) {
    return new ArchCondition<JavaClass>("have only fields typed in allowed packages") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        for (JavaField field : javaClass.getFields()) {
          JavaClass fieldType = field.getRawType();
          if (!AllowedTypes.isInAnyPackage(fieldType, allowedPackages)) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "%sのフィールド%s(%s)の型は許可されていません",
                        javaClass.getSimpleName(), field.getName(), fieldType.getName())));
          }
        }
      }
    };
  }

  private FieldTypeConditions() {}
}
